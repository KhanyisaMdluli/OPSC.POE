package com.opsc.solowork_1.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.opsc.solowork_1.model.FocusSession
import java.util.*

object FocusModeUtils {
    private val db = FirebaseFirestore.getInstance()

    fun saveFocusSession(
        session: FocusSession,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val sessionData = hashMapOf(
            "duration" to session.duration,
            "completedAt" to session.completedAt,
            "userId" to session.userId
        )

        db.collection("focus_sessions")
            .add(sessionData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to save focus session")
            }
    }

    fun getFocusSessions(
        userId: String,
        onSuccess: (List<FocusSession>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("focus_sessions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val sessions = mutableListOf<FocusSession>()
                for (document in documents) {
                    val session = FocusSession(
                        id = document.id,
                        duration = document.getLong("duration") ?: 0,
                        completedAt = document.getDate("completedAt") ?: Date(),
                        userId = document.getString("userId") ?: ""
                    )
                    sessions.add(session)
                }
                onSuccess(sessions.sortedByDescending { it.completedAt })
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch focus sessions")
            }
    }

    fun getTotalFocusTime(
        userId: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        getFocusSessions(
            userId = userId,
            onSuccess = { sessions ->
                val totalTime = sessions.sumOf { it.duration }
                onSuccess(totalTime)
            },
            onError = onError
        )
    }
}