package com.opsc.solowork_1.model

import java.util.Date

data class Document(
    val id: String = "",
    val fileName: String = "",
    val fileType: String = "",
    val fileSize: Long = 0,
    val uploadDate: Date = Date(),
    val category: String = "General",
    val description: String = "",
    val localFilePath: String = "", // This can be empty
    val userId: String = ""
) {
    companion object {
        const val CATEGORY_GENERAL = "General"
        const val CATEGORY_STUDY = "Study"
        const val CATEGORY_WORK = "Work"
        const val CATEGORY_PERSONAL = "Personal"
        const val CATEGORY_IMPORTANT = "Important"

        fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${String.format("%.1f", size / 1024.0)} KB"
                size < 1024 * 1024 * 1024 -> "${String.format("%.1f", size / (1024.0 * 1024.0))} MB"
                else -> "${String.format("%.1f", size / (1024.0 * 1024.0 * 1024.0))} GB"
            }
        }
    }
}