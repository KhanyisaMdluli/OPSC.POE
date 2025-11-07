package com.opsc.solowork_1.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.opsc.solowork_1.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action")
        val notificationId = intent.getIntExtra("notification_id", -1)
        val additionalData = intent.getStringExtra("additional_data")

        Log.d(TAG, "Action received: $action for notification: $notificationId")

        when (action) {
            "mark_done" -> handleMarkTaskDone(context, additionalData)
            "view_event" -> handleViewEvent(context, additionalData)
        }

        // Cancel the notification after action is handled
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }

    private fun handleMarkTaskDone(context: Context, additionalData: String?) {
        additionalData?.let { data ->
            try {
                val json = JSONObject(data)
                val taskId = json.getString("task_id")

                CoroutineScope(Dispatchers.IO).launch {
                    // Update task status in database
                    NotificationUtils.markTaskAsCompleted(context, taskId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking task as done: ${e.message}")
            }
        }
    }

    private fun handleViewEvent(context: Context, additionalData: String?) {
        additionalData?.let { data ->
            try {
                val json = JSONObject(data)
                val eventId = json.getString("event_id")

                // Navigate to event details
                val newIntent = Intent(context, Class.forName("com.opsc.solowork_1.CalendarActivity"))
                newIntent.putExtra("event_id", eventId)
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(newIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling view event: ${e.message}")
            }
        }
    }
}