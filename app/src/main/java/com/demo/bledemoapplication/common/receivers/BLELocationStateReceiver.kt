package com.demo.bledemoapplication.common.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.demo.bledemoapplication.common.BLEApplication
import com.demo.bledemoapplication.common.BLELocationDisabledEvent
import org.greenrobot.eventbus.EventBus

/**
 * This class receive when Gps connectivity state changed
 */
class BLELocationStateReceiver : BroadcastReceiver() {
    companion object {
        val TAG: String = this::class.java.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val isProvidersChanged = intent?.action?.matches(LocationManager.PROVIDERS_CHANGED_ACTION.toRegex()) ?: false
            if (isProvidersChanged) {
                val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                val isGpsProviderEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
                Log.d(TAG, "onReceive: isGpsProviderEnabled: $isGpsProviderEnabled")
                if (!isGpsProviderEnabled) {
                    if (BLEApplication.isGpsEnabled) {
                        EventBus.getDefault().post(BLELocationDisabledEvent())
                        BLEApplication.isGpsEnabled = false
                    }
                } else {
                    BLEApplication.isGpsEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "RTLocationStateReceiver: Caught Exception: ${e.message}")
        }
    }
}