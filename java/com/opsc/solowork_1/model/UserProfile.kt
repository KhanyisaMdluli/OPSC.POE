package com.opsc.solowork_1.model

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val notificationEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val autoSyncEnabled: Boolean = true
)