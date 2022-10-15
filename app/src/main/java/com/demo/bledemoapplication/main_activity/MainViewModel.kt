package com.demo.bledemoapplication.main_activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.demo.bledemoapplication.common.BLEDeviceListData

/**
 * The main screen view model
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    val deviceData by lazy { mutableListOf<BLEDeviceListData>() }
    var distinctDeviceData: MutableList<BLEDeviceListData>? = null
}