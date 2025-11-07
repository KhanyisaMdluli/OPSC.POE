package com.opsc.solowork_1

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.opsc.solowork_1.databinding.ActivitySettingsBinding
import com.opsc.solowork_1.model.UserProfile
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager
import com.opsc.solowork_1.utils.SettingsUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var userProfile: UserProfile? = null
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        languageManager.applySavedLanguage(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        // Profile
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // Language button
        binding.btnLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // App Settings
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationSetting(isChecked)
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            updateDarkModeSetting(isChecked)
        }

        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            updateAutoSyncSetting(isChecked)
        }

        // Account
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            showPrivacyPolicy()
        }

        binding.btnTermsOfService.setOnClickListener {
            showTermsOfService()
        }

        // Danger Zone
        binding.btnClearData.setOnClickListener {
            showClearDataConfirmation()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            SettingsUtils.getUserProfile(
                userId = userId,
                onSuccess = { profile ->
                    showLoading(false)
                    userProfile = profile
                    updateProfileUI(profile)
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            showLoading(false)
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateProfileUI(profile: UserProfile) {
        binding.tvUserName.text = profile.fullName.ifEmpty { "User" }
        binding.tvUserEmail.text = profile.email

        // Load profile image
        if (profile.profileImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(profile.profileImageUrl)
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivProfile)
        }

        // Update switches
        binding.switchNotifications.isChecked = profile.notificationEnabled
        binding.switchDarkMode.isChecked = profile.darkModeEnabled
        binding.switchAutoSync.isChecked = profile.autoSyncEnabled
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etFullName = dialogView.findViewById<EditText>(R.id.etFullName)
        val etPhoneNumber = dialogView.findViewById<EditText>(R.id.etPhoneNumber)
        val etBio = dialogView.findViewById<EditText>(R.id.etBio)
        val btnChangePhoto = dialogView.findViewById<Button>(R.id.btnChangePhoto)

        // Populate fields
        userProfile?.let { profile ->
            etFullName.setText(profile.fullName)
            etPhoneNumber.setText(profile.phoneNumber)
            etBio.setText(profile.bio)
        }

        btnChangePhoto.setOnClickListener {
            Toast.makeText(this, "Profile photo change feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { dialogInterface, _ ->
                val fullName = etFullName.text.toString().trim()
                val phoneNumber = etPhoneNumber.text.toString().trim()
                val bio = etBio.text.toString().trim()

                if (fullName.isEmpty()) {
                    Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateProfile(fullName, phoneNumber, bio)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun updateProfile(fullName: String, phoneNumber: String, bio: String) {
        showLoading(true)
        val currentProfile = userProfile ?: return
        val updatedProfile = currentProfile.copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            bio = bio
        )

        SettingsUtils.saveUserProfile(
            profile = updatedProfile,
            onSuccess = { profile ->
                showLoading(false)
                userProfile = profile
                updateProfileUI(profile)
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change Password") { dialogInterface, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(currentPassword, newPassword)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        showLoading(true)
        SettingsUtils.updatePassword(
            currentPassword = currentPassword,
            newPassword = newPassword,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateNotificationSetting(enabled: Boolean) {
        userProfile?.let { profile ->
            val updatedProfile = profile.copy(notificationEnabled = enabled)
            saveProfileSettings(updatedProfile)
        }
    }

    private fun updateDarkModeSetting(enabled: Boolean) {
        userProfile?.let { profile ->
            val updatedProfile = profile.copy(darkModeEnabled = enabled)
            saveProfileSettings(updatedProfile)
            Toast.makeText(this, "Restart app to apply theme changes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAutoSyncSetting(enabled: Boolean) {
        userProfile?.let { profile ->
            val updatedProfile = profile.copy(autoSyncEnabled = enabled)
            saveProfileSettings(updatedProfile)
        }
    }

    private fun saveProfileSettings(profile: UserProfile) {
        SettingsUtils.saveUserProfile(
            profile = profile,
            onSuccess = { updatedProfile ->
                userProfile = updatedProfile
            },
            onError = { error ->
                Toast.makeText(this, "Error saving settings: $error", Toast.LENGTH_SHORT).show()
                // Revert UI changes
                loadUserProfile()
            }
        )
    }

    private fun showPrivacyPolicy() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("Our privacy policy explains how we collect, use, and protect your personal information. We are committed to keeping your data secure and respecting your privacy.\n\nFor the full privacy policy, please visit our website.")
            .setPositiveButton("Visit Website") { dialog, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTermsOfService() {
        AlertDialog.Builder(this)
            .setTitle("Terms of Service")
            .setMessage("By using this app, you agree to our terms of service. These terms govern your use of the app and outline your rights and responsibilities.\n\nPlease read the full terms on our website.")
            .setPositiveButton("Visit Website") { dialog, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/terms"))
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showClearDataConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all your tasks, notes, calendar events, timetable entries, and documents. This action cannot be undone.")
            .setPositiveButton("Clear Data") { dialog, _ ->
                clearAllData()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun clearAllData() {
        showLoading(true)
        // TODO: Implement clear all data functionality
        Toast.makeText(this, "Clear data functionality coming soon!", Toast.LENGTH_SHORT).show()
        showLoading(false)
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This will permanently delete your account and all associated data. This action cannot be undone. Are you sure you want to proceed?")
            .setPositiveButton("Delete Account") { dialog, _ ->
                deleteAccount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAccount() {
        showLoading(true)
        SettingsUtils.deleteAccount(
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Update the logout method in your existing SettingsActivity.kt
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                // Sign out from Google if signed in with Google
                if (AuthUtils.isSignedInWithGoogle()) {
                    val googleSignInClient = AuthUtils.getGoogleSignInClient(this)
                    googleSignInClient.signOut().addOnCompleteListener {
                        AuthUtils.logout()
                        navigateToLogin()
                    }
                } else {
                    AuthUtils.logout()
                    navigateToLogin()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("English", "isiZulu", "Afrikaans")
        val languageCodes = arrayOf("en", "zu", "af")

        val currentLanguage = languageManager.getCurrentLanguage()
        val currentIndex = languageCodes.indexOf(currentLanguage)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Language")
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguageCode = languageCodes[which]
                languageManager.setAppLanguage(selectedLanguageCode)

                // Update UI immediately
                recreate()

                Toast.makeText(this, "Language changed successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }
}