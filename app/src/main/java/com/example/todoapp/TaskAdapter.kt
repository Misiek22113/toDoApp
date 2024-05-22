package com.example.weather_app.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.CategoryType
import com.example.todoapp.R
import com.example.todoapp.Task
import com.example.todoapp.TaskViewModel
import java.util.Locale

class TaskAdapter (
    private var tasks: List<Task>,
    private val viewModel: TaskViewModel
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val taskTextView: TextView = view.findViewById(R.id.taskName)
        val taskCategory: TextView = view.findViewById(R.id.taskCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.todo_card, parent, false)
        return TaskViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        Log.i("Logcat", ("wyszukane: $newTasks").toString())
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTextView.text = task.title
        holder.taskCategory.text = getTaskCategoryDescription(task.category)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun getTaskCategoryDescription(category: String): String {
        return when (category) {
            CategoryType.WORK.name -> "work"
            CategoryType.SCHOOL.name -> "school"
            CategoryType.HOME.name -> "home"
            else -> ""
        }
    }
}