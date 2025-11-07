package com.opsc.solowork_1.model

import java.util.Date

data class TimetableEntry(
    val id: String = "",
    val courseName: String = "",
    val courseCode: String = "",
    val dayOfWeek: String = "", // Monday, Tuesday, etc.
    val startTime: String = "", // Format: "HH:mm"
    val endTime: String = "", // Format: "HH:mm"
    val location: String = "",
    val instructor: String = "",
    val color: String = "#2196F3", // Color code for the course
    val createdAt: Date = Date(),
    val userId: String = "" // To associate timetable with user
) {
    companion object {
        val DAYS_OF_WEEK = arrayOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
        )

        val DEFAULT_COLORS = arrayOf(
            "#2196F3", "#FF9800", "#4CAF50", "#9C27B0", "#F44336", "#607D8B", "#795548"
        )
    }
}