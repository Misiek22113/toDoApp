package com.example.todoapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Dialog
import android.util.Log
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

    private val viewModel by viewModels<TaskViewModel> (
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskViewModel(db.dao) as T
                }
            }
        }
    )

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

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_task_dialog)

        val taskTitleText = dialog.findViewById<TextInputLayout>(R.id.taskTitle)
        val taskDescriptionText = dialog.findViewById<TextInputLayout>(R.id.taskDescription)
        val addTaskButton = dialog.findViewById<Button>(R.id.addTask)

        adapter = TaskAdapter(emptyList(), viewModel)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.state.collect {state ->
                adapter.updateTasks(state.tasks)
            }
        }

        val datePicker = dialog.findViewById<TextInputLayout>(R.id.dateInputLayout)
        val dateEditText = dialog.findViewById<TextInputEditText>(R.id.dateEditText)
        var dueDate: Long = 0

        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select date")
        val materialDatePicker = builder.build()

        datePicker.editText?.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            val selectedDate = it
            val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))
            dateEditText.setText(dateString)
            dueDate = selectedDate
        }

        addTaskButton.setOnClickListener {
            val title = taskTitleText.editText?.text.toString()
            val description = taskDescriptionText.editText?.text.toString()

            if (title.isNotBlank() && description.isNotBlank() && dueDate != 0L) {
                viewModel.onEvent(TaskEvent.SetTitle(title))
                viewModel.onEvent(TaskEvent.SetDescription(description))
                viewModel.onEvent(TaskEvent.SetIsCompleted(false))
                viewModel.onEvent(TaskEvent.SetCreateTime(System.currentTimeMillis()))
                viewModel.onEvent(TaskEvent.SetDueTime(dueDate))
                viewModel.onEvent(TaskEvent.SetCategory(CategoryType.NONE))
                viewModel.onEvent(TaskEvent.AddTask)
                Log.i("Logcat", "Adding task with title: $title and description: $description")
                dialog.dismiss()
            }
        }

        binding.fab.setOnClickListener {
            dialog.show()
        }

    }
}
