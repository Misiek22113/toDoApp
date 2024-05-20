package com.example.todoapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.Dialog
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.todoapp.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        addTaskButton.setOnClickListener {
            val title = taskTitleText.editText?.text.toString()
            val description = taskDescriptionText.editText?.text.toString()
            if (title.isNotBlank() && description.isNotBlank()) {
                viewModel.onEvent(TaskEvent.SetTitle(title))
                viewModel.onEvent(TaskEvent.SetDescription(description))
                viewModel.onEvent(TaskEvent.SetIsCompleted(false))
                viewModel.onEvent(TaskEvent.SetCreateTime(System.currentTimeMillis()))
                viewModel.onEvent(TaskEvent.SetDueTime(System.currentTimeMillis()))
                viewModel.onEvent(TaskEvent.AddTask)
                dialog.dismiss()
            }
        }

        binding.fab.setOnClickListener {
            dialog.show()
        }


    }
}
