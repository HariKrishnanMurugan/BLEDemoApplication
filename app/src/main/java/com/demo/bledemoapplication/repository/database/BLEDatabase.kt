package com.demo.bledemoapplication.repository.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.demo.bledemoapplication.common.BLEApplication

@Database(entities = [BLEDataToSaveInDB::class], version = 1, exportSchema = true)
abstract class BLEDatabase : RoomDatabase() {

    abstract fun bleDao(): BLEDao

    companion object {

        @Volatile
        private var INSTANCE: BLEDatabase? = null

        /**
         * To get the instance of the [BLEDatabase]
         *
         * @return The instance of the Database
         */
        fun getInstance(): BLEDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(BLEApplication.getInstance(), BLEDatabase::class.java, "BLE Device Data").build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}