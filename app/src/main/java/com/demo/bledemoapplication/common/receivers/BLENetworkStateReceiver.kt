package com.demo.bledemoapplication.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.demo.bledemoapplication.common.BLEApplication

/**
 * This class receive when network connectivity state changed
 */
class BLENetworkStateReceiver : BroadcastReceiver() {
    companion object {
        val TAG: String = this::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        val TAG = this::class.java.simpleName
        try {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                BLEApplication.updateNetworkState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "BLENetworkStateReceiver: onReceive: Caught Exception: ${e.message}")
        }
    }
}