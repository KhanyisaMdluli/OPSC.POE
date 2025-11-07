package com.opsc.solowork_1

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.opsc.solowork_1.adapter.NotesAdapter
import com.opsc.solowork_1.databinding.ActivityNotesBinding
import com.opsc.solowork_1.model.Note
import com.opsc.solowork_1.repository.OfflineRepository
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.LanguageManager  // Add this import
import com.opsc.solowork_1.utils.NotesUtils
import kotlinx.coroutines.launch
import java.util.*

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var offlineRepository: OfflineRepository
    private lateinit var languageManager: LanguageManager
    private var notesList = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize LanguageManager FIRST
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)

        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        offlineRepository = OfflineRepository(this)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        // Refresh language when returning to activity
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
        notesAdapter = NotesAdapter(
            notesList,
            onNoteClick = { note -> showEditNoteDialog(note) },
            onNoteDelete = { note -> deleteNote(note) }
        )

        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = notesAdapter
    }

    private fun setupClickListeners() {
        binding.btnAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun loadNotes() {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        if (userId.isNotEmpty()) {
            lifecycleScope.launch {
                try {
                    offlineRepository.getNotes(userId).collect { notes ->
                        notesList.clear()
                        notesList.addAll(notes)
                        notesAdapter.updateNotes(notesList)
                        showLoading(false)
                        updateEmptyState()
                    }
                } catch (e: Exception) {
                    showLoading(false)
                    Toast.makeText(this@NotesActivity, "Error loading notes: ${e.message}", Toast.LENGTH_LONG).show()
                    updateEmptyState()
                }
            }
        } else {
            showLoading(false)
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showAddNoteDialog() {
        showNoteDialog(null)
    }

    private fun showEditNoteDialog(note: Note) {
        showNoteDialog(note)
    }

    private fun showNoteDialog(existingNote: Note?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etContent)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        val categories = arrayOf("Personal", "Work", "Study", "Ideas", "Important")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        existingNote?.let { note ->
            etTitle.setText(note.title)
            etContent.setText(note.content)
            val categoryIndex = categories.indexOf(note.category)
            if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex)
        }

        val dialogTitle = if (existingNote == null) "Add New Note" else "Edit Note"
        val positiveButtonText = if (existingNote == null) "Add" else "Update"

        val dialog = AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(positiveButtonText) { dialogInterface, _ ->
                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                val category = spinnerCategory.selectedItem as String

                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val note = if (existingNote == null) {
                    Note(
                        title = title,
                        content = content,
                        category = category,
                        userId = AuthUtils.getCurrentUser()?.uid ?: "",
                        createdAt = Date(),
                        updatedAt = Date()
                    )
                } else {
                    existingNote.copy(
                        title = title,
                        content = content,
                        category = category,
                        updatedAt = Date()
                    )
                }

                lifecycleScope.launch {
                    if (existingNote == null) {
                        addNote(note)
                    } else {
                        updateNote(note)
                    }
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    private suspend fun addNote(note: Note) {
        showLoading(true)
        try {
            offlineRepository.saveNote(note)

            if (offlineRepository.isOnline()) {
                NotesUtils.addNote(
                    note = note,
                    onSuccess = {
                        showLoading(false)
                        Toast.makeText(this, "Note added successfully", Toast.LENGTH_SHORT).show()
                        loadNotes()
                    },
                    onError = { error ->
                        showLoading(false)
                        Toast.makeText(this, "Note saved offline: $error", Toast.LENGTH_LONG).show()
                        loadNotes()
                    }
                )
            } else {
                showLoading(false)
                Toast.makeText(this, "Note saved offline", Toast.LENGTH_SHORT).show()
                loadNotes()
            }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun updateNote(note: Note) {
        showLoading(true)
        try {
            offlineRepository.updateNote(note)

            if (offlineRepository.isOnline()) {
                NotesUtils.updateNote(
                    noteId = note.id,
                    note = note,
                    onSuccess = {
                        showLoading(false)
                        Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show()
                        loadNotes()
                    },
                    onError = { error ->
                        showLoading(false)
                        Toast.makeText(this, "Note updated offline: $error", Toast.LENGTH_LONG).show()
                        loadNotes()
                    }
                )
            } else {
                showLoading(false)
                Toast.makeText(this, "Note updated offline", Toast.LENGTH_SHORT).show()
                loadNotes()
            }
        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteNote(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete '${note.title}'?")
            .setPositiveButton("Delete") { dialog, _ ->
                lifecycleScope.launch {
                    showLoading(true)
                    try {
                        offlineRepository.deleteNote(note.id)

                        if (offlineRepository.isOnline()) {
                            NotesUtils.deleteNote(
                                noteId = note.id,
                                onSuccess = {
                                    showLoading(false)
                                    Toast.makeText(this@NotesActivity, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                                    loadNotes()
                                },
                                onError = { error ->
                                    showLoading(false)
                                    Toast.makeText(this@NotesActivity, "Note deleted offline: $error", Toast.LENGTH_LONG).show()
                                    loadNotes()
                                }
                            )
                        } else {
                            showLoading(false)
                            Toast.makeText(this@NotesActivity, "Note deleted offline", Toast.LENGTH_SHORT).show()
                            loadNotes()
                        }
                    } catch (e: Exception) {
                        showLoading(false)
                        Toast.makeText(this@NotesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
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
        val isEmpty = notesList.isEmpty()
        if (isEmpty) {
            // Show empty state
            binding.rvNotes.visibility = View.GONE
            // You might need to add a TextView for empty state in your layout
        } else {
            binding.rvNotes.visibility = View.VISIBLE
        }
    }
}