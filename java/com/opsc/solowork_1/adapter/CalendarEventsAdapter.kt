package com.opsc.solowork_1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.R
import com.opsc.solowork_1.model.CalendarEvent
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventsAdapter(
    private var events: List<CalendarEvent>,
    private val onEventClick: (CalendarEvent) -> Unit,
    private val onEventDelete: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<CalendarEventsAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<CalendarEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvEventType: TextView = itemView.findViewById(R.id.tvEventType)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(event: CalendarEvent) {
            tvTitle.text = event.title
            tvLocation.text = event.location
            tvEventType.text = event.eventType

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeText = if (event.isAllDay) {
                "All Day"
            } else {
                "${timeFormat.format(event.startTime)} - ${timeFormat.format(event.endTime)}"
            }
            tvTime.text = timeText

            when (event.eventType) {
                CalendarEvent.TYPE_WORK -> {
                    tvEventType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.event_type_work))
                }
                CalendarEvent.TYPE_STUDY -> {
                    tvEventType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.event_type_study))
                }
                CalendarEvent.TYPE_MEETING -> {
                    tvEventType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.event_type_meeting))
                }
                else -> {
                    tvEventType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.event_type_personal))
                }
            }

            itemView.setOnClickListener {
                onEventClick(event)
            }

            btnDelete.setOnClickListener {
                onEventDelete(event)
            }
        }
    }
}