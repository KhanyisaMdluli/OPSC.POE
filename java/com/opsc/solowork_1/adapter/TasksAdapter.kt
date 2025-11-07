package com.opsc.solowork_1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.opsc.solowork_1.R
import com.opsc.solowork_1.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TasksAdapter(
    private var tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit,
    private val onTaskStatusChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbTask: CheckBox = itemView.findViewById(R.id.cbTask)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvDescription.text = task.description
            cbTask.isChecked = task.isCompleted

            // Strike through text if completed
            if (task.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvDescription.paintFlags = tvDescription.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvDescription.paintFlags = tvDescription.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // Format due date
            task.dueDate?.let {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                tvDueDate.text = dateFormat.format(it)
                tvDueDate.visibility = View.VISIBLE
            } ?: run {
                tvDueDate.visibility = View.GONE
            }

            // Set priority color
            when (task.priority) {
                Task.PRIORITY_HIGH -> {
                    tvPriority.text = "High"
                    tvPriority.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.priority_high))
                }
                Task.PRIORITY_MEDIUM -> {
                    tvPriority.text = "Medium"
                    tvPriority.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.priority_medium))
                }
                Task.PRIORITY_LOW -> {
                    tvPriority.text = "Low"
                    tvPriority.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.priority_low))
                }
            }

            // Set click listeners
            itemView.setOnClickListener {
                onTaskClick(task)
            }

            btnDelete.setOnClickListener {
                onTaskDelete(task)
            }

            cbTask.setOnCheckedChangeListener { _, isChecked ->
                onTaskStatusChange(task, isChecked)
            }
        }
    }
}