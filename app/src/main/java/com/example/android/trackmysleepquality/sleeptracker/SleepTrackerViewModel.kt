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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 * ViewModel yerine AndroidViewModel kullandık. Bu yüzden application geçtik.
 * Bunu kullanan bileşen, sleep dao aracılığıyla veritabanı işlemi gerçekleşecektir.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    init {
        // ViewModel created durumunda bu metod çalışacak
        initializeTonight()
    }

    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }


    // 1. Adm
    // Coroutine ' nin yapacağı işi yönetebilmek adına oluşturduk.
    // Çünkü Koroinin bir işi vardır ve bu iş buradan yönetilecek
    private var viewModelJob = Job()
    // İlgili koroin nerede çalışacak ViewModel ' da çalışacağı için ViewModel ' da UI ' a bağlı.
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Logic şu şekilde işler. Kullanıcı start düğmesine tıkladığında
     * SleepNight nesnesi oluşturulur daha sonra stop düğmesi gözlemlenir eğer tıklanırsa
     * Bu nesnenin ilgili gece verisi günclellenir bu yüzden bu değişim gözlemlenmelidir.
     */
    private var tonight = MutableLiveData<SleepNight?>()


    // ViewModel ölünce Coroutine ' de ki işlerde ölsün. MemoryLeak yaşanmasın.
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.stopTimeMilli = System.currentTimeMillis()
            update(oldNight)
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }


}



