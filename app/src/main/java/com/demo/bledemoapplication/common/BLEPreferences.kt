package com.demo.bledemoapplication.common

import android.content.Context
import android.content.SharedPreferences
import com.demo.bledemoapplication.BuildConfig

/**
 * This Class to maintain shared preference
 */
class BLEPreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(BuildConfig.PREFERENCE, Context.MODE_PRIVATE)
    private val prefEdit: SharedPreferences.Editor = preferences.edit()

    companion object {
        private const val KEY_BLE_READ_DATA = "ble_read_data"
        private const val KEY_FIREBASE_DEVICE_TOKEN = "firebase_device_token"
    }

    /**
     * Property contains the ble device read data
     */
    var readData: String?
        get() = preferences.getString(KEY_BLE_READ_DATA, null)
        set(value) = prefEdit.putString(KEY_BLE_READ_DATA, value).apply()

    var firebaseDeviceToken: String?
        get() = preferences.getString(KEY_FIREBASE_DEVICE_TOKEN, null)
        set(value) = prefEdit.putString(KEY_FIREBASE_DEVICE_TOKEN, value).apply()

    /**
     * To clear the preference data
     */
    fun clearPreferenceData() {
        prefEdit.clear().apply()
    }
}