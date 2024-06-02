package com.example.todoapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.alarm.TaskAlarmScheduler
import com.example.todoapp.database.TaskDatabase
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.TaskEvent
import com.example.todoapp.util.TaskNotificationService
import com.example.todoapp.view.BottomSheet
import com.example.todoapp.view.TaskViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var service: TaskNotificationService
    private lateinit var taskAlarmManager: TaskAlarmScheduler

    private val fileManager by lazy { FileManager(this) }

    private val addTaskDialog by lazy {
        AddTaskDialog(
            this,
            fileManager,
            viewModel,
            NotificationManager(this)
        )
    }

    private val editTaskDialog by lazy {
        EditTaskDialog(
            this,
            this,
            viewModel,
            taskAlarmManager,
            ::getCategoryType,
            NotificationManager(this)
        )
    }

    private val REQUEST_CODE_PERMISSION = 1

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
        fileManager.registerForResult()
        service = TaskNotificationService(this)
        taskAlarmManager = TaskAlarmScheduler(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = TaskAdapter(emptyList(), viewModel) { task ->
            editTaskDialog.create(task)
        }

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
            addTaskDialog.show()
        }


        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.onEvent(TaskEvent.SearchTaskQuery(query ?: ""))
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onEvent(TaskEvent.SearchTaskQuery(newText ?: ""))
                return true
            }
        })

    }

    private fun getCategoryType(category: String): CategoryType {
        return when (category) {
            CategoryType.WORK.toString() -> CategoryType.WORK
            CategoryType.SCHOOL.toString() -> CategoryType.SCHOOL
            CategoryType.HOME.toString() -> CategoryType.HOME
            else -> CategoryType.NONE
        }
    }

}

