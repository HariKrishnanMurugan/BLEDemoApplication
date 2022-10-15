package com.demo.bledemoapplication.common

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.demo.bledemoapplication.common.receivers.BLELocationStateReceiver
import com.demo.bledemoapplication.common.receivers.BLENetworkStateReceiver
import com.demo.bledemoapplication.repository.network.manager.BLEApiManager
import com.google.firebase.FirebaseApp
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * This application class contains the global instances for the whole project
 */
class BLEApplication : Application() {
    private lateinit var preferences: BLEPreferences
    private lateinit var apiManager: BLEApiManager

    override fun onCreate() {
        super.onCreate()
        initInstance()
    }

    /**
     * To initial the instances which are using for whole project
     */
    private fun initInstance() {
        instance = this
        preferences = BLEPreferences(this)
        apiManager = BLEApiManager()
        FirebaseApp.initializeApp(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startNetworkListenerAboveN()
        } else {
            registerNetworkStateListener()
        }
        registerGPSListener()
    }

    /**
     * To register network state broadcast receiver
     */
    private fun registerNetworkStateListener() {
        val networkIntentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(BLENetworkStateReceiver(), networkIntentFilter)
    }

    /**
     * To register gps state broadcast receiver
     */
    private fun registerGPSListener() {
        val locationIntentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(BLELocationStateReceiver(), locationIntentFilter)
    }

    /**
     * To get the preference instance
     *
     * @return The preference instance
     */
    fun getPrefs(): BLEPreferences = preferences

    /**
     * To get the api manager instance
     *
     * @return The api manager instance
     */
    fun getApiManager(): BLEApiManager = apiManager

    companion object {
        private val TAG = this::class.java.simpleName
        private lateinit var instance: BLEApplication
        var isNetworkConnected: Boolean = false
        var isGpsEnabled: Boolean = false

        /**
         * To get the application instance
         *
         * @return The application instance
         */
        fun getInstance(): BLEApplication = instance

        /**
         * To start the network listener
         */
        @RequiresApi(Build.VERSION_CODES.N)
        fun startNetworkListenerAboveN() {
            try {
                val connectivityManager = getInstance().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

                if (networkCapabilities == null) {
                    updateNetworkStatus(false)
                }

                connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        updateNetworkStatus(true)
                    }

                    override fun onLost(network: Network) {
                        updateNetworkStatus(false)
                    }

                    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                        updateNetworkStatus(isInternetActive())
                    }

                    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                        updateNetworkStatus(isInternetActive())
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "startNetworkListenerAboveN: Caught Exception: ${e.message}")
            }
        }

        /**
         * To update the network state
         */
        fun updateNetworkState() {
            val connectivityManager = getInstance().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            val isNetworkAvailable = activeNetwork?.let {
                activeNetwork.type == ConnectivityManager.TYPE_MOBILE || activeNetwork.type == ConnectivityManager.TYPE_WIFI
            } ?: false
            updateNetworkStatus(isNetworkAvailable)
        }

        /**
         * To update the network status
         *
         * @param isNetworkAvailable Whether the network is available or not
         */
        private fun updateNetworkStatus(isNetworkAvailable: Boolean) {
            isNetworkConnected = isNetworkAvailable
            // To notify further in any
            EventBus.getDefault().post(BLENetworkStateChangedEvent(isNetworkConnected))
        }

        /**
         * To check whether connected internet has active or not
         *
         * @return The connected internet is active or not
         */
        private fun isInternetActive(): Boolean {
            try {
                val url = URL("https://www.google.co.in")
                val conn: HttpURLConnection? = url.openConnection() as HttpURLConnection?
                conn?.let {
                    it.setRequestProperty("Connection", "close")
                    with(BLEConstants) {
                        it.connectTimeout = MAX_TIMEOUT_DURATION
                        return it.responseCode == SUCCESS_RESPONSE_CODE
                    }
                } ?: return false
            } catch (e: IOException) {
                Log.e(TAG, "isInternetActive: Caught exception: ${e.message}")
                return false
            }
        }
    }
}