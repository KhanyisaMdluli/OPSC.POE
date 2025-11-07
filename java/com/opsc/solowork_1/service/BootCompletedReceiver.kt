package com.opsc.solowork_1.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.opsc.solowork_1.database.AppDatabase
import com.opsc.solowork_1.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device boot completed or app updated, restoring alarms...")
                restoreScheduledNotifications(context)
            }
        }
    }

    private fun restoreScheduledNotifications(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val taskDao = database.taskDao()

                // Re-initialize notification channels
                NotificationUtils.initializeNotificationChannels(context)

                Log.d(TAG, "Scheduled notifications restored successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring scheduled notifications: ${e.message}")
            }
        }
    }
}