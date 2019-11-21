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

package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SleepDatabaseDao{

    @Insert // insert işlemi için gerekli kodu oluşturur.
    fun insert(night: SleepNight)

    @Update // update işlemi için gerekli kodu oluşturur.
    // Önemli nokta güncellenecek öğe doğru belirtilmelidir.
    fun update(night: SleepNight)

    /**
     * Metoda gönderilen key değeri , Query içerisinde ki :key değerine gönderiliyor.
     * Metod geriye SleepNight objesi döndürüyor.
     */
    @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
    fun get(key:Long) : SleepNight?

    /**
     * Tablonun kendisini silmez, içeriğini siler.
     * @Delete ek açıklaması, belirli girişleri silmek için mükemmeldir,
     * ancak tüm girişleri bir tablodan silmek için etkili değildir.
     */
    @Query("DELETE FROM daily_sleep_quality_table")
    fun clear()

    /**
     * Azalan düzende sıralama yaptık.
     * İlk değeri aldık.
     * Bu şekilde en son değere ulaştık. (Son gece bilgisi)
     */
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    fun getTonight(): SleepNight?

    /**
     * Tüm bilgileri almak için kullanılan bir sorgudur.
     * LiveData olmasının sebebi, değişim olacağı için gözlemlenebilir olmaılıdır.
     * List olmasının sebebi , birden fazla SleepNight objesi olacaktır.
     * LiveData olmasının diğer bir avantajı, Daha sonra değişimi gözlemlenir sadece.
     */
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>>




}
