package com.demo.bledemoapplication.common.interfaces

import com.demo.bledemoapplication.common.BLEDeviceListData

/**
 * The interface to listen for ble device connect
 */
interface BLEDeviceConnectCallback {
    /**
     * Invoked when click the specific device
     *
     * @param deviceData The clicked device data
     */
    fun onDeviceClicked(deviceData: BLEDeviceListData)
}