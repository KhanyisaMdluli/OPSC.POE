package com.opsc.solowork_1.model

import java.util.Date

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "General",
    val userId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    companion object {
        const val CATEGORY_GENERAL = "General"
        const val CATEGORY_WORK = "Work"
        const val CATEGORY_PERSONAL = "Personal"
        const val CATEGORY_STUDY = "Study"
        const val CATEGORY_IDEAS = "Ideas"
    }
}