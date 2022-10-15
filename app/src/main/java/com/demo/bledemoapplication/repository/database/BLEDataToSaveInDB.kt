package com.demo.bledemoapplication.repository.database

import androidx.room.*

/**
 * This class represents the room db data to be stored
 */
@Entity(tableName = "ble_device_data")
data class BLEDataToSaveInDB(@PrimaryKey(autoGenerate = true) var id: Int = 0, @field:ColumnInfo(name = "read_data") var data: String?)