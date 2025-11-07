package com.opsc.solowork_1.database.dao

import androidx.room.*
import com.opsc.solowork_1.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTasksByUser(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 AND dueDate IS NOT NULL AND dueDate > :currentTime ORDER BY dueDate ASC")
    fun getUpcomingTasks(userId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: String)

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 ORDER BY " +
            "CASE priority " +
            "WHEN 'High' THEN 1 " +
            "WHEN 'Medium' THEN 2 " +
            "WHEN 'Low' THEN 3 " +
            "ELSE 4 END, dueDate ASC")
    fun getTasksByPriority(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND isCompleted = 0")
    suspend fun getPendingTaskCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND isCompleted = 1 AND createdAt >= :startDate")
    suspend fun getCompletedTasksCount(userId: String, startDate: Long): Int
}