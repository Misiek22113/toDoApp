package com.example.todoapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.model.CategoryType
import com.example.todoapp.R
import com.example.todoapp.model.Task
import com.example.todoapp.model.TaskEvent
import com.example.todoapp.view.TaskViewModel

class TaskAdapter (
    private var tasks: List<Task>,
    private val viewModel: TaskViewModel
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val taskTextView: TextView = view.findViewById(R.id.taskCheckBox)
        val taskCategory: TextView = view.findViewById(R.id.taskCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.todo_card, parent, false)
        return TaskViewHolder(view)
    }

    fun updateTasks(newTasks: List<Task>) {
        val diffCallback = TaskDiffCallback(tasks, newTasks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        tasks = newTasks
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTextView.text = task.title
        holder.taskCategory.text = getTaskCategoryDescription(task.category)

        val checkBox: CheckBox = holder.view.findViewById(R.id.taskCheckBox)

        checkBox.setOnCheckedChangeListener(null)

        checkBox.isChecked = task.isCompleted
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isCompleted = isChecked
            viewModel.onEvent(TaskEvent.SetIsCompleted(isChecked))
            viewModel.onEvent(TaskEvent.UpdateTask(task))
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    private fun getTaskCategoryDescription(category: String): String {
        return when (category) {
            CategoryType.WORK.name -> "work"
            CategoryType.SCHOOL.name -> "school"
            CategoryType.HOME.name -> "home"
            else -> ""
        }
    }
}

class TaskDiffCallback(
    private val oldList: List<Task>,
    private val newList: List<Task>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}