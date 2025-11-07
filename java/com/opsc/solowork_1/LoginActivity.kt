package com.opsc.solowork_1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.material.textfield.TextInputEditText
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager
import java.io.File

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleSignIn: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var languageManager: LanguageManager
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupGoogleSignIn()
        setupClickListeners()

        // Check if user is already logged in
        if (AuthUtils.isUserLoggedIn()) {
            navigateToDashboard()
        }
        testGoogleSignInSetup()
    }

    override fun onResume() {
        super.onResume()
        languageManager.applySavedLanguage(this)
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        tvRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun signInWithGoogle() {
        showLoading(true)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed
                showLoading(false)
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Add detailed logging
        println("GOOGLE_SIGN_IN_DEBUG: Starting Firebase authentication")
        println("GOOGLE_SIGN_IN_DEBUG: ID Token received: ${idToken.take(20)}...")

        AuthUtils.loginWithGoogle(
            credential = credential,
            onSuccess = {
                showLoading(false)
                println("GOOGLE_SIGN_IN_DEBUG: SUCCESS - User authenticated")
                navigateToDashboard()
                Toast.makeText(this, "Google sign in successful!", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                showLoading(false)
                println("GOOGLE_SIGN_IN_DEBUG: ERROR - $error")

                // More specific error handling
                when {
                    error.contains("12500") -> {
                        println("GOOGLE_SIGN_IN_DEBUG: Error 12500 - Configuration issue")
                        checkFirebaseConfiguration()
                    }
                    error.contains("12501") -> {
                        println("GOOGLE_SIGN_IN_DEBUG: Error 12501 - User cancelled")
                    }
                    error.contains("10") -> {
                        println("GOOGLE_SIGN_IN_DEBUG: Error 10 - Configuration error")
                    }
                    else -> {
                        println("GOOGLE_SIGN_IN_DEBUG: Unknown error: $error")
                    }
                }

                Toast.makeText(this, "Authentication failed: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun checkFirebaseConfiguration() {
        println("GOOGLE_SIGN_IN_DEBUG: Checking Firebase configuration...")

        // Check if google-services.json exists in the correct location
        try {
            val file = File("app/google-services.json")
            if (file.exists()) {
                println("GOOGLE_SIGN_IN_DEBUG: google-services.json exists in app/ folder")
            } else {
                println("GOOGLE_SIGN_IN_DEBUG: google-services.json NOT FOUND in app/ folder")
            }
        } catch (e: Exception) {
            println("GOOGLE_SIGN_IN_DEBUG: Error checking google-services.json: ${e.message}")
        }

        // Check current user (should be null before login)
        val currentUser = AuthUtils.getCurrentUser()
        println("GOOGLE_SIGN_IN_DEBUG: Current user: $currentUser")

        // Check package name
        println("GOOGLE_SIGN_IN_DEBUG: Package name: $packageName")

        // Check Web Client ID being used
        println("GOOGLE_SIGN_IN_DEBUG: Web Client ID: 128165556877-e8q1u3qbr7moeef3u0m0cejgbum7ges0.apps.googleusercontent.com")
    }

    private fun testGoogleSignInSetup() {
        println("=== GOOGLE SIGN-IN SETUP TEST ===")
        println("1. Package Name: $packageName")
        println("2. Web Client ID: 128165556877-e8q1u3qbr7moeef3u0m0cejgbum7ges0.apps.googleusercontent.com")
        println("3. SHA-1: 86:60:EB:4D:B4:46:16:B0:B4:CC:AF:66:EA:13:0D:25:BC:BE:3F:75")

        // Test if we can create Google Sign-In client
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("128165556877-e8q1u3qbr7moeef3u0m0cejgbum7ges0.apps.googleusercontent.com")
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(this, gso)
            println("4. Google Sign-In Client: CREATED SUCCESSFULLY")
        } catch (e: Exception) {
            println("4. Google Sign-In Client: FAILED - ${e.message}")
        }

        println("=== END TEST ===")
    }
    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(email, password)) {
            showLoading(true)
            AuthUtils.loginUser(
                email = email,
                password = password,
                onSuccess = {
                    showLoading(false)
                    navigateToDashboard()
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, "Login failed: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
        btnGoogleSignIn.isEnabled = !show
    }
}