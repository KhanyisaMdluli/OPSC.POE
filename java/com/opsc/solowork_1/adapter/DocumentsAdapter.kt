package com.opsc.solowork_1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.R
import com.opsc.solowork_1.model.Document
import java.text.SimpleDateFormat
import java.util.*

class DocumentsAdapter(
    private var documents: List<Document>,
    private val onDocumentClick: (Document) -> Unit,
    private val onDocumentDownload: (Document) -> Unit,
    private val onDocumentDelete: (Document) -> Unit
) : RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document, parent, false)
        return DocumentViewHolder(view)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val document = documents[position]
        holder.bind(document)
    }

    override fun getItemCount() = documents.size

    fun updateDocuments(newDocuments: List<Document>) {
        documents = newDocuments
        notifyDataSetChanged()
    }

    inner class DocumentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivFileIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        private val tvUploadDate: TextView = itemView.findViewById(R.id.tvUploadDate)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val btnDownload: ImageButton = itemView.findViewById(R.id.btnDownload)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(document: Document) {
            tvFileName.text = document.fileName
            tvFileSize.text = Document.formatFileSize(document.fileSize)
            tvCategory.text = document.category

            // Format upload date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            tvUploadDate.text = dateFormat.format(document.uploadDate)

            // Set file icon based on file type
            val fileIcon = getFileIcon(document.fileType)
            ivFileIcon.setImageResource(fileIcon)

            // Set category color
            when (document.category) {
                Document.CATEGORY_STUDY -> {
                    tvCategory.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.category_study)
                    )
                }
                Document.CATEGORY_WORK -> {
                    tvCategory.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.category_work)
                    )
                }
                Document.CATEGORY_PERSONAL -> {
                    tvCategory.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.category_personal)
                    )
                }
                Document.CATEGORY_IMPORTANT -> {
                    tvCategory.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.priority_high)
                    )
                }
                else -> {
                    tvCategory.setBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.category_general)
                    )
                }
            }

            // TEMPORARY FIX: Disable download for now until we fix the upload
            btnDownload.isEnabled = false
            btnDownload.alpha = 0.5f
            btnDownload.contentDescription = "Download coming soon"

            // Set click listeners
            itemView.setOnClickListener {
                onDocumentClick(document)
            }

            btnDownload.setOnClickListener {
                // Temporary: Show message that download is coming soon
                // onDocumentDownload(document) - Disabled for now
            }

            btnDelete.setOnClickListener {
                onDocumentDelete(document)
            }
        }

        private fun getFileIcon(fileType: String): Int {
            return when (fileType.lowercase(Locale.ROOT)) {
                "pdf" -> R.drawable.ic_pdf
                "doc", "docx" -> R.drawable.ic_document
                "txt" -> R.drawable.ic_text_file
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> R.drawable.ic_image
                "xls", "xlsx", "csv" -> R.drawable.ic_spreadsheet
                "ppt", "pptx" -> R.drawable.ic_presentation
                "zip", "rar", "7z", "tar", "gz" -> R.drawable.ic_archive
                else -> R.drawable.ic_file
            }
        }
    }
}