package com.opsc.solowork_1.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.opsc.solowork_1.database.converter.DateConverter
import java.util.*

@Entity(tableName = "notifications")
@TypeConverters(DateConverter::class)
data class NotificationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val type: String = "general",
    val timestamp: Date = Date(),
    val read: Boolean = false,
    val additionalData: String? = null,
    val userId: String = "",
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_GENERAL = "general"
        const val TYPE_TASK_REMINDER = "task_reminder"
        const val TYPE_EVENT_REMINDER = "event_reminder"
        const val TYPE_SYNC = "sync"
        const val TYPE_FOCUS_COMPLETE = "focus_complete"
    }
}