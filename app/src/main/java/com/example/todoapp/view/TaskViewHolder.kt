package com.example.todoapp.view

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R

class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val taskName: TextView = view.findViewById(R.id.taskName)
    val taskCreationTime: TextView = view.findViewById(R.id.taskCategory)
}