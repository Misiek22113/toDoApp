package com.example.todoapp.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.todoapp.R
import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.TaskEvent
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch

class BottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

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

        notificationTimeToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.oneMinute -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.NONE))
                    R.id.fiveMinute -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.WORK))
                    R.id.tenMinute -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.SCHOOL))
                    R.id.thirtyMinute -> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.HOME))
                    R.id.oneMinute-> viewModel.onEvent(TaskEvent.FilterTasks(CategoryType.NONE))
                }
            }
        }


        return view
    }

}