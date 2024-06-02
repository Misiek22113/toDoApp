package com.example.todoapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.example.todoapp.alarm.TaskAlarmScheduler
import com.example.todoapp.databinding.EditTaskDialogBinding
import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.Task
import com.example.todoapp.model.TaskEvent
import com.example.todoapp.util.TimeUtils.formatDate
import com.example.todoapp.util.TimeUtils.getFinalTime
import com.example.todoapp.view.TaskViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class EditTaskDialog(
    private val activity: FragmentActivity,
    private val context: Context,
    private val viewModel: TaskViewModel,
    private val taskAlarmManager: TaskAlarmScheduler,
    private val getCategoryType: (String) -> CategoryType,
    private val notificationManager: NotificationManager
) {
    @SuppressLint("SetTextI18n")
    fun create(task: Task) {
        val binding = EditTaskDialogBinding.inflate(LayoutInflater.from(context))

        binding.createdTime.text = "Created at: ${formatDate(task.createTime)}"
        binding.taskTitle.editText?.setText(task.title)
        binding.taskDescription.editText?.setText(task.description)
        binding.notificationsSwitch.isChecked = task.notifications
        binding.dateInputLayout.editText?.setText(task.dueTime.toString())
        binding.categoryToggleButton.check(
            when (task.category) {
                CategoryType.WORK.toString() -> R.id.buttonWork
                CategoryType.SCHOOL.toString() -> R.id.buttonSchool
                CategoryType.HOME.toString() -> R.id.buttonHome
                else -> R.id.buttonNone
            }
        )

        var category: CategoryType = getCategoryType(task.category)

        binding.categoryToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonNone -> category = CategoryType.NONE
                    R.id.buttonWork -> category = CategoryType.WORK
                    R.id.buttonSchool -> category = CategoryType.SCHOOL
                    R.id.buttonHome -> category = CategoryType.HOME
                    R.id.buttonNone -> category = CategoryType.NONE
                }
            }
        }

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select due date")
        builder.setCalendarConstraints(
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()
        )

        val materialDatePicker = builder.build()

        binding.dateInputLayout.setOnClickListener {
            materialDatePicker.show(activity.supportFragmentManager, "DATE_PICKER")
        }

        var dueDate: Long = task.dueTime

        materialDatePicker.addOnPositiveButtonClickListener { selectedDate ->
            dueDate = selectedDate
        }

        val picker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select due time")
                .build()

        var dueHour = 0
        var dueMinute = 0

        picker.addOnPositiveButtonClickListener {
            dueHour = picker.hour
            dueMinute = picker.minute
            Log.i("Logcat", "Due time: $dueHour:$dueMinute")
        }

        binding.hourInput.setOnClickListener {
            picker.show(activity.supportFragmentManager, "TIME_PICKER")
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.edit_task_title)) // use context.resources
            .setNegativeButton(context.resources.getString(R.string.delete_task)) { _, _ ->
                if (task.id != null)
                    viewModel.onEvent(TaskEvent.DeleteTaskById(task.id))
            }
            .setNeutralButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(context.resources.getString(R.string.apply_changes)) { _, _ ->
                val title = binding.taskTitle.editText?.text.toString()
                val description = binding.taskDescription.editText?.text.toString()
                if (task.notifications && !binding.notificationsSwitch.isChecked) {
                    taskAlarmManager.cancelAlarm(task.id ?: 0)
                }
                updateTaskInDatabase(
                    task.id,
                    title,
                    description,
                    getFinalTime(dueDate, dueHour, dueMinute),
                    category,
                    binding.notificationsSwitch.isChecked,
                    task.createTime,
                    task.isCompleted
                )
            }.setView(binding.root)

        dialog.show()
    }

    private fun updateTaskInDatabase(
        taskId: Int? = null,
        title: String,
        description: String,
        dueDate: Long,
        category: CategoryType,
        notifications: Boolean = false,
        createTime: Long = System.currentTimeMillis(),
        isCompleted: Boolean = false
    ) {
        if (title.isNotBlank() && description.isNotBlank() && dueDate != 0L) {
            viewModel.onEvent(
                TaskEvent.UpdateTask(
                    Task(
                        title,
                        description,
                        createTime,
                        dueDate,
                        notifications,
                        isCompleted,
                        category.toString(),
                        emptyList(),
                        taskId
                    )
                )
            )
            if (notifications) {
                notificationManager.setNotification(title, description, dueDate, taskId ?: 0)
            }
        }
    }
}