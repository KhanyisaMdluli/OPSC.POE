package com.opsc.solowork_1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.adapter.CalendarDayAdapter
import com.opsc.solowork_1.adapter.CalendarEventsAdapter
import com.opsc.solowork_1.databinding.ActivityCalendarUiBinding
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

class CalendarUIActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarUiBinding
    private lateinit var eventsAdapter: CalendarEventsAdapter
    private lateinit var calendarDayAdapter: CalendarDayAdapter
    private var eventsList = mutableListOf<CalendarEvent>()
    private var allEvents = mutableListOf<CalendarEvent>()
    private val calendar = Calendar.getInstance()
    private val calendarRepository = CalendarRepository()
    private var selectedDate = Date()
    private var currentMonth = Calendar.getInstance()
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCalendar()
        setupRecyclerView()
        setupClickListeners()
        loadAllEvents()
        updateCalendar()
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

    private fun setupCalendar() {
        // FIXED: Use proper RecyclerView layout manager assignment
        val layoutManager = GridLayoutManager(this, 7)
        binding.calendarGrid.layoutManager = layoutManager
        updateMonthYearHeader()
    }

    private fun setupRecyclerView() {
        eventsAdapter = CalendarEventsAdapter(
            eventsList,
            onEventClick = { event -> showEditEventDialog(event) },
            onEventDelete = { event -> deleteEvent(event) }
        )

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@CalendarUIActivity)
            adapter = eventsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnToday.setOnClickListener {
            selectToday()
        }

        binding.btnAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        binding.btnPrevMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.btnNextMonth.setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    private fun updateCalendar() {
        updateMonthYearHeader()
        generateCalendarDays()
        loadEventsForDate(selectedDate)
    }

    private fun updateMonthYearHeader() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = monthFormat.format(currentMonth.time)
    }

    private fun generateCalendarDays() {
        val calendarDays = mutableListOf<CalendarDayAdapter.CalendarDay>()

        // Set calendar to first day of the month
        val firstDayOfMonth = currentMonth.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        // Get the day of week for the first day (1=Sunday, 7=Saturday)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)

        // Calculate how many days from previous month to show
        val daysFromPrevMonth = firstDayOfWeek - 1 // Sunday = 1, so subtract 1 to get empty days

        // Add empty days from previous month
        val prevMonth = currentMonth.clone() as Calendar
        prevMonth.add(Calendar.MONTH, -1)
        val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in daysFromPrevMonth - 1 downTo 0) {
            val day = daysInPrevMonth - i
            val date = Calendar.getInstance().apply {
                set(prevMonth.get(Calendar.YEAR), prevMonth.get(Calendar.MONTH), day)
            }
            calendarDays.add(CalendarDayAdapter.CalendarDay(
                day = day,
                isCurrentMonth = false,
                date = date.time
            ))
        }

        // Add days of current month
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance()

        for (day in 1..daysInMonth) {
            val date = Calendar.getInstance().apply {
                set(currentMonth.get(Calendar.YEAR), currentMonth.get(Calendar.MONTH), day)
            }

            // Check if this day is today
            val isToday = date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            // Check if this day is selected
            val selectedCal = Calendar.getInstance().apply { time = selectedDate }
            val isSelected = date.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH) &&
                    date.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)

            // Find events for this specific day
            val dayEvents = allEvents.filter { event ->
                val eventCal = Calendar.getInstance().apply { time = event.startTime }
                eventCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                        eventCal.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                        eventCal.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
            }

            calendarDays.add(CalendarDayAdapter.CalendarDay(
                day = day,
                isCurrentMonth = true,
                isToday = isToday,
                isSelected = isSelected,
                events = dayEvents,
                date = date.time
            ))
        }

        // Add empty days for next month to complete the grid (6 rows × 7 columns = 42 cells)
        val totalCells = 42
        val remainingCells = totalCells - calendarDays.size
        val nextMonth = currentMonth.clone() as Calendar
        nextMonth.add(Calendar.MONTH, 1)

        for (day in 1..remainingCells) {
            val date = Calendar.getInstance().apply {
                set(nextMonth.get(Calendar.YEAR), nextMonth.get(Calendar.MONTH), day)
            }
            calendarDays.add(CalendarDayAdapter.CalendarDay(
                day = day,
                isCurrentMonth = false,
                date = date.time
            ))
        }

        // Create and set the adapter
        calendarDayAdapter = CalendarDayAdapter(calendarDays) { day ->
            if (day.isCurrentMonth) {
                selectedDate = day.date
                updateCalendar() // This will regenerate the calendar with new selection
                loadEventsForDate(selectedDate)
            }
        }

        // FIXED: Set the adapter directly to RecyclerView
        binding.calendarGrid.apply {
            adapter = calendarDayAdapter
        }
    }

    private fun loadAllEvents() {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            CalendarUtils.getEvents(
                userId = userId,
                onSuccess = { events ->
                    showLoading(false)
                    allEvents.clear()
                    allEvents.addAll(events)
                    updateCalendar() // Refresh calendar to show event dots
                },
                onError = { error ->
                    showLoading(false)
                    Toast.makeText(this, "Error loading events: $error", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            showLoading(false)
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadEventsForDate(date: Date) {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            CalendarUtils.getEventsForDate(
                userId = userId,
                date = date,
                onSuccess = { events ->
                    showLoading(false)
                    eventsList.clear()
                    eventsList.addAll(events.sortedBy { it.startTime })
                    eventsAdapter.updateEvents(eventsList)
                    updateSelectedDateHeader()
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

    private fun updateSelectedDateHeader() {
        val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance().apply { time = selectedDate }

        val dateText = when {
            selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selected.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    selected.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) -> {
                "Today's Events"
            }
            selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selected.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    selected.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) + 1 -> {
                "Tomorrow's Events"
            }
            else -> {
                "Events for ${dateFormatter.format(selectedDate)}"
            }
        }

        binding.tvSelectedDate.text = dateText
    }

    private fun selectToday() {
        selectedDate = Date()
        currentMonth = Calendar.getInstance() // Reset to current month
        updateCalendar()
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

        var startDate = existingEvent?.startTime ?: selectedDate
        var endDate = existingEvent?.endTime ?: Calendar.getInstance().apply {
            time = selectedDate
            add(Calendar.HOUR_OF_DAY, 1)
        }.time

        // Populate fields if editing
        existingEvent?.let { event ->
            etTitle.setText(event.title)
            etDescription.setText(event.description)
            etLocation.setText(event.location)
            cbAllDay.isChecked = event.isAllDay
            val typeIndex = eventTypes.indexOf(event.eventType)
            if (typeIndex >= 0) spinnerEventType.setSelection(typeIndex)
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
                // Reload all events to update calendar dots
                loadAllEvents()

                // Sync with MockAPI
                CoroutineScope(Dispatchers.Main).launch {
                    val success = calendarRepository.addEventToCalendar(event)
                    if (success) {
                        Toast.makeText(this@CalendarUIActivity, "Event synced with cloud", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CalendarUIActivity, "Failed to sync with cloud", Toast.LENGTH_SHORT).show()
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
                // Reload all events to update calendar dots
                loadAllEvents()

                // Sync with MockAPI
                CoroutineScope(Dispatchers.Main).launch {
                    val success = calendarRepository.updateEventInCalendar(event.id, event)
                    if (success) {
                        Toast.makeText(this@CalendarUIActivity, "Event updated in cloud", Toast.LENGTH_SHORT).show()
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
                        // Reload all events to update calendar dots
                        loadAllEvents()

                        // Sync with MockAPI
                        CoroutineScope(Dispatchers.Main).launch {
                            val success = calendarRepository.deleteEventFromCalendar(event.id)
                            if (success) {
                                Toast.makeText(this@CalendarUIActivity, "Event deleted from cloud", Toast.LENGTH_SHORT).show()
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

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState() {
        val isEmpty = eventsList.isEmpty()
        binding.tvNoEvents.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvEvents.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}