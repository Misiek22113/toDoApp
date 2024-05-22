package com.example.todoapp

import BottomSheet
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Dialog
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.weather_app.adapter.TaskAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "task_database"
        ).build()
    }

    private val viewModel by viewModels<TaskViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskViewModel(db.dao) as T
                }
            }
        }
    )

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dialogView = layoutInflater.inflate(R.layout.add_task_dialog, null)

        val taskTitleText = dialogView.findViewById<TextInputLayout>(R.id.taskTitle)
        val taskDescriptionText = dialogView.findViewById<TextInputLayout>(R.id.taskDescription)
        val category = dialogView.findViewById<AutoCompleteTextView>(R.id.menuCategory)
        val datePicker = dialogView.findViewById<TextInputLayout>(R.id.dateInputLayout)
        val dateEditText = dialogView.findViewById<TextInputEditText>(R.id.dateEditText)

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select date")
        val materialDatePicker = builder.build()

        datePicker.editText?.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        var dueDate: Long = 0

        materialDatePicker.addOnPositiveButtonClickListener {
            val selectedDate = it
            val dateString =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))
            dateEditText.setText(dateString)
            dueDate = selectedDate
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.add_task_dialog_title))
            .setMessage(resources.getString(R.layout.add_task_dialog))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                val title = taskTitleText.editText?.text.toString()
                val description = taskDescriptionText.editText?.text.toString()
                addTaskToDatabase(title, description, dueDate, category.text.toString())
            }.setView(dialogView)

        adapter = TaskAdapter(emptyList(), viewModel)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                adapter.updateTasks(state.tasks)
            }
        }

        val bottomSheetDialog = BottomSheet()

        binding.settingsButton.setOnClickListener {
            bottomSheetDialog.show(supportFragmentManager, "MyBottomSheetDialogFragment")
        }

        binding.fab.setOnClickListener {
            dialog.show()
        }

    }

    private fun getCategoryType(category: String): CategoryType {
        return when (category) {
            "work" -> CategoryType.WORK
            "school" -> CategoryType.SCHOOL
            "home" -> CategoryType.HOME
            else -> CategoryType.NONE
        }
    }

    private fun addTaskToDatabase(title: String, description: String, dueDate: Long, category: String) {
        if (title.isNotBlank() && description.isNotBlank() && dueDate != 0L) {
            viewModel.onEvent(TaskEvent.SetTitle(title))
            viewModel.onEvent(TaskEvent.SetDescription(description))
            viewModel.onEvent(TaskEvent.SetIsCompleted(false))
            viewModel.onEvent(TaskEvent.SetCreateTime(System.currentTimeMillis()))
            viewModel.onEvent(TaskEvent.SetDueTime(dueDate))
            viewModel.onEvent(TaskEvent.SetCategory(getCategoryType(category)))
            viewModel.onEvent(TaskEvent.AddTask)
        }
    }
}
