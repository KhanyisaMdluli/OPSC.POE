package com.opsc.solowork_1.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.opsc.solowork_1.database.converter.DateConverter
import java.util.*

@Entity(tableName = "notifications")
@TypeConverters(DateConverter::class)
data class NotificationData(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general",
    val timestamp: Date = Date(),
    val read: Boolean = false,
    val additionalData: String? = null
)