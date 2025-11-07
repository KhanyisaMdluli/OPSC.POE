package com.opsc.solowork_1.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.opsc.solowork_1.model.TimetableEntry
import java.util.Date

object TimetableUtils {
    private val db = FirebaseFirestore.getInstance()

    fun addTimetableEntry(
        entry: TimetableEntry,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val entryData = hashMapOf(
            "courseName" to entry.courseName,
            "courseCode" to entry.courseCode,
            "dayOfWeek" to entry.dayOfWeek,
            "startTime" to entry.startTime,
            "endTime" to entry.endTime,
            "location" to entry.location,
            "instructor" to entry.instructor,
            "color" to entry.color,
            "createdAt" to entry.createdAt,
            "userId" to entry.userId
        )

        db.collection("timetable_entries")
            .add(entryData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add timetable entry")
            }
    }

    fun updateTimetableEntry(
        entryId: String,
        entry: TimetableEntry,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val entryData = hashMapOf(
            "courseName" to entry.courseName,
            "courseCode" to entry.courseCode,
            "dayOfWeek" to entry.dayOfWeek,
            "startTime" to entry.startTime,
            "endTime" to entry.endTime,
            "location" to entry.location,
            "instructor" to entry.instructor,
            "color" to entry.color
        )

        db.collection("timetable_entries").document(entryId)
            .update(entryData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update timetable entry") }
    }

    fun deleteTimetableEntry(
        entryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("timetable_entries").document(entryId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to delete timetable entry") }
    }

    fun getTimetableEntries(
        userId: String,
        onSuccess: (List<TimetableEntry>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("timetable_entries")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<TimetableEntry>()
                for (document in documents) {
                    val entry = TimetableEntry(
                        id = document.id,
                        courseName = document.getString("courseName") ?: "",
                        courseCode = document.getString("courseCode") ?: "",
                        dayOfWeek = document.getString("dayOfWeek") ?: "",
                        startTime = document.getString("startTime") ?: "",
                        endTime = document.getString("endTime") ?: "",
                        location = document.getString("location") ?: "",
                        instructor = document.getString("instructor") ?: "",
                        color = document.getString("color") ?: "#2196F3",
                        createdAt = document.getDate("createdAt") ?: Date(),
                        userId = document.getString("userId") ?: ""
                    )
                    entries.add(entry)
                }
                onSuccess(entries)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch timetable entries")
            }
    }

    fun getTimetableEntriesForDay(
        userId: String,
        dayOfWeek: String,
        onSuccess: (List<TimetableEntry>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("timetable_entries")
            .whereEqualTo("userId", userId)
            .whereEqualTo("dayOfWeek", dayOfWeek)
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<TimetableEntry>()
                for (document in documents) {
                    val entry = TimetableEntry(
                        id = document.id,
                        courseName = document.getString("courseName") ?: "",
                        courseCode = document.getString("courseCode") ?: "",
                        dayOfWeek = document.getString("dayOfWeek") ?: "",
                        startTime = document.getString("startTime") ?: "",
                        endTime = document.getString("endTime") ?: "",
                        location = document.getString("location") ?: "",
                        instructor = document.getString("instructor") ?: "",
                        color = document.getString("color") ?: "#2196F3",
                        createdAt = document.getDate("createdAt") ?: Date(),
                        userId = document.getString("userId") ?: ""
                    )
                    entries.add(entry)
                }
                // Sort by start time
                val sortedEntries = entries.sortedBy { it.startTime }
                onSuccess(sortedEntries)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch timetable entries for day")
            }
    }
}