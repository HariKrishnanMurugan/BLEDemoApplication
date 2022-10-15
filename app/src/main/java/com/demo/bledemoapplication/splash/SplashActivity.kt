package com.demo.bledemoapplication.splash

import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.demo.bledemoapplication.R
import com.demo.bledemoapplication.common.BLEApplication
import com.demo.bledemoapplication.common.BLEConstants
import com.demo.bledemoapplication.databinding.ActivitySplashBinding
import com.demo.bledemoapplication.main_activity.MainActivity
import com.google.firebase.messaging.FirebaseMessaging

/**
 * The activity class for splash
 */
class SplashActivity : AppCompatActivity() {
    private lateinit var activitySplashBinding: ActivitySplashBinding

    companion object {
        private val TAG = this::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            super.onCreate(savedInstanceState)
            splashScreen.setKeepOnScreenCondition { true }
        } else {
            super.onCreate(savedInstanceState)
            activitySplashBinding = ActivitySplashBinding.inflate(layoutInflater).apply {
                setContentView(this.root)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        moveToNextScreen()
    }

    /**
     * To move to the next screen
     */
    private fun moveToNextScreen() {
        // To get and save the firebase device token for the push notification
        getDeviceToken()
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, BLEConstants.SPLASH_SCREEN_TIME)
    }

    /**
     * To get the firebase device token
     */
    private fun getDeviceToken() {
        try {
            val prefs = BLEApplication.getInstance().getPrefs()
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase Token generated successfully")
                    prefs.firebaseDeviceToken = task.result
                } else {
                    Toast.makeText(this, getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getDeviceToken: Caught exception: ${e.message}", e)
        }
    }
}