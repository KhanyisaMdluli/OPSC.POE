package com.opsc.solowork_1.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.opsc.solowork_1.MainActivity
import com.opsc.solowork_1.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID_DEFAULT = "default_notifications"
        const val CHANNEL_ID_TASKS = "task_notifications"
        const val CHANNEL_ID_EVENTS = "event_notifications"
        const val CHANNEL_ID_SYNC = "sync_notifications"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed FCM token: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            sendNotification(
                title = notification.title ?: getString(R.string.app_name),
                message = notification.body ?: "",
                channelId = CHANNEL_ID_DEFAULT
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        try {
            val type = data["type"] ?: "general"
            val title = data["title"] ?: getString(R.string.app_name)
            val message = data["message"] ?: ""
            val additionalData = data["data"]

            val channelId = when (type) {
                "task_reminder" -> CHANNEL_ID_TASKS
                "event_reminder" -> CHANNEL_ID_EVENTS
                "sync_complete" -> CHANNEL_ID_SYNC
                else -> CHANNEL_ID_DEFAULT
            }

            sendNotification(
                title = title,
                message = message,
                channelId = channelId,
                additionalData = additionalData
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error handling data message: ${e.message}")
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        channelId: String,
        additionalData: String? = null
    ) {
        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            additionalData?.let { putExtra("notification_data", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = System.currentTimeMillis().toInt()

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        with(NotificationManagerCompat.from(this)) {
            try {
                notify(notificationId, notificationBuilder.build())
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException when showing notification: ${e.message}")
            }
        }
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = when (channelId) {
                CHANNEL_ID_TASKS -> "Task Reminders"
                CHANNEL_ID_EVENTS -> "Event Reminders"
                CHANNEL_ID_SYNC -> "Sync Notifications"
                else -> "General Notifications"
            }

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = when (channelId) {
                    CHANNEL_ID_TASKS -> "Notifications for task reminders and deadlines"
                    CHANNEL_ID_EVENTS -> "Notifications for calendar events and schedules"
                    CHANNEL_ID_SYNC -> "Notifications for sync status and offline actions"
                    else -> "General app notifications"
                }
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        Log.d(TAG, "FCM Token: $token")
    }
}