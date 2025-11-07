package com.opsc.solowork_1.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.opsc.solowork_1.model.UserProfile
import java.util.*

object SettingsUtils {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference

    fun getUserProfile(
        userId: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("user_profiles")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = UserProfile(
                        userId = document.id,
                        email = document.getString("email") ?: "",
                        fullName = document.getString("fullName") ?: "",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        bio = document.getString("bio") ?: "",
                        profileImageUrl = document.getString("profileImageUrl") ?: "",
                        createdAt = document.getLong("createdAt") ?: 0,
                        updatedAt = document.getLong("updatedAt") ?: 0,
                        notificationEnabled = document.getBoolean("notificationEnabled") ?: true,
                        darkModeEnabled = document.getBoolean("darkModeEnabled") ?: false,
                        autoSyncEnabled = document.getBoolean("autoSyncEnabled") ?: true
                    )
                    onSuccess(profile)
                } else {
                    // Create default profile if doesn't exist
                    val user = auth.currentUser
                    val defaultProfile = UserProfile(
                        userId = userId,
                        email = user?.email ?: "",
                        fullName = user?.displayName ?: "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    saveUserProfile(defaultProfile, onSuccess, onError)
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch user profile")
            }
    }

    fun saveUserProfile(
        profile: UserProfile,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val profileData = hashMapOf(
            "email" to profile.email,
            "fullName" to profile.fullName,
            "phoneNumber" to profile.phoneNumber,
            "bio" to profile.bio,
            "profileImageUrl" to profile.profileImageUrl,
            "createdAt" to profile.createdAt,
            "updatedAt" to System.currentTimeMillis(),
            "notificationEnabled" to profile.notificationEnabled,
            "darkModeEnabled" to profile.darkModeEnabled,
            "autoSyncEnabled" to profile.autoSyncEnabled
        )

        db.collection("user_profiles")
            .document(profile.userId)
            .set(profileData)
            .addOnSuccessListener {
                onSuccess(profile)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to save profile")
            }
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
            // Re-authenticate user before changing password
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "Failed to update password")
                        }
                }
                .addOnFailureListener { e ->
                    onError("Current password is incorrect")
                }
        } else {
            onError("User not authenticated")
        }
    }

    fun uploadProfileImage(
        userId: String,
        imageUri: android.net.Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageRef = storage.child("profile_images/$userId/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        onSuccess(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to get download URL")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to upload image")
            }
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        val userId = user?.uid

        if (user != null && userId != null) {
            // Delete user data from Firestore
            db.collection("user_profiles").document(userId).delete()
            db.collection("tasks").whereEqualTo("userId", userId).get().addOnSuccessListener { tasks ->
                tasks.forEach { it.reference.delete() }
            }
            db.collection("notes").whereEqualTo("userId", userId).get().addOnSuccessListener { notes ->
                notes.forEach { it.reference.delete() }
            }
            db.collection("calendar_events").whereEqualTo("userId", userId).get().addOnSuccessListener { events ->
                events.forEach { it.reference.delete() }
            }
            db.collection("timetable_entries").whereEqualTo("userId", userId).get().addOnSuccessListener { entries ->
                entries.forEach { it.reference.delete() }
            }
            db.collection("documents").whereEqualTo("userId", userId).get().addOnSuccessListener { documents ->
                documents.forEach { it.reference.delete() }
            }

            // Delete user from Firebase Auth
            user.delete()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "Failed to delete account")
                }
        } else {
            onError("User not authenticated")
        }
    }
}