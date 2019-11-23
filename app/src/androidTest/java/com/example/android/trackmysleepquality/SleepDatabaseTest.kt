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

package com.example.android.trackmysleepquality

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class) // Testleri kuran ve yöneten dependency
class SleepDatabaseTest {

    private lateinit var sleepDao: SleepDatabaseDao
    private lateinit var db: SleepDatabase

    // 1. Adım olarak DataBase oluşturuluyor. Database inMemory ile birlikte Bellek üzerinde oluşturuluyor.
    @Before // İlk yapılması gereken işlem
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed. Bellek üzerinde oluşturma sağlanır. İşlem bitince kaldırılır.
        db = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries() // Ana-İş parçacağı üzerinde işlem yapılmasını sağlar. Test e özgüdür.
                .build()
        sleepDao = db.sleepDatabaseDao // Database Sınıfı ile iletişime geçebilir.
    }

    @After // 3. Adım Veritabanı kapatılır.
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test // 2. Adım Test Yapılır.  Dao Insert Function Checked
    @Throws(Exception::class)
    fun insertAndGetNight() {
        val night = SleepNight() // Dummy Veri
        sleepDao.insert(night) // Dummy insert
        val tonight = sleepDao.getTonight() // En son eklenen dummy veri
        assertEquals(tonight?.sleepQuality, -1) // eşit mi değil mi
    }


    /**
     * Dao Update Function
     */
    @Test // Dao Update Function Checked
    @Throws(java.lang.Exception::class)
    fun updateAndGetNight() {
        val night = SleepNight() // Dummy Veri
        night.sleepQuality = 5
        night.startTimeMilli = 15
        night.stopTimeMilli = 20
        sleepDao.insert(night) // Dummy insert
        val tonight = sleepDao.getTonight()
        tonight?.stopTimeMilli = 30
        tonight?.sleepQuality = 10
        tonight?.startTimeMilli = 50
        sleepDao.update(tonight!!)
        //Log.i("Night Quality",""+tonight.sleepQuality)
        //Log.i("Night StartTimeMilli",""+tonight.startTimeMilli)
        //Log.i("Night StopTimeMilli",""+tonight.stopTimeMilli)
    }

    @Test // Dao Get Function Checked
    @Throws(java.lang.Exception::class)
    fun getAndGetNight() {

        val night = SleepNight()
        night.sleepQuality = 10;
        night.startTimeMilli = 10;
        night.stopTimeMilli = 10;
        night.nightId = 5;
        sleepDao.insert(night)
        val getNight = sleepDao.get(5)
        //Log.i("Night Quality",""+getNight?.sleepQuality)
        //Log.i("Night StartTimeMilli",""+getNight?.startTimeMilli)
        //Log.i("Night StopTimeMilli",""+getNight?.stopTimeMilli)
    }

    @Test // Dao Clear Function Checked
    @Throws(java.lang.Exception::class)
    fun clearTableData() {

        for (i in 1..10) {
            val night = SleepNight()
            night.nightId = i.toLong()
            sleepDao.insert(night)
        }

        for (i in 1..10) {

            Log.i("Night Value = > ", "" + sleepDao.get(i.toLong())?.nightId)

        }

        sleepDao.clear()

        for (i in 1..10) {

            Log.i("Night Value = > ", "" + sleepDao.get(i.toLong())?.nightId)

        }

    }







}