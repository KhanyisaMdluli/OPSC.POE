package com.opsc.solowork_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        languageManager.applySavedLanguage(this)
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            attemptRegistration()
        }

        tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun attemptRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (validateInputs(fullName, email, password)) {
            showLoading(true)
            AuthUtils.registerUser(
                email = email,
                password = password,
                fullName = fullName,
                onSuccess = {
                    showLoading(false)
                    navigateToDashboard()
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun validateInputs(fullName: String, email: String, password: String): Boolean {
        if (fullName.isEmpty()) {
            etFullName.error = "Full name is required"
            return false
        }

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

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        btnRegister.isEnabled = !show
    }
}