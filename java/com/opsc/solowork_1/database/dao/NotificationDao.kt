package com.opsc.solowork_1.database.dao

import androidx.room.*
import com.opsc.solowork_1.database.entity.NotificationEntity
import java.util.Date

@Dao
interface NotificationDao {

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsByUser(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE userId = :userId AND read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(userId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getNotificationsByType(userId: String, type: String): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: String)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotifications(userId: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND read = 0")
    suspend fun getUnreadCount(userId: String): Int

    @Query("SELECT * FROM notifications WHERE isSynced = 0")
    suspend fun getUnsyncedNotifications(): List<NotificationEntity>

    @Query("UPDATE notifications SET isSynced = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsSynced(notificationId: String)

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp AND userId = :userId")
    suspend fun deleteOldNotifications(timestamp: Date, userId: String)
}