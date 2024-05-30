package com.example.todoapp.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.todoapp.R
import com.example.todoapp.alarm.TaskAlarmScheduler
import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.TaskEvent
import com.example.todoapp.util.SharedPreferences
import com.example.todoapp.util.TimeUtils
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch

class BottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)
        sharedPref = SharedPreferences(requireContext())

        val switch = view.findViewById<MaterialSwitch>(R.id.finishedFilterPicker)
        switch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEvent(TaskEvent.FilterDoneTasks(isChecked))
        }

        val categoryToggleButton = view.findViewById<MaterialButtonToggleGroup>(R.id.categoryToggleButton)
        categoryToggleButton.check(R.id.buttonNone)

        categoryToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.buttonNone -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.NONE))
                    R.id.buttonWork -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.WORK))
                    R.id.buttonSchool -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.SCHOOL))
                    R.id.buttonHome -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.HOME))
                    R.id.buttonNone -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.NONE))
                }
            }
        }

        val notificationTimeToggleButton = view.findViewById<MaterialButtonToggleGroup>(R.id.notificationTimeToggleButton)
        notificationTimeToggleButton.check(R.id.oneMinute)

        val notificationTime = sharedPref.loadNotificationTime()
        when (notificationTime) {
            1 -> notificationTimeToggleButton.check(R.id.oneMinute)
            5 -> notificationTimeToggleButton.check(R.id.fiveMinute)
            10 -> notificationTimeToggleButton.check(R.id.tenMinute)
            30 -> notificationTimeToggleButton.check(R.id.thirtyMinute)
            else -> notificationTimeToggleButton.check(R.id.oneMinute)
        }

        notificationTimeToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.oneMinute -> {
                        sharedPref.saveNotificationTime(1)
                    }
                    R.id.fiveMinute -> {
                        sharedPref.saveNotificationTime(5)
                    }
                    R.id.tenMinute -> {
                        sharedPref.saveNotificationTime(10)
                    }
                    R.id.thirtyMinute -> {
                        sharedPref.saveNotificationTime(30)
                    }
                }
                overrideScheduledNotification()
            }
        }


        return view
    }

    private fun overrideScheduledNotification() {
        val tasks = viewModel.state.value.tasks
        for (task in tasks) {
            if (task.notifications && task.dueTime > TimeUtils.getCurrentTime()) {
                val alarmScheduler = TaskAlarmScheduler(requireContext())
                val notificationTime = TimeUtils.convertToUtcZero(task.dueTime, requireContext())
                task.id?.let {
                    alarmScheduler.scheduleAlarm(
                        it,
                        notificationTime,
                        task.title,
                        task.description
                    )
                }
                Log.i("Logcat", "Scheduled notification for task ${task.id} for $notificationTime")
            }
        }
    }
}