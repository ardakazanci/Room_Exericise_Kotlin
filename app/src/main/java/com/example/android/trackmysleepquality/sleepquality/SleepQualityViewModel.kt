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

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.*

/**
 * @param sleepNightKey : Üzerinde puan verilecek SleepNight nesnesi index i
 * @param database : Veritabanı işlemleri için Dao nesnesi.
 */
class SleepQualityViewModel(
        private val sleepNightKey: Long = 0L,
        val database: SleepDatabaseDao) : ViewModel() {

    // ViewModel içerisinde kullanılacak coroutine ' nin yönetileceği iş nesnesi.
    private val viewModelJob = Job()
    // Coroutine sonuçları viewmodel kapsamı içerisinde çalışacak. Çünkü viewModel sonuçları Main Thread üzerinde işlenecek
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigateToSleepTracker = MutableLiveData<Boolean>()
    val navigateToSleepTracker: LiveData<Boolean?>
        get() = _navigateToSleepTracker

    fun doNavigateUp() {
        _navigateToSleepTracker.value = null
    }

    // Seçilen puana göre ilgili key ' e ait SleepNight nesnesini güncelle.
    fun onSetSleepQuality(quality: Int) {
        uiScope.launch {

            withContext(Dispatchers.IO) {
                // withContext aslında globalScope.async
                val tonight = database.get(sleepNightKey) ?: return@withContext
                tonight.sleepQuality = quality
                database.update(tonight)
            }
            _navigateToSleepTracker.value = true
        }
    }

    // ViewModel öldürüğünde coroutine ' ni sonlandır.
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}
