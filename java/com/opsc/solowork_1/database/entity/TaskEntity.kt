package com.opsc.solowork_1.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.opsc.solowork_1.database.converter.DateConverter
import java.util.Date

@Entity(tableName = "tasks")
@TypeConverters(DateConverter::class)
data class TaskEntity(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = "Medium",
    val dueDate: Date? = null,
    val isCompleted: Boolean = false,
    val userId: String = "",
    val createdAt: Date = Date(),
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val recurringType: String? = null, // "daily", "weekly", "monthly", "custom"
    val recurringInterval: Int? = null, // For custom intervals in days
    val parentTaskId: String? = null // For recurring task chains
) {
    companion object {
        const val PRIORITY_LOW = "Low"
        const val PRIORITY_MEDIUM = "Medium"
        const val PRIORITY_HIGH = "High"

        const val RECURRING_DAILY = "daily"
        const val RECURRING_WEEKLY = "weekly"
        const val RECURRING_MONTHLY = "monthly"
        const val RECURRING_CUSTOM = "custom"
    }
}