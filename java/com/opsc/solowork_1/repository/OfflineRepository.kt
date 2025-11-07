package com.opsc.solowork_1.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.opsc.solowork_1.database.AppDatabase
import com.opsc.solowork_1.database.entity.NoteEntity
import com.opsc.solowork_1.database.entity.TaskEntity
import com.opsc.solowork_1.model.Note
import com.opsc.solowork_1.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class OfflineRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val noteDao = database.noteDao()
    private val taskDao = database.taskDao()

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Note Operations
    suspend fun saveNote(note: Note) {
        val noteEntity = NoteEntity(
            id = note.id.ifEmpty { UUID.randomUUID().toString() },
            title = note.title,
            content = note.content,
            category = note.category,
            userId = note.userId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            isSynced = isOnline()
        )
        noteDao.insertNote(noteEntity)
    }

    suspend fun updateNote(note: Note) {
        val noteEntity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            category = note.category,
            userId = note.userId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            isSynced = isOnline()
        )
        noteDao.updateNote(noteEntity)
    }

    suspend fun deleteNote(noteId: String) {
        val note = noteDao.getNoteById(noteId)
        note?.let {
            noteDao.deleteNote(it)
        }
    }

    fun getNotes(userId: String): Flow<List<Note>> {
        return noteDao.getNotesByUser(userId).map { entities ->
            entities.map { entity ->
                Note(
                    id = entity.id,
                    title = entity.title,
                    content = entity.content,
                    category = entity.category,
                    userId = entity.userId,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    // Task Operations (simplified for now)
    suspend fun saveTask(task: Task) {
        val taskEntity = TaskEntity(
            id = task.id.ifEmpty { UUID.randomUUID().toString() },
            title = task.title,
            description = task.description,
            priority = task.priority,
            dueDate = task.dueDate,
            isCompleted = task.isCompleted,
            userId = task.userId,
            createdAt = task.createdAt,
            isSynced = isOnline()
        )
        taskDao.insertTask(taskEntity)
    }

    suspend fun updateTask(task: Task) {
        val taskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            priority = task.priority,
            dueDate = task.dueDate,
            isCompleted = task.isCompleted,
            userId = task.userId,
            createdAt = task.createdAt,
            isSynced = isOnline()
        )
        taskDao.updateTask(taskEntity)
    }

    suspend fun deleteTask(taskId: String) {
        val task = taskDao.getTaskById(taskId)
        task?.let {
            taskDao.deleteTask(it)
        }
    }

    fun getTasks(userId: String): Flow<List<Task>> {
        return taskDao.getTasksByUser(userId).map { entities ->
            entities.map { entity ->
                Task(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    priority = entity.priority,
                    dueDate = entity.dueDate,
                    isCompleted = entity.isCompleted,
                    userId = entity.userId,
                    createdAt = entity.createdAt
                )
            }
        }
    }

    // Sync Operations
    suspend fun getUnsyncedNotes(): List<NoteEntity> {
        return noteDao.getUnsyncedNotes()
    }

    suspend fun getUnsyncedTasks(): List<TaskEntity> {
        return taskDao.getUnsyncedTasks()
    }

    suspend fun markNoteAsSynced(noteId: String) {
        noteDao.markNoteAsSynced(noteId)
    }

    suspend fun markTaskAsSynced(taskId: String) {
        taskDao.markTaskAsSynced(taskId)
    }
}