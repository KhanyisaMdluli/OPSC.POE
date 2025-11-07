package com.opsc.solowork_1.model

import java.util.Date

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val priority: String = "Medium", // Low, Medium, High
    val dueDate: Date? = null,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val userId: String = "" // To associate task with user
) {
    companion object {
        const val PRIORITY_LOW = "Low"
        const val PRIORITY_MEDIUM = "Medium"
        const val PRIORITY_HIGH = "High"
    }
}