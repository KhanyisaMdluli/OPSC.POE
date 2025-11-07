package com.opsc.solowork_1.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.opsc.solowork_1.model.Task
import java.util.*

object TaskUtils {
    private val db = FirebaseFirestore.getInstance()

    fun getTasks(
        userId: String,
        onSuccess: (List<Task>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val tasks = mutableListOf<Task>()
                for (document in documents) {
                    val task = Task(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: "",
                        priority = document.getString("priority") ?: Task.PRIORITY_MEDIUM,
                        dueDate = document.getDate("dueDate"),
                        isCompleted = document.getBoolean("isCompleted") ?: false,
                        userId = document.getString("userId") ?: "",
                        createdAt = document.getDate("createdAt") ?: Date()
                    )
                    tasks.add(task)
                }
                onSuccess(tasks)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch tasks")
            }
    }

    fun addTask(
        task: Task,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val taskData = hashMapOf(
            "title" to task.title,
            "description" to task.description,
            "priority" to task.priority,
            "dueDate" to task.dueDate,
            "isCompleted" to task.isCompleted,
            "userId" to task.userId,
            "createdAt" to task.createdAt
        )

        db.collection("tasks")
            .add(taskData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add task")
            }
    }

    fun updateTask(
        taskId: String,
        task: Task,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val taskData = hashMapOf(
            "title" to task.title,
            "description" to task.description,
            "priority" to task.priority,
            "dueDate" to task.dueDate,
            "isCompleted" to task.isCompleted
        )

        db.collection("tasks").document(taskId)
            .update(taskData as Map<String, Any>)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update task") }
    }

    fun deleteTask(
        taskId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("tasks").document(taskId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to delete task") }
    }
}