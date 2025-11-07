package com.opsc.solowork_1.model

import java.util.*

data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val eventType: String = TYPE_PERSONAL,
    val location: String = "",
    val isAllDay: Boolean = false,
    val userId: String = "",
    val createdAt: Date = Date()
) {
    companion object {
        const val TYPE_PERSONAL = "Personal"
        const val TYPE_WORK = "Work"
        const val TYPE_STUDY = "Study"
        const val TYPE_MEETING = "Meeting"
    }
}