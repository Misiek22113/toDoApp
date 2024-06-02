package com.example.todoapp

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import com.example.todoapp.databinding.AddTaskDialogBinding
import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.TaskEvent
import com.example.todoapp.util.TimeUtils.getFinalTime
import com.example.todoapp.view.TaskViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class AddTaskDialog(
    private val activity: FragmentActivity,
    private val fileManager: FileManager,
    private val viewModel: TaskViewModel,
    private val notificationManager: NotificationManager
    ) {
    @SuppressLint("InflateParams")
    fun show() {
        val binding = AddTaskDialogBinding.inflate(LayoutInflater.from(activity))

        var category: CategoryType = CategoryType.NONE
        val filePaths = mutableListOf<String>()

        binding.addFile.setOnClickListener {
            fileManager.pickFile { copiedFilePath ->
                filePaths.add(copiedFilePath)
                Log.i("Logcat", "Copied file path: $filePaths")
            }
        }

        binding.categoryToggleButton.check(R.id.buttonNone)

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

        var dueDate: Long = 0

        materialDatePicker.addOnPositiveButtonClickListener { selectedDate ->
            dueDate = selectedDate
        }


        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(activity.resources.getString(R.string.add_task_dialog_title))
            .setPositiveButton(activity.resources.getString(R.string.accept)) { dialog, which ->
                val title = binding.taskTitle.editText?.text.toString()
                val description = binding.taskDescription.editText?.text.toString()
                addTaskToDatabase(
                    title,
                    description,
                    getFinalTime(dueDate, dueHour, dueMinute),
                    category,
                    binding.notificationsSwitch.isChecked,
                    filePaths = filePaths
                )
            }.setView(binding.root)

        dialog.show()
    }

    private fun addTaskToDatabase(
        title: String,
        description: String,
        dueDate: Long,
        category: CategoryType,
        notifications: Boolean = false,
        createTime: Long = System.currentTimeMillis(),
        isCompleted: Boolean = false,
        filePaths: List<String>? = null
    ) {
        if (title.isNotBlank() && description.isNotBlank() && dueDate != 0L) {
            val id = generateId(title, dueDate)
            viewModel.onEvent(TaskEvent.SetTitle(title))
            viewModel.onEvent(TaskEvent.SetDescription(description))
            viewModel.onEvent(TaskEvent.SetIsCompleted(isCompleted))
            viewModel.onEvent(TaskEvent.SetCreateTime(createTime))
            viewModel.onEvent(TaskEvent.SetDueTime(dueDate))
            viewModel.onEvent(TaskEvent.SetCategory(category))
            viewModel.onEvent(TaskEvent.SetNotifications(notifications))
            viewModel.onEvent(TaskEvent.SetAttachments(filePaths ?: emptyList()))
            viewModel.onEvent(TaskEvent.SetId(id))
            viewModel.onEvent(TaskEvent.AddTask)
            if (notifications) {
                notificationManager.setNotification(title, description, dueDate, id)
            }
        }
    }

    private fun generateId(title: String, dueDate: Long): Int {
        var id = (title.hashCode() + dueDate.hashCode()) % 1000000
        if (id < 0) {
            id *= -1
        }
        return id
    }
}