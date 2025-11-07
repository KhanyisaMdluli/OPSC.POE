package com.opsc.solowork_1.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.opsc.solowork_1.MainActivity
import com.opsc.solowork_1.R
import com.opsc.solowork_1.database.AppDatabase
import com.opsc.solowork_1.database.entity.NotificationEntity
import com.opsc.solowork_1.model.Task
import com.opsc.solowork_1.repository.TaskRepository
import com.opsc.solowork_1.service.MyFirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationUtils {

    fun initializeNotificationChannels(context: Context) {
        createNotificationChannel(
            context,
            MyFirebaseMessagingService.CHANNEL_ID_DEFAULT,
            "General Notifications",
            "General app notifications and updates"
        )

        createNotificationChannel(
            context,
            MyFirebaseMessagingService.CHANNEL_ID_TASKS,
            "Task Reminders",
            "Notifications for task reminders and deadlines"
        )

        createNotificationChannel(
            context,
            MyFirebaseMessagingService.CHANNEL_ID_EVENTS,
            "Event Reminders",
            "Notifications for calendar events and schedules"
        )

        createNotificationChannel(
            context,
            MyFirebaseMessagingService.CHANNEL_ID_SYNC,
            "Sync Notifications",
            "Notifications for sync status and offline actions"
        )
    }

    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendLocalNotification(
        context: Context,
        title: String,
        message: String,
        type: String = "general",
        data: Map<String, String>? = null
    ) {
        // Check if we have notification permission
        if (!areNotificationsEnabled(context)) {
            Log.w("NotificationUtils", "Notifications are disabled, skipping: $title")
            return
        }

        val channelId = when (type) {
            "task", "task_reminder" -> MyFirebaseMessagingService.CHANNEL_ID_TASKS
            "event", "event_reminder" -> MyFirebaseMessagingService.CHANNEL_ID_EVENTS
            "sync" -> MyFirebaseMessagingService.CHANNEL_ID_SYNC
            else -> MyFirebaseMessagingService.CHANNEL_ID_DEFAULT
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            data?.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationId = System.currentTimeMillis().toInt()

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(type))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add action buttons based on notification type
        when (type) {
            "task_reminder" -> {
                val markDoneIntent = Intent(context, com.opsc.solowork_1.service.NotificationActionReceiver::class.java).apply {
                    putExtra("action", "mark_done")
                    putExtra("notification_id", notificationId)
                    data?.forEach { (key, value) -> putExtra(key, value) }
                }
                val markDonePendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    markDoneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                notificationBuilder.addAction(
                    R.drawable.ic_check,
                    "Mark Done",
                    markDonePendingIntent
                )
            }
            "event_reminder" -> {
                val viewEventIntent = Intent(context, com.opsc.solowork_1.service.NotificationActionReceiver::class.java).apply {
                    putExtra("action", "view_event")
                    putExtra("notification_id", notificationId)
                    data?.forEach { (key, value) -> putExtra(key, value) }
                }
                val viewEventPendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + 1,
                    viewEventIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                notificationBuilder.addAction(
                    R.drawable.ic_calendar,
                    "View Event",
                    viewEventPendingIntent
                )
            }
        }

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notificationBuilder.build())
            }
            Log.d("NotificationUtils", "Notification sent: $title")
        } catch (e: SecurityException) {
            Log.e("NotificationUtils", "SecurityException when sending notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error sending notification: ${e.message}")
        }

        // Save notification to local database
        saveNotificationToDatabase(context, title, message, type, data)
    }

    private fun saveNotificationToDatabase(
        context: Context,
        title: String,
        message: String,
        type: String,
        data: Map<String, String>?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val notificationDao = database.notificationDao()
                val currentUser = AuthUtils.getCurrentUser()

                val additionalData = data?.let { map ->
                    JSONObject(map).toString()
                }

                val notificationEntity = NotificationEntity(
                    title = title,
                    message = message,
                    type = type,
                    timestamp = Date(),
                    read = false,
                    additionalData = additionalData,
                    userId = currentUser?.uid ?: "unknown"
                )

                notificationDao.insertNotification(notificationEntity)
                Log.d("NotificationUtils", "Notification saved to database: $title")
            } catch (e: Exception) {
                Log.e("NotificationUtils", "Error saving notification to database: ${e.message}")
            }
        }
    }

    fun scheduleTaskReminder(context: Context, task: Task, reminderTime: Long) {
        val timeUntilReminder = reminderTime - System.currentTimeMillis()

        if (timeUntilReminder > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                android.app.AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                createTaskReminderPendingIntent(context, task, reminderTime.toInt())
                            )
                        } else {
                            // Fallback to inexact alarm
                            alarmManager.set(
                                android.app.AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                createTaskReminderPendingIntent(context, task, reminderTime.toInt())
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            createTaskReminderPendingIntent(context, task, reminderTime.toInt())
                        )
                    }

                    Log.d("NotificationUtils", "Task reminder scheduled for: ${Date(reminderTime)}")
                } catch (e: Exception) {
                    Log.e("NotificationUtils", "Error scheduling task reminder: ${e.message}")
                    scheduleTaskReminderFallback(context, task, reminderTime)
                }
            }
        }
    }

    private fun scheduleTaskReminderFallback(context: Context, task: Task, reminderTime: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                reminderTime,
                createTaskReminderPendingIntent(context, task, reminderTime.toInt())
            )
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Fallback scheduling also failed: ${e.message}")
        }
    }

    private fun createTaskReminderPendingIntent(context: Context, task: Task, requestCode: Int): PendingIntent {
        val intent = Intent(context, com.opsc.solowork_1.service.TaskReminderReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_description", task.description)
            putExtra("due_date", task.dueDate?.time)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendSyncNotification(context: Context, success: Boolean, itemsSynced: Int = 0) {
        val title = if (success) "Sync Completed" else "Sync Failed"
        val message = if (success) {
            "Successfully synced $itemsSynced items"
        } else {
            "Failed to sync your data. Please check your connection."
        }

        sendLocalNotification(
            context,
            title,
            message,
            "sync",
            mapOf("items_synced" to itemsSynced.toString(), "success" to success.toString())
        )
    }

    fun sendFocusCompleteNotification(context: Context, duration: Long, sessionId: String) {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60

        val durationText = if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }

        sendLocalNotification(
            context,
            "Focus Session Complete!",
            "Great job! You focused for $durationText",
            "focus_complete",
            mapOf("session_id" to sessionId, "duration" to duration.toString())
        )
    }

    // Complete implementation for marking task as completed
    suspend fun markTaskAsCompleted(context: Context, taskId: String) {
        try {
            val taskRepository = TaskRepository(context)
            taskRepository.markTaskAsCompleted(taskId)

            Log.d("NotificationUtils", "Task marked as completed: $taskId")
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error marking task as completed: ${e.message}")
            // Fallback: Send a notification that the action failed
            sendLocalNotification(
                context,
                "Action Failed",
                "Could not mark task as completed. Please try again.",
                "general"
            )
        }
    }

    // Helper function to create recurring tasks
    suspend fun createRecurringTask(
        context: Context,
        originalTask: Task,
        recurringType: String,
        intervalDays: Int? = null
    ): Task {
        return try {
            val taskRepository = TaskRepository(context)
            val newTask = taskRepository.createRecurringTask(originalTask, recurringType, intervalDays)

            // Send notification about the recurring task creation
            sendLocalNotification(
                context,
                "Recurring Task Created",
                "${originalTask.title} will repeat $recurringType",
                "task_reminder"
            )

            newTask
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error creating recurring task: ${e.message}")
            // Fallback notification
            sendLocalNotification(
                context,
                "Recurring Task Failed",
                "Could not create recurring task for ${originalTask.title}",
                "general"
            )
            throw e
        }
    }

    private fun getNotificationIcon(type: String): Int {
        return when (type) {
            "task", "task_reminder" -> R.drawable.ic_tasks
            "event", "event_reminder" -> R.drawable.ic_calendar
            "sync" -> R.drawable.ic_sync
            "focus_complete" -> R.drawable.ic_focus
            else -> R.drawable.ic_notification
        }
    }

    // Helper function to check if notifications are enabled
    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // Cancel specific notification
    fun cancelNotification(context: Context, notificationId: Int) {
        try {
            NotificationManagerCompat.from(context).cancel(notificationId)
        } catch (e: SecurityException) {
            Log.e("NotificationUtils", "SecurityException when canceling notification: ${e.message}")
        }
    }

    // Cancel all notifications
    fun cancelAllNotifications(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancelAll()
        } catch (e: SecurityException) {
            Log.e("NotificationUtils", "SecurityException when canceling all notifications: ${e.message}")
        }
    }

    // Get unread notification count from database
    suspend fun getUnreadNotificationCount(context: Context): Int {
        return try {
            val database = AppDatabase.getInstance(context)
            val notificationDao = database.notificationDao()
            val userId = AuthUtils.getCurrentUser()?.uid ?: return 0
            notificationDao.getUnreadCount(userId)
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error getting unread count: ${e.message}")
            0
        }
    }

    // Mark notification as read
    suspend fun markNotificationAsRead(context: Context, notificationId: String) {
        try {
            val database = AppDatabase.getInstance(context)
            val notificationDao = database.notificationDao()
            notificationDao.markAsRead(notificationId)
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error marking notification as read: ${e.message}")
        }
    }

    // Mark all notifications as read
    suspend fun markAllNotificationsAsRead(context: Context) {
        try {
            val database = AppDatabase.getInstance(context)
            val notificationDao = database.notificationDao()
            val userId = AuthUtils.getCurrentUser()?.uid ?: return
            notificationDao.markAllAsRead(userId)
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error marking all notifications as read: ${e.message}")
        }
    }

    // Clean up old notifications (older than 30 days)
    suspend fun cleanupOldNotifications(context: Context) {
        try {
            val database = AppDatabase.getInstance(context)
            val notificationDao = database.notificationDao()
            val userId = AuthUtils.getCurrentUser()?.uid ?: return

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -30) // 30 days ago
            val cutoffDate = calendar.time

            notificationDao.deleteOldNotifications(cutoffDate, userId)
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Error cleaning up old notifications: ${e.message}")
        }
    }

    // Schedule multiple reminders for a task (e.g., 1 day before, 1 hour before)
    fun scheduleMultipleTaskReminders(context: Context, task: Task) {
        task.dueDate?.let { dueDate ->
            // Remind 1 day before
            val oneDayBefore = dueDate.time - TimeUnit.DAYS.toMillis(1)
            if (oneDayBefore > System.currentTimeMillis()) {
                scheduleTaskReminder(context, task, oneDayBefore)
            }

            // Remind 1 hour before
            val oneHourBefore = dueDate.time - TimeUnit.HOURS.toMillis(1)
            if (oneHourBefore > System.currentTimeMillis()) {
                scheduleTaskReminder(context, task, oneHourBefore)
            }

            // Remind 15 minutes before
            val fifteenMinutesBefore = dueDate.time - TimeUnit.MINUTES.toMillis(15)
            if (fifteenMinutesBefore > System.currentTimeMillis()) {
                scheduleTaskReminder(context, task, fifteenMinutesBefore)
            }
        }
    }
}