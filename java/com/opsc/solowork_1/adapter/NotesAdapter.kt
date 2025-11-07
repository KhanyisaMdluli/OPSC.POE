package com.opsc.solowork_1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.R
import com.opsc.solowork_1.model.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter(
    private var notes: List<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvUpdatedAt: TextView = itemView.findViewById(R.id.tvUpdatedAt)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(note: Note) {
            tvTitle.text = note.title
            tvContent.text = note.content
            tvCategory.text = note.category

            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            tvUpdatedAt.text = dateFormat.format(note.updatedAt)

            // Set category color
            when (note.category) {
                "Work" -> {
                    tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.category_work))
                }
                "Personal" -> {
                    tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.category_personal))
                }
                "Study" -> {
                    tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.category_study))
                }
                "Ideas" -> {
                    tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.category_ideas))
                }
                else -> {
                    tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.category_general))
                }
            }

            itemView.setOnClickListener {
                onNoteClick(note)
            }

            btnDelete.setOnClickListener {
                onNoteDelete(note)
            }
        }
    }
}