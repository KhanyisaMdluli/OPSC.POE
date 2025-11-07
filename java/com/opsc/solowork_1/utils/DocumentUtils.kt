package com.opsc.solowork_1.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.opsc.solowork_1.model.Document
import java.io.File
import java.io.FileOutputStream
import java.util.*

object DocumentUtils {
    private val db = FirebaseFirestore.getInstance()

    fun uploadDocument(
        context: Context,
        fileUri: Uri,
        fileName: String,
        category: String,
        description: String,
        userId: String,
        onProgress: (Double) -> Unit,
        onSuccess: (Document) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Validate inputs
            if (userId.isEmpty()) {
                onError("User ID is empty")
                return
            }

            if (fileName.isEmpty()) {
                onError("File name is empty")
                return
            }

            // Create user directory if it doesn't exist
            val userDir = File(context.filesDir, "documents/$userId")
            if (!userDir.exists()) {
                userDir.mkdirs()
            }

            // Generate unique file name to avoid conflicts
            val fileExtension = getFileExtension(fileName).takeIf { it.isNotEmpty() } ?: "txt"
            val uniqueFileName = "${UUID.randomUUID()}_$fileName"
            val localFile = File(userDir, uniqueFileName)

            // Copy file to local storage
            context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                FileOutputStream(localFile).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    val fileSize = getFileSize(context, fileUri)

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        // Update progress
                        if (fileSize > 0) {
                            val progress = (100.0 * totalBytesRead) / fileSize
                            onProgress(progress)
                        }
                    }
                }

                // Create document record in Firestore
                val document = Document(
                    fileName = fileName,
                    fileType = fileExtension,
                    fileSize = localFile.length(),
                    category = category,
                    description = description,
                    localFilePath = "documents/$userId/$uniqueFileName",
                    userId = userId
                )

                addDocumentToFirestore(document, onSuccess, onError)
            } ?: run {
                onError("Could not open file stream")
            }

        } catch (e: Exception) {
            onError("Upload error: ${e.message}")
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { parcel ->
                parcel.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun addDocumentToFirestore(
        document: Document,
        onSuccess: (Document) -> Unit,
        onError: (String) -> Unit
    ) {
        val documentData = hashMapOf(
            "fileName" to document.fileName,
            "fileType" to document.fileType,
            "fileSize" to document.fileSize,
            "uploadDate" to document.uploadDate,
            "category" to document.category,
            "description" to document.description,
            "localFilePath" to document.localFilePath,
            "userId" to document.userId
        )

        db.collection("documents")
            .add(documentData)
            .addOnSuccessListener { documentRef ->
                val newDocument = document.copy(id = documentRef.id)
                onSuccess(newDocument)
            }
            .addOnFailureListener { e ->
                onError("Failed to save document info: ${e.message}")
            }
    }

    fun getDocuments(
        userId: String,
        onSuccess: (List<Document>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("documents")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                println("DEBUG: Found ${documents.size()} documents in collection")

                val documentList = mutableListOf<Document>()
                for (document in documents) {
                    try {
                        println("DEBUG: Processing document: ${document.id}")
                        val doc = createDocumentFromFirestore(document)
                        documentList.add(doc)
                    } catch (e: Exception) {
                        println("DEBUG: Error parsing document ${document.id}: ${e.message}")
                    }
                }
                onSuccess(documentList)
            }
            .addOnFailureListener { e ->
                println("DEBUG: Failed to fetch documents: ${e.message}")
                onError(e.message ?: "Failed to fetch documents")
            }
    }

    fun deleteDocument(
        context: Context,
        document: Document,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Delete from Firestore first
        db.collection("documents").document(document.id)
            .delete()
            .addOnSuccessListener {
                // Delete local file if path exists
                if (document.localFilePath.isNotEmpty()) {
                    val localFile = File(context.filesDir, document.localFilePath)
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                }
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Failed to delete document record: ${e.message}")
            }
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    fun getDocumentsByCategory(
        userId: String,
        category: String,
        onSuccess: (List<Document>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("documents")
            .whereEqualTo("userId", userId)
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val documentList = mutableListOf<Document>()
                for (document in documents) {
                    try {
                        val doc = createDocumentFromFirestore(document)
                        documentList.add(doc)
                    } catch (e: Exception) {
                        println("DEBUG: Error parsing document ${document.id}: ${e.message}")
                    }
                }
                onSuccess(documentList)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch documents by category")
            }
    }

    // COMPLETELY SAFE document creation
    private fun createDocumentFromFirestore(document: com.google.firebase.firestore.DocumentSnapshot): Document {
        // Print all available fields for debugging
        println("DEBUG: Available fields in document ${document.id}: ${document.data?.keys}")

        // Get localFilePath safely - it might not exist in old documents
        val localFilePath = try {
            document.getString("localFilePath") ?: ""
        } catch (e: Exception) {
            ""
        }

        return Document(
            id = document.id,
            fileName = document.getString("fileName") ?: "Unknown File",
            fileType = document.getString("fileType") ?: "txt",
            fileSize = document.getLong("fileSize") ?: 0,
            uploadDate = document.getDate("uploadDate") ?: Date(),
            category = document.getString("category") ?: Document.CATEGORY_GENERAL,
            description = document.getString("description") ?: "",
            localFilePath = localFilePath, // Use the safely retrieved value
            userId = document.getString("userId") ?: ""
        )
    }

    // Method to get local file URI for viewing/downloading using FileProvider
    fun getLocalFileUri(context: Context, document: Document): Uri? {
        return try {
            if (document.localFilePath.isNotEmpty()) {
                val localFile = File(context.filesDir, document.localFilePath)
                if (localFile.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        localFile
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}