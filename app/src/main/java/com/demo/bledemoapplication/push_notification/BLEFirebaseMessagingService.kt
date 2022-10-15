package com.demo.bledemoapplication.push_notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.demo.bledemoapplication.R
import com.demo.bledemoapplication.common.BLEApplication
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * This class represents the firebase messaging service
 */
class BLEFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private val TAG = this::class.java.simpleName
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        val prefs = BLEApplication.getInstance().getPrefs()
        if (prefs.firebaseDeviceToken != newToken) {
            // Store the new token
            prefs.firebaseDeviceToken = newToken
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived - RemoteMessageData - ${message.data}, Notification - ${message.notification}")
        with(message) {
            val notificationTitle = notification?.title ?: getString(R.string.app_name)
            val notificationMessage = notification?.body

            val notificationBuilder =
                NotificationCompat.Builder(this@BLEFirebaseMessagingService, getString(R.string.app_name)).setContentTitle(notificationTitle)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setAutoCancel(true)
                    .setContentText(notificationMessage).setSmallIcon(R.drawable.ic_bluetooth_icon)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(NotificationChannel(getString(R.string.app_name),
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    lightColor = Color.GREEN
                })
            }
            notificationManager.notify(0, notificationBuilder.build())
            playNotificationSound()
        }
    }

    /**
     * To play the notification sound
     */
    private fun playNotificationSound() {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(this, notification)
            r.play()
        } catch (e: Exception) {
            Log.d(TAG, "playNotificationSound: Caught Exception: ${e.message}")
        }
    }
}