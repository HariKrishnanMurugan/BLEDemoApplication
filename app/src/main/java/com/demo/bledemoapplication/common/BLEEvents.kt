package com.demo.bledemoapplication.common

/**
 *  This event is triggered when the network state changed
 *
 *  @param isNetworkConnected Whether the network is connected or not
 */
class BLENetworkStateChangedEvent(private var isNetworkConnected: Boolean)

/**
 * The event triggered when the Gps location disabled
 */
class BLELocationDisabledEvent