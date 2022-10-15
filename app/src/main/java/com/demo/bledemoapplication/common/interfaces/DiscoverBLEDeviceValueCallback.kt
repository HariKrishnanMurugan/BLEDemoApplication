package com.demo.bledemoapplication.common.interfaces

/**
 * The interface for communicate the result back to activity
 */
interface DiscoverBLEDeviceValueCallback {
    /**
     * Invoked when value is discovered from the ble device
     *
     * @param data The value of the discovered success device
     */
    fun discoveredSuccess(data: String?)

    /**
     * Invoked when discovered failed
     */
    fun discoveredFailed()

    /**
     * Invoked when disconnected
     */
    fun disconnected()
}