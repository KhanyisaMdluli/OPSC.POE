package com.opsc.solowork_1.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.opsc.solowork_1.model.CalendarEvent
import java.util.Date

object CalendarUtils {
    private val db = FirebaseFirestore.getInstance()

    fun addEvent(
        event: CalendarEvent,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val eventData = hashMapOf(
            "title" to event.title,
            "description" to event.description,
            "startTime" to event.startTime,
            "endTime" to event.endTime,
            "eventType" to event.eventType,
            "location" to event.location,
            "isAllDay" to event.isAllDay,
            "createdAt" to event.createdAt,
            "userId" to event.userId
        )

        db.collection("calendar_events")
            .add(eventData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add event")
            }
    }

    fun updateEvent(
        eventId: String,
        event: CalendarEvent,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val eventData = hashMapOf(
            "title" to event.title,
            "description" to event.description,
            "startTime" to event.startTime,
            "endTime" to event.endTime,
            "eventType" to event.eventType,
            "location" to event.location,
            "isAllDay" to event.isAllDay
        )

        db.collection("calendar_events").document(eventId)
            .update(eventData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update event") }
    }

    fun deleteEvent(
        eventId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("calendar_events").document(eventId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to delete event") }
    }

    fun getEvents(
        userId: String,
        onSuccess: (List<CalendarEvent>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("calendar_events")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val events = mutableListOf<CalendarEvent>()
                for (document in documents) {
                    val event = CalendarEvent(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        startTime = document.getDate("startTime") ?: Date(),
                        endTime = document.getDate("endTime") ?: Date(),
                        eventType = document.getString("eventType") ?: CalendarEvent.TYPE_PERSONAL,
                        location = document.getString("location") ?: "",
                        isAllDay = document.getBoolean("isAllDay") ?: false,
                        createdAt = document.getDate("createdAt") ?: Date(),
                        userId = document.getString("userId") ?: ""
                    )
                    events.add(event)
                }
                onSuccess(events)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch events")
            }
    }

    fun getEventsForDate(
        userId: String,
        date: Date,
        onSuccess: (List<CalendarEvent>) -> Unit,
        onError: (String) -> Unit
    ) {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.time

        db.collection("calendar_events")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startTime", startOfDay)
            .whereLessThan("startTime", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                val events = mutableListOf<CalendarEvent>()
                for (document in documents) {
                    val event = CalendarEvent(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        startTime = document.getDate("startTime") ?: Date(),
                        endTime = document.getDate("endTime") ?: Date(),
                        eventType = document.getString("eventType") ?: CalendarEvent.TYPE_PERSONAL,
                        location = document.getString("location") ?: "",
                        isAllDay = document.getBoolean("isAllDay") ?: false,
                        createdAt = document.getDate("createdAt") ?: Date(),
                        userId = document.getString("userId") ?: ""
                    )
                    events.add(event)
                }
                onSuccess(events)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch events for date")
            }
    }
}