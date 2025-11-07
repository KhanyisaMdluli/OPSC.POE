package com.opsc.solowork_1.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.opsc.solowork_1.database.AppDatabase
import com.opsc.solowork_1.repository.OfflineRepository
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val offlineRepository = OfflineRepository(context)
    private val database = AppDatabase.getInstance(context)
    private val noteDao = database.noteDao()
    private val taskDao = database.taskDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!offlineRepository.isOnline()) {
                NotificationUtils.sendLocalNotification(
                    applicationContext,
                    "Sync Failed",
                    "No internet connection available",
                    "sync"
                )
                return@withContext Result.retry()
            }

            val userId = AuthUtils.getCurrentUser()?.uid
            if (userId.isNullOrEmpty()) {
                return@withContext Result.failure()
            }

            var totalSynced = 0

            // Sync notes
            val unsyncedNotes = noteDao.getUnsyncedNotes()
            unsyncedNotes.forEach { note ->
                // TODO: Implement actual backend sync logic
                // For now, just mark as synced
                noteDao.markNoteAsSynced(note.id)
                totalSynced++
            }

            // Sync tasks
            val unsyncedTasks = taskDao.getUnsyncedTasks()
            unsyncedTasks.forEach { task ->
                // TODO: Implement actual backend sync logic
                // For now, just mark as synced
                taskDao.markTaskAsSynced(task.id)
                totalSynced++
            }

            // Send success notification
            if (totalSynced > 0) {
                NotificationUtils.sendSyncNotification(applicationContext, true, totalSynced)
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Sync failed: ${e.message}")
            NotificationUtils.sendSyncNotification(applicationContext, false)
            Result.retry()
        }
    }
}