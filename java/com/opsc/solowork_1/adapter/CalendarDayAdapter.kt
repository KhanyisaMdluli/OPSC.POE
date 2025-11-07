package com.opsc.solowork_1.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.R
import com.opsc.solowork_1.model.CalendarEvent
import java.util.*

class CalendarDayAdapter(
    private val days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder>() {

    data class CalendarDay(
        val day: Int,
        val isCurrentMonth: Boolean = true,
        val isToday: Boolean = false,
        val isSelected: Boolean = false,
        val events: List<CalendarEvent> = emptyList(),
        val date: Date = Date()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount() = days.size

    inner class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val eventDotsLayout: LinearLayout = itemView.findViewById(R.id.eventDotsLayout)
        private val cardView: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.cardView)

        fun bind(day: CalendarDay) {
            tvDayNumber.text = if (day.day > 0) day.day.toString() else ""

            when {
                !day.isCurrentMonth -> {
                    tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, R.color.light_grey))
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.background))
                }
                day.isToday -> {
                    tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_color))
                }
                day.isSelected -> {
                    tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary_color))
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.light_blue))
                }
                else -> {
                    tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                }
            }

            eventDotsLayout.removeAllViews()

            val eventsToShow = day.events.take(3)
            eventsToShow.forEach { event ->
                val dotView = View(itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(8, 8).apply {
                        marginEnd = 2
                    }
                    setBackgroundColor(getEventColor(event.eventType))
                }
                eventDotsLayout.addView(dotView)
            }

            if (day.events.size > 3) {
                val moreView = TextView(itemView.context).apply {
                    text = "+${day.events.size - 3}"
                    textSize = 8f
                    setTextColor(ContextCompat.getColor(itemView.context, R.color.grey))
                }
                eventDotsLayout.addView(moreView)
            }

            itemView.setOnClickListener {
                if (day.isCurrentMonth && day.day > 0) {
                    onDayClick(day)
                }
            }
        }

        private fun getEventColor(eventType: String): Int {
            return when (eventType) {
                CalendarEvent.TYPE_WORK -> ContextCompat.getColor(itemView.context, R.color.event_type_work)
                CalendarEvent.TYPE_STUDY -> ContextCompat.getColor(itemView.context, R.color.event_type_study)
                CalendarEvent.TYPE_MEETING -> ContextCompat.getColor(itemView.context, R.color.event_type_meeting)
                else -> ContextCompat.getColor(itemView.context, R.color.event_type_personal)
            }
        }
    }
}