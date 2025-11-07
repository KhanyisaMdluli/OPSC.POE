package com.opsc.solowork_1

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.opsc.solowork_1.adapter.TimetableAdapter
import com.opsc.solowork_1.databinding.ActivityTimetableBinding
import com.opsc.solowork_1.model.TimetableEntry
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager
import com.opsc.solowork_1.utils.TimetableUtils
import java.util.*

class TimetableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimetableBinding
    private lateinit var timetableAdapter: TimetableAdapter
    private var timetableEntries = mutableListOf<TimetableEntry>()
    private var currentSelectedDay = getCurrentDay()
    private val calendar = Calendar.getInstance()
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupDaySelector()
        setupClickListeners()
        loadTimetableForDay(currentSelectedDay)
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

    private fun setupRecyclerView() {
        timetableAdapter = TimetableAdapter(
            timetableEntries,
            onEntryClick = { entry -> showEditEntryDialog(entry) },
            onEntryDelete = { entry -> deleteEntry(entry) }
        )

        binding.rvTimetable.apply {
            layoutManager = LinearLayoutManager(this@TimetableActivity)
            adapter = timetableAdapter
        }
    }

    private fun setupDaySelector() {
        val daySelector = binding.daySelector
        daySelector.removeAllViews()

        TimetableEntry.DAYS_OF_WEEK.forEach { day ->
            val dayButton = MaterialButton(this).apply {
                text = day.take(3) // Show abbreviated day names
                setOnClickListener {
                    selectDay(day)
                }
                isCheckable = true
                cornerRadius = 20.dpToPx()
                setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            daySelector.addView(dayButton)

            // Select current day by default
            if (day == currentSelectedDay) {
                dayButton.isChecked = true
                dayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color))
                dayButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun setupClickListeners() {
        binding.btnAddEntry.setOnClickListener {
            showAddEntryDialog()
        }
    }

    private fun selectDay(day: String) {
        currentSelectedDay = day
        binding.tvCurrentDay.text = "$day's Schedule"

        // Update day selector buttons
        for (i in 0 until binding.daySelector.childCount) {
            val button = binding.daySelector.getChildAt(i) as MaterialButton
            val buttonDay = TimetableEntry.DAYS_OF_WEEK[i]

            if (buttonDay == day) {
                button.isChecked = true
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color))
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                button.isChecked = false
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_grey))
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        loadTimetableForDay(day)
    }

    private fun loadTimetableForDay(day: String) {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            TimetableUtils.getTimetableEntriesForDay(
                userId = userId,
                dayOfWeek = day,
                onSuccess = { entries ->
                    showLoading(false)
                    timetableEntries.clear()
                    timetableEntries.addAll(entries)
                    timetableAdapter.updateEntries(timetableEntries)
                    updateEmptyState()
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    updateEmptyState()
                }
            )
        } else {
            showLoading(false)
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showAddEntryDialog() {
        showEntryDialog(null)
    }

    private fun showEditEntryDialog(entry: TimetableEntry) {
        showEntryDialog(entry)
    }

    private fun showEntryDialog(existingEntry: TimetableEntry?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_timetable, null)
        val etCourseName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val etCourseCode = dialogView.findViewById<EditText>(R.id.etCourseCode)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val etLocation = dialogView.findViewById<EditText>(R.id.etLocation)
        val etInstructor = dialogView.findViewById<EditText>(R.id.etInstructor)
        val tvStartTime = dialogView.findViewById<TextView>(R.id.tvStartTime)
        val tvEndTime = dialogView.findViewById<TextView>(R.id.tvEndTime)
        val btnSetStartTime = dialogView.findViewById<Button>(R.id.btnSetStartTime)
        val btnSetEndTime = dialogView.findViewById<Button>(R.id.btnSetEndTime)

        // Populate fields if editing
        existingEntry?.let { entry ->
            etCourseName.setText(entry.courseName)
            etCourseCode.setText(entry.courseCode)
            etLocation.setText(entry.location)
            etInstructor.setText(entry.instructor)
            tvStartTime.text = entry.startTime
            tvEndTime.text = entry.endTime
        }

        // Setup day spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, TimetableEntry.DAYS_OF_WEEK)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = adapter
        spinnerDay.setSelection(TimetableEntry.DAYS_OF_WEEK.indexOf(existingEntry?.dayOfWeek ?: currentSelectedDay))

        var startTime = existingEntry?.startTime ?: "09:00"
        var endTime = existingEntry?.endTime ?: "10:00"

        // Set initial times if not editing
        if (existingEntry == null) {
            tvStartTime.text = startTime
            tvEndTime.text = endTime
        }

        btnSetStartTime.setOnClickListener {
            showTimePicker { hour, minute ->
                startTime = String.format("%02d:%02d", hour, minute)
                tvStartTime.text = startTime
            }
        }

        btnSetEndTime.setOnClickListener {
            showTimePicker { hour, minute ->
                endTime = String.format("%02d:%02d", hour, minute)
                tvEndTime.text = endTime
            }
        }

        val dialogTitle = if (existingEntry == null) "Add Class" else "Edit Class"
        val positiveButtonText = if (existingEntry == null) "Add" else "Update"

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(positiveButtonText) { dialogInterface, _ ->
                val courseName = etCourseName.text.toString().trim()
                val courseCode = etCourseCode.text.toString().trim()
                val dayOfWeek = spinnerDay.selectedItem as String
                val location = etLocation.text.toString().trim()
                val instructor = etInstructor.text.toString().trim()

                if (courseName.isEmpty()) {
                    Toast.makeText(this, "Please enter course name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (courseCode.isEmpty()) {
                    Toast.makeText(this, "Please enter course code", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Assign a random color from default colors
                val randomColor = TimetableEntry.DEFAULT_COLORS.random()

                val entry = if (existingEntry == null) {
                    TimetableEntry(
                        courseName = courseName,
                        courseCode = courseCode,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        location = location,
                        instructor = instructor,
                        color = randomColor,
                        userId = AuthUtils.getCurrentUser()?.uid ?: ""
                    )
                } else {
                    existingEntry.copy(
                        courseName = courseName,
                        courseCode = courseCode,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        location = location,
                        instructor = instructor
                    )
                }

                if (existingEntry == null) {
                    addTimetableEntry(entry)
                } else {
                    updateTimetableEntry(entry)
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            onTimeSet(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun addTimetableEntry(entry: TimetableEntry) {
        showLoading(true)
        TimetableUtils.addTimetableEntry(
            entry = entry,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Class added successfully", Toast.LENGTH_SHORT).show()
                loadTimetableForDay(entry.dayOfWeek)
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateTimetableEntry(entry: TimetableEntry) {
        showLoading(true)
        TimetableUtils.updateTimetableEntry(
            entryId = entry.id,
            entry = entry,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Class updated successfully", Toast.LENGTH_SHORT).show()
                loadTimetableForDay(entry.dayOfWeek)
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun deleteEntry(entry: TimetableEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Class")
            .setMessage("Are you sure you want to delete this class?")
            .setPositiveButton("Delete") { dialog, _ ->
                showLoading(true)
                TimetableUtils.deleteTimetableEntry(
                    entryId = entry.id,
                    onSuccess = {
                        showLoading(false)
                        Toast.makeText(this, "Class deleted successfully", Toast.LENGTH_SHORT).show()
                        loadTimetableForDay(currentSelectedDay)
                    },
                    onError = { error ->
                        showLoading(false)
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState() {
        val isEmpty = timetableEntries.isEmpty()
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTimetable.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    companion object {
        fun getCurrentDay(): String {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_WEEK)
            return when (day) {
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                Calendar.SUNDAY -> "Sunday"
                else -> "Monday"
            }
        }
    }
}