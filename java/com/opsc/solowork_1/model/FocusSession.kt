package com.opsc.solowork_1.model

import java.util.*

data class FocusSession(
    val id: String = "",
    val duration: Long = 0, // in milliseconds
    val completedAt: Date = Date(),
    val userId: String = ""
) {
    fun getFormattedDuration(): String {
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (duration % (1000 * 60)) / 1000

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}