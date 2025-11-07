package com.opsc.solowork_1.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.opsc.solowork_1.database.converter.DateConverter
import java.util.Date

@Entity(tableName = "notes")
@TypeConverters(DateConverter::class)
data class NoteEntity(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "General",
    val userId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)