package com.opsc.solowork_1

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.opsc.solowork_1.adapter.TasksAdapter
import com.opsc.solowork_1.databinding.ActivityTasksBinding
import com.opsc.solowork_1.model.Task
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager
import com.opsc.solowork_1.utils.TaskUtils
import java.text.SimpleDateFormat
import java.util.*

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private lateinit var tasksAdapter: TasksAdapter
    private var tasksList = mutableListOf<Task>()
    private val calendar = Calendar.getInstance()
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadTasks()
    }

    override fun onResume() {
        super.onResume()
        languageManager.applySavedLanguage(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter(
            tasksList,
            onTaskClick = { task -> showEditTaskDialog(task) },
            onTaskDelete = { task -> deleteTask(task) },
            onTaskStatusChange = { task, isCompleted -> updateTaskStatus(task, isCompleted) }
        )

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@TasksActivity)
            adapter = tasksAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun loadTasks() {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            TaskUtils.getTasks(
                userId = userId,
                onSuccess = { tasks ->
                    showLoading(false)
                    tasksList.clear()
                    tasksList.addAll(tasks.sortedBy { it.dueDate })
                    tasksAdapter.updateTasks(tasksList)
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
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        val btnSetDate = dialogView.findViewById<Button>(R.id.btnSetDate)

        // Setup priority spinner
        val priorities = arrayOf(Task.PRIORITY_LOW, Task.PRIORITY_MEDIUM, Task.PRIORITY_HIGH)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter

        var selectedDate: Date? = null

        btnSetDate.setOnClickListener {
            showDatePicker { year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.time
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDueDate.text = dateFormat.format(selectedDate!!)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val priority = spinnerPriority.selectedItem as String

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val task = Task(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = selectedDate,
                    userId = AuthUtils.getCurrentUser()?.uid ?: ""
                )

                addTask(task)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        val btnSetDate = dialogView.findViewById<Button>(R.id.btnSetDate)

        // Populate fields
        etTitle.setText(task.title)
        etDescription.setText(task.description)

        // Set priority
        val priorities = arrayOf(Task.PRIORITY_LOW, Task.PRIORITY_MEDIUM, Task.PRIORITY_HIGH)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter
        spinnerPriority.setSelection(priorities.indexOf(task.priority))

        // Set due date
        var selectedDate = task.dueDate
        selectedDate?.let {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvDueDate.text = dateFormat.format(it)
            calendar.time = it
        }

        btnSetDate.setOnClickListener {
            showDatePicker { year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.time
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDueDate.text = dateFormat.format(selectedDate!!)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { dialogInterface, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val priority = spinnerPriority.selectedItem as String

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedTask = task.copy(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = selectedDate
                )

                updateTask(updatedTask)
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

    private fun addTask(task: Task) {
        showLoading(true)
        TaskUtils.addTask(
            task = task,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show()
                loadTasks()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateTask(task: Task) {
        showLoading(true)
        TaskUtils.updateTask(
            taskId = task.id,
            task = task,
            onSuccess = {
                showLoading(false)
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                loadTasks()
            },
            onError = { error ->
                showLoading(false)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateTaskStatus(task: Task, isCompleted: Boolean) {
        val updatedTask = task.copy(isCompleted = isCompleted)
        TaskUtils.updateTask(
            taskId = task.id,
            task = updatedTask,
            onSuccess = {
                // Status updated successfully
                loadTasks() // Reload to refresh the list
            },
            onError = { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                loadTasks() // Reload to revert the checkbox state
            }
        )
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { dialog, _ ->
                showLoading(true)
                TaskUtils.deleteTask(
                    taskId = task.id,
                    onSuccess = {
                        showLoading(false)
                        Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                        loadTasks()
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

    private fun showFilterDialog() {
        val filterOptions = arrayOf("All", "Pending", "Completed", "High Priority")

        AlertDialog.Builder(this)
            .setTitle("Filter Tasks")
            .setItems(filterOptions) { dialog, which ->
                when (which) {
                    0 -> loadTasks() // All
                    1 -> filterTasks(false) // Pending
                    2 -> filterTasks(true) // Completed
                    3 -> filterByPriority(Task.PRIORITY_HIGH) // High Priority
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun filterTasks(completed: Boolean) {
        val filteredTasks = tasksList.filter { it.isCompleted == completed }
        tasksAdapter.updateTasks(filteredTasks)
        updateEmptyState(filteredTasks.isEmpty())
    }

    private fun filterByPriority(priority: String) {
        val filteredTasks = tasksList.filter { it.priority == priority }
        tasksAdapter.updateTasks(filteredTasks)
        updateEmptyState(filteredTasks.isEmpty())
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateEmptyState(isEmpty: Boolean = tasksList.isEmpty()) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTasks.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}