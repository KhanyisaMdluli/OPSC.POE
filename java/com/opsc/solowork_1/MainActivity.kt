package com.opsc.solowork_1

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.opsc.solowork_1.databinding.ActivityMainBinding
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigation()
        setupDashboard()
        checkAuthentication()
    }

    override fun onResume() {
        super.onResume()
        languageManager.applySavedLanguage(this)
    }

    private fun setupToolbar() {
        // Remove setSupportActionBar call - the toolbar is already in XML
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigation() {
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Update navigation header with user info
        val headerView = binding.navigationView.getHeaderView(0)
        val tvNavUserName = headerView.findViewById<TextView>(R.id.tvNavUserName)
        val tvNavUserEmail = headerView.findViewById<TextView>(R.id.tvNavUserEmail)

        val user = AuthUtils.getCurrentUser()
        tvNavUserName.text = user?.displayName ?: "User"
        tvNavUserEmail.text = user?.email ?: "user@email.com"
    }

    private fun setupDashboard() {
        // Update main welcome section
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)

        val user = AuthUtils.getCurrentUser()
        val welcomeText = "Welcome, ${user?.displayName ?: "User"}!"
        tvWelcome.text = welcomeText
        tvUserEmail.text = user?.email ?: "user@email.com"

        // Set up click listeners for feature cards
        findViewById<android.view.View>(R.id.cardTasks).setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardFocus).setOnClickListener {
            startActivity(Intent(this, FocusModeActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarUIActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardTimetable).setOnClickListener {
            startActivity(Intent(this, TimetableActivity::class.java))
        }

        findViewById<android.view.View>(R.id.cardDocuments).setOnClickListener {
            startActivity(Intent(this, DocumentsActivity::class.java))
        }
    }

    private fun checkAuthentication() {
        if (!AuthUtils.isUserLoggedIn()) {
            navigateToLogin()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // Already on dashboard
            }
            R.id.nav_tasks -> {
                startActivity(Intent(this, TasksActivity::class.java))
            }
            R.id.nav_notes -> {
                startActivity(Intent(this, NotesActivity::class.java))
            }
            R.id.nav_focus -> {
                startActivity(Intent(this, FocusModeActivity::class.java))
            }
            R.id.nav_calendar -> {
                startActivity(Intent(this, CalendarActivity::class.java))
            }
            R.id.nav_timetable -> {
                startActivity(Intent(this, TimetableActivity::class.java))
            }
            R.id.nav_documents -> {
                startActivity(Intent(this, DocumentsActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_logout -> {
                logout()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        AuthUtils.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}