package com.opsc.solowork_1.repository

import android.content.Context
import com.opsc.solowork_1.database.AppDatabase
import com.opsc.solowork_1.database.entity.TaskEntity
import com.opsc.solowork_1.model.Task
import com.opsc.solowork_1.utils.NotificationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class TaskRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val taskDao = database.taskDao()

    suspend fun saveTask(task: Task) {
        val taskEntity = TaskEntity(
            id = task.id.ifEmpty { UUID.randomUUID().toString() },
            title = task.title,
            description = task.description,
            priority = task.priority,
            dueDate = task.dueDate,
            isCompleted = task.isCompleted,
            userId = task.userId,
            createdAt = task.createdAt,
            isSynced = false,
            lastModified = System.currentTimeMillis()
        )
        taskDao.insertTask(taskEntity)

        // Schedule notification if due date is set
        task.dueDate?.let { dueDate ->
            val reminderTime = dueDate.time - (60 * 60 * 1000) // 1 hour before
            if (reminderTime > System.currentTimeMillis()) {
                NotificationUtils.scheduleTaskReminder(context, task, reminderTime)
            }
        }
    }

    suspend fun updateTask(task: Task) {
        val taskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            priority = task.priority,
            dueDate = task.dueDate,
            isCompleted = task.isCompleted,
            userId = task.userId,
            createdAt = task.createdAt,
            isSynced = false,
            lastModified = System.currentTimeMillis()
        )
        taskDao.updateTask(taskEntity)

        // Reschedule notification if due date changed
        task.dueDate?.let { dueDate ->
            val reminderTime = dueDate.time - (60 * 60 * 1000)
            if (reminderTime > System.currentTimeMillis()) {
                NotificationUtils.scheduleTaskReminder(context, task, reminderTime)
            }
        }
    }

    suspend fun markTaskAsCompleted(taskId: String) {
        val task = taskDao.getTaskById(taskId)
        task?.let {
            val updatedTask = it.copy(
                isCompleted = true,
                lastModified = System.currentTimeMillis(),
                isSynced = false
            )
            taskDao.updateTask(updatedTask)

            // Cancel any pending notifications for this task
            NotificationUtils.cancelNotification(context, taskId.hashCode())

            // Send completion notification
            NotificationUtils.sendLocalNotification(
                context,
                "Task Completed!",
                "Great job completing: ${it.title}",
                "task_completed"
            )
        }
    }

    suspend fun deleteTask(taskId: String) {
        val task = taskDao.getTaskById(taskId)
        task?.let {
            taskDao.deleteTask(it)

            // Cancel any pending notifications for this task
            NotificationUtils.cancelNotification(context, taskId.hashCode())
        }
    }

    fun getTasks(userId: String): Flow<List<Task>> {
        return taskDao.getTasksByUser(userId).map { entities ->
            entities.map { entity ->
                Task(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    priority = entity.priority,
                    dueDate = entity.dueDate,
                    isCompleted = entity.isCompleted,
                    userId = entity.userId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    fun getUpcomingTasks(userId: String): Flow<List<Task>> {
        return taskDao.getUpcomingTasks(userId).map { entities ->
            entities.map { entity ->
                Task(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    priority = entity.priority,
                    dueDate = entity.dueDate,
                    isCompleted = entity.isCompleted,
                    userId = entity.userId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.let { entity ->
            Task(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                priority = entity.priority,
                dueDate = entity.dueDate,
                isCompleted = entity.isCompleted,
                userId = entity.userId,
                createdAt = entity.createdAt
            )
        }
    }

    suspend fun createRecurringTask(
        originalTask: Task,
        recurringType: String,
        intervalDays: Int? = null
    ): Task {
        val newDueDate = calculateNextDueDate(originalTask.dueDate, recurringType, intervalDays)

        val newTask = originalTask.copy(
            id = UUID.randomUUID().toString(),
            dueDate = newDueDate,
            isCompleted = false,
            createdAt = Date()
        )

        saveTask(newTask)

        // Schedule the next recurring instance
        scheduleNextRecurringInstance(newTask, recurringType, intervalDays)

        return newTask
    }

    private fun calculateNextDueDate(currentDueDate: Date?, recurringType: String, intervalDays: Int?): Date {
        return Calendar.getInstance().apply {
            time = currentDueDate ?: Date()
            when (recurringType) {
                TaskEntity.RECURRING_DAILY -> add(Calendar.DAY_OF_YEAR, 1)
                TaskEntity.RECURRING_WEEKLY -> add(Calendar.WEEK_OF_YEAR, 1)
                TaskEntity.RECURRING_MONTHLY -> add(Calendar.MONTH, 1)
                TaskEntity.RECURRING_CUSTOM -> add(Calendar.DAY_OF_YEAR, intervalDays ?: 1)
                else -> add(Calendar.DAY_OF_YEAR, 1)
            }
        }.time
    }

    private fun scheduleNextRecurringInstance(task: Task, recurringType: String, intervalDays: Int?) {
        task.dueDate?.let { dueDate ->
            val nextDueDate = calculateNextDueDate(dueDate, recurringType, intervalDays)
            val reminderTime = nextDueDate.time - (60 * 60 * 1000) // 1 hour before

            if (reminderTime > System.currentTimeMillis()) {
                NotificationUtils.scheduleTaskReminder(context, task, reminderTime)
            }
        }
    }

    // Sync operations
    suspend fun getUnsyncedTasks(): List<TaskEntity> {
        return taskDao.getUnsyncedTasks()
    }

    suspend fun markTaskAsSynced(taskId: String) {
        taskDao.markTaskAsSynced(taskId)
    }
}