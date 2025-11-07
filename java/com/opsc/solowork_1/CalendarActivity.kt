package com.opsc.solowork_1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.opsc.solowork_1.adapter.CalendarEventsAdapter
import com.opsc.solowork_1.databinding.ActivityCalendarBinding
import com.opsc.solowork_1.model.CalendarEvent
import com.opsc.solowork_1.repository.CalendarRepository
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.CalendarUtils
import com.opsc.solowork_1.utils.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var eventsAdapter: CalendarEventsAdapter
    private var eventsList = mutableListOf<CalendarEvent>()
    private val calendar = Calendar.getInstance()
    private lateinit var languageManager: LanguageManager

    // Calendar repository for MockAPI integration
    private val calendarRepository = CalendarRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadTodayEvents()
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
        eventsAdapter = CalendarEventsAdapter(
            eventsList,
            onEventClick = { event -> showEditEventDialog(event) },
            onEventDelete = { event -> deleteEvent(event) }
        )

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = eventsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        binding.btnSync.setOnClickListener {
            syncEventsWithCalendar()
        }
    }

    private fun loadTodayEvents() {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            CalendarUtils.getEventsForDate(
                userId = userId,
                date = Date(),
                onSuccess = { events ->
                    showLoading(false)
                    eventsList.clear()
                    eventsList.addAll(events.sortedBy { it.startTime })
                    eventsAdapter.updateEvents(eventsList)
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

    private fun showAddEventDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        showEventDialog(dialogView, null)
    }

    private fun showEditEventDialog(event: CalendarEvent) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        showEventDialog(dialogView, event)
    }

    private fun showEventDialog(dialogView: View, existingEvent: CalendarEvent?) {
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etLocation = dialogView.findViewById<EditText>(R.id.etLocation)
        val spinnerEventType = dialogView.findViewById<Spinner>(R.id.spinnerEventType)
        val cbAllDay = dialogView.findViewById<CheckBox>(R.id.cbAllDay)
        val tvStartDate = dialogView.findViewById<TextView>(R.id.tvStartDate)
        val tvStartTime = dialogView.findViewById<TextView>(R.id.tvStartTime)
        val tvEndDate = dialogView.findViewById<TextView>(R.id.tvEndDate)
        val tvEndTime = dialogView.findViewById<TextView>(R.id.tvEndTime)
        val btnSetStartDate = dialogView.findViewById<Button>(R.id.btnSetStartDate)
        val btnSetStartTime = dialogView.findViewById<Button>(R.id.btnSetStartTime)
        val btnSetEndDate = dialogView.findViewById<Button>(R.id.btnSetEndDate)
        val btnSetEndTime = dialogView.findViewById<Button>(R.id.btnSetEndTime)

        // Setup event type spinner
        val eventTypes = arrayOf(
            CalendarEvent.TYPE_PERSONAL,
            CalendarEvent.TYPE_WORK,
            CalendarEvent.TYPE_STUDY,
            CalendarEvent.TYPE_MEETING
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEventType.adapter = adapter

        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        var startDate = existingEvent?.startTime ?: Date()
        var endDate = existingEvent?.endTime ?: Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }.time

        // Populate fields if editing
        existingEvent?.let { event ->
            etTitle.setText(event.title)
            etDescription.setText(event.description)
            etLocation.setText(event.location)
            cbAllDay.isChecked = event.isAllDay
            spinnerEventType.setSelection(eventTypes.indexOf(event.eventType))
        }

        // Set initial dates and times
        tvStartDate.text = dateFormatter.format(startDate)
        tvEndDate.text = dateFormatter.format(endDate)
        tvStartTime.text = timeFormatter.format(startDate)
        tvEndTime.text = timeFormatter.format(endDate)

        // Show/hide time fields based on all-day setting
        val timeVisibility = if (existingEvent?.isAllDay == true) View.GONE else View.VISIBLE
        tvStartTime.visibility = timeVisibility
        tvEndTime.visibility = timeVisibility
        btnSetStartTime.visibility = timeVisibility
        btnSetEndTime.visibility = timeVisibility

        btnSetStartDate.setOnClickListener {
            showDatePicker { year, month, day ->
                calendar.time = startDate
                calendar.set(year, month, day)
                startDate = calendar.time
                tvStartDate.text = dateFormatter.format(startDate)
            }
        }

        btnSetStartTime.setOnClickListener {
            showTimePicker { hour, minute ->
                calendar.time = startDate
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                startDate = calendar.time
                tvStartTime.text = timeFormatter.format(startDate)
            }
        }

        btnSetEndDate.setOnClickListener {
            showDatePicker { year, month, day ->
                calendar.time = endDate
                calendar.set(year, month, day)
                endDate = calendar.time
                tvEndDate.text = dateFormatter.format(endDate)
            }
        }

        btnSetEndTime.setOnClickListener {
            showTimePicker { hour, minute ->
                calendar.time = endDate
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                endDate = calendar.time
                tvEndTime.text = timeFormatter.format(endDate)
            }
        }

        cbAllDay.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if (isChecked) View.GONE else View.VISIBLE
            tvStartTime.visibility = visibility
            tvEndTime.visibility = visibility
            btnSetStartTime.visibility = visibility
            btnSetEndTime.visibility = visibility
        }

        val dialogTitle = if (existingEvent == null) "Add New Event" else "Edit Event"
        val positiveButtonText = if (existingEvent == null) "Add" else "Update"

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(positiveButtonText) { dialogInterface, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val location = etLocation.text.toString().trim()
                val eventType = spinnerEventType.selectedItem as String
                val isAllDay = cbAllDay.isChecked

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val event = if (existingEvent == null) {
                    CalendarEvent(
                        title = title,
                        description = description,
                        startTime = startDate,
                        endTime = endDate,
                        eventType = eventType,
                        location = location,
                        isAllDay = isAllDay,
                        userId = AuthUtils.getCurrentUser()?.uid ?: ""
                    )
                } else {
                    existingEvent.copy(
                        title = title,
                        description = description,
                        startTime = startDate,
                        endTime = endDate,
                        eventType = eventType,
                        location = location,
                        isAllDay = isAllDay
                    )
                }

                if (existingEvent == null) {
                    addEvent(event)
                } else {
                    updateEvent(event)
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            onDateSet(selectedYear, selectedMonth, selectedDay)
        }, year, month, day).show()
    }

    private fun showTimePicker(onTimeSet: (Int, Int) -> Unit) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            onTimeSet(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

    private fun addEvent(event: CalendarEvent) {
        showLoading(true)
        CalendarUtils.addEvent(
            event = event,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                loadTodayEvents()

                // Sync with MockAPI
                CoroutineScope(Dispatchers.Main).launch {
                    val success = calendarRepository.addEventToCalendar(event)
                    if (success) {
                        Toast.makeText(this@CalendarActivity, "Event synced with cloud", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CalendarActivity, "Failed to sync with cloud", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateEvent(event: CalendarEvent) {
        showLoading(true)
        CalendarUtils.updateEvent(
            eventId = event.id,
            event = event,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show()
                loadTodayEvents()

                // Sync with MockAPI
                CoroutineScope(Dispatchers.Main).launch {
                    val success = calendarRepository.updateEventInCalendar(event.id, event)
                    if (success) {
                        Toast.makeText(this@CalendarActivity, "Event updated in cloud", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun deleteEvent(event: CalendarEvent) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event?")
            .setPositiveButton("Delete") { dialog, _ ->
                showLoading(true)
                CalendarUtils.deleteEvent(
                    eventId = event.id,
                    onSuccess = {
                        showLoading(false)
                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show()
                        loadTodayEvents()

                        // Sync with MockAPI
                        CoroutineScope(Dispatchers.Main).launch {
                            val success = calendarRepository.deleteEventFromCalendar(event.id)
                            if (success) {
                                Toast.makeText(this@CalendarActivity, "Event deleted from cloud", Toast.LENGTH_SHORT).show()
                            }
                        }
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

    private fun syncEventsWithCalendar() {
        showLoading(true)
        CoroutineScope(Dispatchers.Main).launch {
            val success = calendarRepository.syncEventsToCalendar(eventsList)
            showLoading(false)
            if (success) {
                Toast.makeText(this@CalendarActivity, "All events synced with cloud!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@CalendarActivity, "Failed to sync events with cloud", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState() {
        val isEmpty = eventsList.isEmpty()
        binding.tvNoEvents.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvEvents.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}