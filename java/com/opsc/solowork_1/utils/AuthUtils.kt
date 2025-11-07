package com.opsc.solowork_1.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase

object AuthUtils {

    private val auth: FirebaseAuth = Firebase.auth

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get current user
     */
    fun getCurrentUser() = auth.currentUser

    /**
     * Login with email and password
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Login failed")
                }
            }
    }

    /**
     * Register new user
     */
    fun registerUser(
        email: String,
        password: String,
        fullName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update user profile with display name
                    val user = auth.currentUser
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                onSuccess()
                            } else {
                                onError(profileTask.exception?.message ?: "Profile update failed")
                            }
                        }
                } else {
                    onError(task.exception?.message ?: "Registration failed")
                }
            }
    }

    /**
     * Login with Google credentials
     */
    fun loginWithGoogle(
        credential: com.google.firebase.auth.AuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Google sign in failed")
                }
            }
    }

    /**
     * Check if user is signed in with Google
     */
    fun isSignedInWithGoogle(): Boolean {
        val user = auth.currentUser
        return user?.let {
            for (profile in it.providerData) {
                if (profile.providerId == GoogleAuthProvider.PROVIDER_ID) {
                    return true
                }
            }
            false
        } ?: false
    }

    /**
     * Get Google Sign-In client
     */
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("128165556877-e8q1u3qbr7moeef3u0m0cejgbum7ges0.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Logout user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Failed to send reset email")
                }
            }
    }
}