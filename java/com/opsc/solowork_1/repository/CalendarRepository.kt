package com.opsc.solowork_1.repository

import android.util.Log
import com.opsc.solowork_1.api.ApiService
import com.opsc.solowork_1.api.MockEvent
import com.opsc.solowork_1.api.RetrofitClient
import com.opsc.solowork_1.model.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CalendarRepository {
    private val apiService: ApiService = RetrofitClient.apiService

    companion object {
        private const val TAG = "CalendarRepository"
    }

    suspend fun syncEventsToCalendar(events: List<CalendarEvent>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                events.forEach { event ->
                    addEventToCalendar(event)
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync events to calendar", e)
                false
            }
        }
    }

    suspend fun addEventToCalendar(event: CalendarEvent): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val mockEvent = event.toMockEvent()
                val response = apiService.createEvent(mockEvent)
                if (response.isSuccessful) {
                    Log.d(TAG, "Event added to calendar: ${event.title}")
                    true
                } else {
                    Log.e(TAG, "Failed to add event: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add event to calendar: ${event.title}", e)
                false
            }
        }
    }

    suspend fun getCalendarEvents(): List<CalendarEvent> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEvents()
                if (response.isSuccessful) {
                    val mockEvents = response.body() ?: emptyList()
                    mockEvents.map { it.toCalendarEvent() }
                } else {
                    Log.e(TAG, "Failed to get events: ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get events from calendar", e)
                emptyList()
            }
        }
    }

    suspend fun updateEventInCalendar(eventId: String, event: CalendarEvent): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val mockEvent = event.toMockEvent()
                val response = apiService.updateEvent(eventId, mockEvent)
                if (response.isSuccessful) {
                    Log.d(TAG, "Event updated in calendar: ${event.title}")
                    true
                } else {
                    Log.e(TAG, "Failed to update event: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update event in calendar: ${event.title}", e)
                false
            }
        }
    }

    suspend fun deleteEventFromCalendar(eventId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteEvent(eventId)
                if (response.isSuccessful) {
                    Log.d(TAG, "Event deleted from calendar: $eventId")
                    true
                } else {
                    Log.e(TAG, "Failed to delete event: ${response.code()}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete event from calendar: $eventId", e)
                false
            }
        }
    }
}

// ADD EXTENSION FUNCTIONS HERE - at the bottom of CalendarRepository.kt
fun CalendarEvent.toMockEvent(): MockEvent {
    return MockEvent(
        id = if (id.isNotEmpty()) id else null,
        title = title,
        description = description,
        eventType = eventType,
        location = location,
        startTime = startTime.time,
        endTime = endTime.time,
        isAllDay = isAllDay,
        userId = userId,
        createdAt = null
    )
}

fun MockEvent.toCalendarEvent(): CalendarEvent {
    return CalendarEvent(
        id = id ?: "",
        title = title,
        description = description,
        eventType = eventType,
        location = location,
        startTime = Date(startTime),
        endTime = Date(endTime),
        isAllDay = isAllDay,
        userId = userId
    )
}