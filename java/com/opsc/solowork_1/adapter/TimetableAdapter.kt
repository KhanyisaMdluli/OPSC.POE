package com.opsc.solowork_1.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.R
import com.opsc.solowork_1.model.TimetableEntry

class TimetableAdapter(
    private var entries: List<TimetableEntry>,
    private val onEntryClick: (TimetableEntry) -> Unit,
    private val onEntryDelete: (TimetableEntry) -> Unit
) : RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimetableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timetable, parent, false)
        return TimetableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimetableViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
    }

    override fun getItemCount() = entries.size

    fun updateEntries(newEntries: List<TimetableEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    inner class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        private val tvCourseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvInstructor: TextView = itemView.findViewById(R.id.tvInstructor)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val colorIndicator: View = itemView.findViewById(R.id.colorIndicator)

        fun bind(entry: TimetableEntry) {
            tvCourseName.text = entry.courseName
            tvCourseCode.text = entry.courseCode
            tvTime.text = "${entry.startTime} - ${entry.endTime}"
            tvLocation.text = entry.location
            tvInstructor.text = entry.instructor

            // Set color indicator
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(entry.color))
            } catch (e: Exception) {
                colorIndicator.setBackgroundColor(Color.parseColor("#2196F3"))
            }

            // Set click listeners
            itemView.setOnClickListener {
                onEntryClick(entry)
            }

            btnDelete.setOnClickListener {
                onEntryDelete(entry)
            }
        }
    }
}