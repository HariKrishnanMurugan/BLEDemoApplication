package com.demo.bledemoapplication.repository.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BLEDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun saveData(data: BLEDataToSaveInDB)

    @Query("SELECT * FROM ble_device_data")
    fun getSavedData(): LiveData<BLEDataToSaveInDB>

    @Query("DELETE FROM ble_device_data")
    fun deleteAllSavedData()

    @Delete
    fun deleteData(data: BLEDataToSaveInDB)
}