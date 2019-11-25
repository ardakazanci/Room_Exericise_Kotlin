/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * @param database : SleepDatabaseDao -> ViewModel içerisinde CRUD yapılacak.
 * @param application : AndroidViewModel ' dan kalıttık.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {


    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    fun doNavigating() {
        _navigateToSleepQuality.value = null
    }


    private var _showSnackbarEvent = MutableLiveData<Boolean>()

    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    /**
     * 1. Adm
     * Coroutine ' nin yapacağı işi yönetebilmek adına oluşturduk.
     * Çünkü Koroinin bir işi vardır ve bu iş buradan yönetilecek.
     * */
    private var viewModelJob = Job()

    // İlgili koroin nerede çalışacak ViewModel ' da çalışacağı için ViewModel ' da UI ' a bağlı.
    // Ek olarak Kapsam olarak Activity-Fragment ' ın yaşam döngüsü sonlandığında dahi çalışacaktır.
    // Ek olarak sonuç olarak alınan veriler UI Thread üzerinde işlenecektir.
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Dao içerisinde LiveData olarak return aldığım için coroutine e gerek yoktur.
    private val nights = database.getAllNights()

    // Transformation kullanmamızın sebebi, tarihler üzerinde değişim sağlıyoruz.
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    /**
     * Logic şu şekilde işler. Kullanıcı start düğmesine tıkladığında
     * SleepNight nesnesi oluşturulur daha sonra stop düğmesi gözlemlenir  tıklanırsa
     * Bu nesnenin ilgili SleepNight objesi güncellenir bu yüzden bu değişim gözlemlenmelidir.
     */
    private var tonight = MutableLiveData<SleepNight?>()


    init {
        // ViewModel created durumunda bu metod çalışacak
        initializeTonight()
    }


    private fun initializeTonight() {
        // İlgili metod içerisinde belirlenen scope içerisinde coroutines başlatılacak
        // coroutesines'in sonucu liveData objemize atanacak
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }

    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.stopTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night
        }


    }

    // Start Butonuna tıklandığında çalışacak metod
    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }

    }

    // Stop Butonuna tıklandığında çalışacak metod
    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.stopTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    // Clear Butonuna tıklandığında çalışacak metod
    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()

        }
    }


    // ViewModel ölünce Coroutine ' de ki işlerde ölsün. MemoryLeak yaşanmasın.
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * SleepNight nesnesi aracılığıyla Butonların aktiflik durumunu kontrol edeceğiz.
     */


    val startButtonVisible = Transformations.map(tonight) {
        it == null
    }
    val stopButtonVisible = Transformations.map(tonight) {
        it != null
    }
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty() // Bu durum gerçekleşiyorsa enabled durumu şekillenecek.
    }


    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }
}



