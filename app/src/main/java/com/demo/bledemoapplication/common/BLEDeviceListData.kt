package com.demo.bledemoapplication.common

import android.bluetooth.BluetoothDevice

/**
 * The data class represents the ble device list
 */
data class BLEDeviceListData(
    val deviceResult: BluetoothDevice? = null,
    val deviceName: String? = null,
    val deviceAddress: String? = null,
)