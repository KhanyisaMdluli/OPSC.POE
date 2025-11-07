package com.opsc.solowork_1.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.opsc.solowork_1.model.Note
import com.google.firebase.Timestamp

object NotesUtils {
    private val db = FirebaseFirestore.getInstance()

    fun addNote(
        note: Note,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val noteData = hashMapOf(
            "title" to note.title,
            "content" to note.content,
            "category" to note.category,
            "userId" to note.userId,
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        db.collection("notes")
            .add(noteData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add note")
            }
    }

    fun updateNote(
        noteId: String,
        note: Note,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val noteData = hashMapOf(
            "title" to note.title,
            "content" to note.content,
            "category" to note.category,
            "updatedAt" to Timestamp.now()
        )

        db.collection("notes").document(noteId)
            .update(noteData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update note") }
    }

    fun deleteNote(
        noteId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("notes").document(noteId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to delete note") }
    }
}