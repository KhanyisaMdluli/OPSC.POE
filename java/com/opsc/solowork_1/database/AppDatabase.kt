package com.opsc.solowork_1.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.opsc.solowork_1.database.converter.DateConverter
import com.opsc.solowork_1.database.dao.NoteDao
import com.opsc.solowork_1.database.dao.NotificationDao
import com.opsc.solowork_1.database.dao.TaskDao
import com.opsc.solowork_1.database.entity.NoteEntity
import com.opsc.solowork_1.database.entity.NotificationEntity
import com.opsc.solowork_1.database.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(
    entities = [NoteEntity::class, TaskEntity::class, NotificationEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2 (adding notifications table)
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create notifications table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `notifications` (
                        `id` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `message` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `timestamp` INTEGER, 
                        `read` INTEGER NOT NULL, 
                        `additionalData` TEXT, 
                        `userId` TEXT NOT NULL, 
                        `isSynced` INTEGER NOT NULL, 
                        `lastModified` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // Create index for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_userId` ON `notifications` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_timestamp` ON `notifications` (`timestamp`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_read` ON `notifications` (`read`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solowork_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(databaseCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val databaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("AppDatabase", "Database created successfully")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    cleanupOldData(db)
                }
            }
        }

        private fun cleanupOldData(db: SupportSQLiteDatabase) {
            try {
                // Delete notifications older than 90 days
                val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
                db.execSQL("DELETE FROM notifications WHERE timestamp < ?", arrayOf(ninetyDaysAgo))

                Log.d("AppDatabase", "Old data cleanup completed")
            } catch (e: Exception) {
                Log.e("AppDatabase", "Error during data cleanup")
            }
        }
    }
}