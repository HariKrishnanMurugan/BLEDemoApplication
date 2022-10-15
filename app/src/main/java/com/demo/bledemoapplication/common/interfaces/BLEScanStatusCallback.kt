package com.demo.bledemoapplication.common.interfaces

/**
 * The interface to listen for ble scan status
 */
interface BLEScanStatusCallback {
    /**
     * Invoked when BLE scan started
     */
    fun scanStarted()

    /**
     * Invokedn when BLE scan Stopped
     */
    fun scanStopped()
}