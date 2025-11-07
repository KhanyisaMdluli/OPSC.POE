package com.opsc.solowork_1

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.opsc.solowork_1.adapter.DocumentsAdapter
import com.opsc.solowork_1.databinding.ActivityDocumentsBinding
import com.opsc.solowork_1.model.Document
import com.opsc.solowork_1.utils.AuthUtils
import com.opsc.solowork_1.utils.DocumentUtils
import com.opsc.solowork_1.utils.LanguageManager
import java.text.SimpleDateFormat
import java.util.*

class DocumentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentsBinding
    private lateinit var documentsAdapter: DocumentsAdapter
    private var documentsList = mutableListOf<Document>()
    private var currentCategory = "All"
    private lateinit var languageManager: LanguageManager

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d(TAG, "File selected - URI: $uri")
                onFileSelected(uri)
            }
        } else {
            Log.d(TAG, "File picker cancelled or failed")
        }
    }

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var currentUploadDialog: AlertDialog? = null
    private var currentTvSelectedFile: TextView? = null

    companion object {
        private const val TAG = "DocumentsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        languageManager = LanguageManager(this)
        languageManager.applySavedLanguage(this)
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupCategoryFilter()
        setupClickListeners()
        loadDocuments()
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
        documentsAdapter = DocumentsAdapter(
            documentsList,
            onDocumentClick = { document -> showDocumentDetails(document) },
            onDocumentDownload = { document -> downloadDocument(document) },
            onDocumentDelete = { document -> deleteDocument(document) }
        )

        binding.rvDocuments.apply {
            layoutManager = LinearLayoutManager(this@DocumentsActivity)
            adapter = documentsAdapter
        }
    }

    private fun setupCategoryFilter() {
        val categoryFilter = binding.categoryFilter
        categoryFilter.removeAllViews()

        val categories = arrayOf("All", "General", "Study", "Work", "Personal", "Important")

        categories.forEach { category ->
            val categoryButton = MaterialButton(this).apply {
                text = category
                setOnClickListener {
                    filterByCategory(category)
                }
                isCheckable = true
                cornerRadius = 20.dpToPx()
                setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            categoryFilter.addView(categoryButton)

            // Select "All" by default
            if (category == "All") {
                categoryButton.isChecked = true
                categoryButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color))
                categoryButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun setupClickListeners() {
        binding.btnUpload.setOnClickListener {
            Log.d(TAG, "Upload button clicked")
            showUploadDialog()
        }
    }

    private fun filterByCategory(category: String) {
        currentCategory = category

        // Update category filter buttons
        for (i in 0 until binding.categoryFilter.childCount) {
            val button = binding.categoryFilter.getChildAt(i) as MaterialButton
            val buttonCategory = when (i) {
                0 -> "All"
                1 -> "General"
                2 -> "Study"
                3 -> "Work"
                4 -> "Personal"
                5 -> "Important"
                else -> "All"
            }

            if (buttonCategory == category) {
                button.isChecked = true
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color))
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                button.isChecked = false
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_grey))
                button.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        if (category == "All") {
            loadDocuments()
        } else {
            loadDocumentsByCategory(category)
        }
    }

    private fun loadDocuments() {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        Log.d(TAG, "Loading documents for user: $userId")

        if (userId.isNotEmpty()) {
            DocumentUtils.getDocuments(
                userId = userId,
                onSuccess = { documents ->
                    showLoading(false)
                    Log.d(TAG, "Documents loaded successfully: ${documents.size} documents")
                    documentsList.clear()
                    documentsList.addAll(documents.sortedByDescending { it.uploadDate })
                    documentsAdapter.updateDocuments(documentsList)
                    updateStorageInfo()
                    updateEmptyState()
                },
                onError = { error ->
                    showLoading(false)
                    Log.e(TAG, "Error loading documents: $error")
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    updateEmptyState()
                }
            )
        } else {
            showLoading(false)
            Log.e(TAG, "User not authenticated")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadDocumentsByCategory(category: String) {
        showLoading(true)
        val userId = AuthUtils.getCurrentUser()?.uid ?: ""
        Log.d(TAG, "Loading documents for category: $category, user: $userId")

        if (userId.isNotEmpty()) {
            DocumentUtils.getDocumentsByCategory(
                userId = userId,
                category = category,
                onSuccess = { documents ->
                    showLoading(false)
                    Log.d(TAG, "Category documents loaded successfully: ${documents.size} documents")
                    documentsList.clear()
                    documentsList.addAll(documents.sortedByDescending { it.uploadDate })
                    documentsAdapter.updateDocuments(documentsList)
                    updateEmptyState()
                },
                onError = { error ->
                    showLoading(false)
                    Log.e(TAG, "Error loading category documents: $error")
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    updateEmptyState()
                }
            )
        } else {
            showLoading(false)
            Log.e(TAG, "User not authenticated")
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showUploadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_document, null)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSelectFile = dialogView.findViewById<Button>(R.id.btnSelectFile)
        val tvSelectedFile = dialogView.findViewById<TextView>(R.id.tvSelectedFile)

        // Store reference to the current dialog's TextView
        currentTvSelectedFile = tvSelectedFile

        // Setup category spinner
        val categories = arrayOf(
            Document.CATEGORY_GENERAL,
            Document.CATEGORY_STUDY,
            Document.CATEGORY_WORK,
            Document.CATEGORY_PERSONAL,
            Document.CATEGORY_IMPORTANT
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Reset file selection for new dialog
        selectedFileUri = null
        selectedFileName = null
        tvSelectedFile.text = "No file selected"
        tvSelectedFile.setTextColor(ContextCompat.getColor(this, R.color.grey))

        btnSelectFile.setOnClickListener {
            Log.d(TAG, "Select file button clicked")
            // Launch file picker using the launcher
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // All file types
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            // Optionally restrict file types
            val mimeTypes = arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain",
                "image/*",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/zip"
            )
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

            filePickerLauncher.launch(Intent.createChooser(intent, "Select File"))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Upload Document")
            .setView(dialogView)
            .setPositiveButton("Upload") { dialogInterface, _ ->
                val description = etDescription.text.toString().trim()
                val category = spinnerCategory.selectedItem as String

                if (selectedFileUri == null) {
                    Log.w(TAG, "Upload attempted without file selection")
                    Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Get user ID
                val userId = AuthUtils.getCurrentUser()?.uid ?: ""
                if (userId.isEmpty()) {
                    Log.e(TAG, "Upload attempted without user authentication")
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                Log.d(TAG, "Starting upload process...")
                Log.d(TAG, "File URI: $selectedFileUri")
                Log.d(TAG, "File Name: $selectedFileName")
                Log.d(TAG, "User ID: $userId")
                Log.d(TAG, "Category: $category")
                Log.d(TAG, "Description: $description")

                // Show progress dialog
                val progressDialog = AlertDialog.Builder(this)
                    .setTitle("Uploading...")
                    .setView(R.layout.activity_dialog_upload_progress)
                    .setCancelable(false)
                    .create()
                progressDialog.show()

                val progressBar = progressDialog.findViewById<ProgressBar>(R.id.progressBar)
                val tvProgress = progressDialog.findViewById<TextView>(R.id.tvProgress)

                // Upload document using local storage
                DocumentUtils.uploadDocument(
                    context = this,
                    fileUri = selectedFileUri!!,
                    fileName = selectedFileName!!,
                    category = category,
                    description = description,
                    userId = userId,
                    onProgress = { progress ->
                        runOnUiThread {
                            progressBar?.progress = progress.toInt()
                            tvProgress?.text = "${progress.toInt()}%"
                            Log.d(TAG, "Upload progress: $progress%")
                        }
                    },
                    onSuccess = { document ->
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.d(TAG, "Upload completed successfully! Document ID: ${document.id}")
                            Toast.makeText(this, "Document uploaded successfully!", Toast.LENGTH_SHORT).show()
                            loadDocuments() // Refresh the list
                            dialogInterface.dismiss()

                            // Reset selection
                            selectedFileUri = null
                            selectedFileName = null
                        }
                    },
                    onError = { error ->
                        runOnUiThread {
                            progressDialog.dismiss()
                            Log.e(TAG, "Upload failed: $error")
                            Toast.makeText(this, "Upload failed: $error", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                Log.d(TAG, "Upload dialog cancelled")
                dialogInterface.dismiss()
                // Clear references when dialog is dismissed
                currentUploadDialog = null
                currentTvSelectedFile = null
            }
            .create()

        currentUploadDialog = dialog
        dialog.show()
        Log.d(TAG, "Upload dialog shown")
    }

    private fun onFileSelected(uri: Uri) {
        selectedFileUri = uri
        selectedFileName = getFileName(uri)

        Log.d(TAG, "File selected - URI: $uri, Name: $selectedFileName")

        // Update the UI in the currently shown dialog
        runOnUiThread {
            currentTvSelectedFile?.let { tvSelectedFile ->
                tvSelectedFile.text = selectedFileName
                tvSelectedFile.setTextColor(ContextCompat.getColor(this, R.color.black))
                Log.d(TAG, "Updated file selection UI with: $selectedFileName")
            } ?: run {
                Log.w(TAG, "No TextView reference found to update file selection")
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                        Log.d(TAG, "Got file name from content resolver: $result")
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
                Log.d(TAG, "Got file name from URI path: $result")
            }
        }
        return result ?: "unknown_file"
    }

    private fun showDocumentDetails(document: Document) {
        Log.d(TAG, "Showing document details: ${document.fileName}")
        val dialogView = layoutInflater.inflate(R.layout.dialog_document_details, null)
        val tvFileName = dialogView.findViewById<TextView>(R.id.tvFileName)
        val tvFileType = dialogView.findViewById<TextView>(R.id.tvFileType)
        val tvFileSize = dialogView.findViewById<TextView>(R.id.tvFileSize)
        val tvUploadDate = dialogView.findViewById<TextView>(R.id.tvUploadDate)
        val tvCategory = dialogView.findViewById<TextView>(R.id.tvCategory)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)

        tvFileName.text = document.fileName
        tvFileType.text = document.fileType.uppercase()
        tvFileSize.text = Document.formatFileSize(document.fileSize)
        tvCategory.text = document.category
        tvDescription.text = document.description

        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        tvUploadDate.text = dateFormat.format(document.uploadDate)

        AlertDialog.Builder(this)
            .setTitle("Document Details")
            .setView(dialogView)
            .setPositiveButton("Download") { dialog, _ ->
                downloadDocument(document)
                dialog.dismiss()
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun downloadDocument(document: Document) {
        Log.d(TAG, "Download initiated for: ${document.fileName}")

        // Get local file URI and open it
        val fileUri = DocumentUtils.getLocalFileUri(this, document)
        if (fileUri != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, getMimeType(document.fileType))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                Toast.makeText(this, "Opening ${document.fileName}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No app available to open this file type", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File not found locally", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(fileType: String): String {
        return when (fileType.lowercase()) {
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            else -> "*/*"
        }
    }

    private fun deleteDocument(document: Document) {
        Log.d(TAG, "Delete initiated for: ${document.fileName}")
        AlertDialog.Builder(this)
            .setTitle("Delete Document")
            .setMessage("Are you sure you want to delete '${document.fileName}'? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                showLoading(true)
                Log.d(TAG, "Deleting document: ${document.fileName}")
                DocumentUtils.deleteDocument(
                    context = this,
                    document = document,
                    onSuccess = {
                        showLoading(false)
                        Log.d(TAG, "Document deleted successfully")
                        Toast.makeText(this, "Document deleted successfully", Toast.LENGTH_SHORT).show()
                        loadDocuments()
                    },
                    onError = { error ->
                        showLoading(false)
                        Log.e(TAG, "Error deleting document: $error")
                        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Log.d(TAG, "Delete cancelled")
                dialog.dismiss()
            }
            .show()
    }

    private fun updateStorageInfo() {
        val totalSize = documentsList.sumOf { it.fileSize }
        val documentCount = documentsList.size

        binding.tvStorageUsed.text = "Storage Used: ${Document.formatFileSize(totalSize)}"
        binding.tvDocumentCount.text = "Documents: $documentCount"

        Log.d(TAG, "Storage info updated - Count: $documentCount, Size: $totalSize")
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        Log.d(TAG, "Loading state: $show")
    }

    private fun updateEmptyState() {
        val isEmpty = documentsList.isEmpty()
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvDocuments.visibility = if (isEmpty) View.GONE else View.VISIBLE
        Log.d(TAG, "Empty state updated: $isEmpty")
    }
}