package com.opsc.solowork_1.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.opsc.solowork_1.database.AppDatabase
import com.opsc.solowork_1.database.entity.TaskEntity
import com.opsc.solowork_1.model.Task
import com.opsc.solowork_1.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TaskReminderReceiver"
        const val ACTION_SCHEDULE_RECURRING = "SCHEDULE_RECURRING_TASK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "TaskReminderReceiver triggered with action: ${intent.action}")

        when (intent.action) {
            ACTION_SCHEDULE_RECURRING -> handleRecurringTask(context, intent)
            Intent.ACTION_BOOT_COMPLETED -> handleBootCompleted(context)
            else -> handleTaskReminder(context, intent)
        }
    }

    private fun handleTaskReminder(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id")
        val taskTitle = intent.getStringExtra("task_title")
        val taskDescription = intent.getStringExtra("task_description")
        val dueDate = intent.getLongExtra("due_date", -1L)

        Log.d(TAG, "Task reminder triggered for: $taskTitle (ID: $taskId)")

        if (taskId.isNullOrEmpty() || taskTitle.isNullOrEmpty()) {
            Log.e(TAG, "Invalid task data in reminder")
            return
        }

        // Send notification for task reminder
        val data = mapOf(
            "task_id" to taskId,
            "due_date" to dueDate.toString()
        )

        NotificationUtils.sendLocalNotification(
            context,
            "Task Reminder: $taskTitle",
            taskDescription ?: "You have a task due soon",
            "task_reminder",
            data
        )
    }

    private fun handleRecurringTask(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id")
        val recurringType = intent.getStringExtra("recurring_type")
        val intervalDays = intent.getIntExtra("interval_days", 1)

        Log.d(TAG, "Handling recurring task: $taskId, type: $recurringType")

        if (taskId != null && recurringType != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getInstance(context)
                    val taskDao = database.taskDao()
                    val taskEntity = taskDao.getTaskById(taskId)

                    taskEntity?.let { currentTask ->
                        // Check if task still exists and isn't completed
                        if (!currentTask.isCompleted) {
                            // Create new task instance for next occurrence
                            val newDueDate = calculateNextDueDate(currentTask.dueDate, recurringType, intervalDays)

                            val newTask = Task(
                                id = UUID.randomUUID().toString(),
                                title = currentTask.title,
                                description = currentTask.description,
                                priority = currentTask.priority,
                                dueDate = newDueDate,
                                isCompleted = false,
                                userId = currentTask.userId,
                                createdAt = Date()
                            )

                            // Use NotificationUtils to create the recurring task
                            NotificationUtils.createRecurringTask(context, newTask, recurringType, intervalDays)
                        } else {
                            Log.d(TAG, "Task $taskId is completed, skipping recurring creation")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling recurring task: ${e.message}")
                }
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        Log.d(TAG, "Boot completed, restoring alarms...")
    }

    private fun calculateNextDueDate(currentDueDate: Date?, recurringType: String, intervalDays: Int = 1): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDueDate ?: Date()

        when (recurringType) {
            "daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "monthly" -> calendar.add(Calendar.MONTH, 1)
            "custom" -> calendar.add(Calendar.DAY_OF_YEAR, intervalDays)
            else -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.time
    }
}