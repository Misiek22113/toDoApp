package com.example.todoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.todoapp.database.TaskDatabase
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.model.CategoryType
import com.example.todoapp.model.Task
import com.example.todoapp.model.TaskEvent
import com.example.todoapp.view.BottomSheet
import com.example.todoapp.view.TaskViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TaskAdapter

    private val REQUEST_CODE_PERMISSION = 1
    private val REQUEST_CODE_PICK_FILE = 2

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pickFileButton = findViewById<Button>(R.id.pick_file_button)
        pickFileButton.setOnClickListener {
            pickFile()
        }

        adapter = TaskAdapter(emptyList(), viewModel, ::createEditTaskDialog)

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
            showAddTaskDialog()
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

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_task_dialog, null)

        val taskTitleText = dialogView.findViewById<TextInputLayout>(R.id.taskTitle)
        val taskDescriptionText = dialogView.findViewById<TextInputLayout>(R.id.taskDescription)
        val categoryToggleButton =
            dialogView.findViewById<MaterialButtonToggleGroup>(R.id.categoryToggleButton)
        val datePicker = dialogView.findViewById<TextInputLayout>(R.id.dateInputLayout)
        val notificationsSwitch = dialogView.findViewById<MaterialSwitch>(R.id.notificationsSwitch)
        var category: CategoryType = CategoryType.NONE

        categoryToggleButton.check(R.id.buttonNone)

        categoryToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
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

        datePicker.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        var dueDate: Long = 0

        materialDatePicker.addOnPositiveButtonClickListener { selectedDate ->
            dueDate = selectedDate
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.add_task_dialog_title))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
            }
            .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                val title = taskTitleText.editText?.text.toString()
                val description = taskDescriptionText.editText?.text.toString()
                addTaskToDatabase(
                    title,
                    description,
                    dueDate,
                    category,
                    notificationsSwitch.isChecked
                )
            }.setView(dialogView)

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun createEditTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.edit_task_dialog, null)

        val taskTitleText = dialogView.findViewById<TextInputLayout>(R.id.taskTitle)
        val createdTimeText = dialogView.findViewById<TextView>(R.id.createdTime)
        val taskDescriptionText = dialogView.findViewById<TextInputLayout>(R.id.taskDescription)
        val categoryToggleButton =
            dialogView.findViewById<MaterialButtonToggleGroup>(R.id.categoryToggleButton)
        val datePicker = dialogView.findViewById<TextInputLayout>(R.id.dateInputLayout)
        val notificationsSwitch = dialogView.findViewById<MaterialSwitch>(R.id.notificationsSwitch)

        createdTimeText.text = "Created at: ${formatDate(task.createTime)}"
        taskTitleText.editText?.setText(task.title)
        taskDescriptionText.editText?.setText(task.description)
        notificationsSwitch.isChecked = task.notifications
        datePicker.editText?.setText(task.dueTime.toString())
        categoryToggleButton.check(
            when (task.category) {
                CategoryType.WORK.toString() -> R.id.buttonWork
                CategoryType.SCHOOL.toString() -> R.id.buttonSchool
                CategoryType.HOME.toString() -> R.id.buttonHome
                else -> R.id.buttonNone
            }
        )

        var category: CategoryType = getCategoryType(task.category)

        categoryToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
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

        datePicker.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        var dueDate: Long = task.dueTime

        materialDatePicker.addOnPositiveButtonClickListener { selectedDate ->
            dueDate = selectedDate
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.edit_task_title))
            .setNegativeButton(resources.getString(R.string.delete_task)) { dialog, which ->
                if (task.id != null)
                    viewModel.onEvent(TaskEvent.DeleteTaskById(task.id))
            }
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.apply_changes)) { dialog, which ->
                val title = taskTitleText.editText?.text.toString()
                val description = taskDescriptionText.editText?.text.toString()
                updateTaskInDatabase(
                    task.id,
                    title,
                    description,
                    dueDate,
                    category,
                    notificationsSwitch.isChecked,
                    task.createTime,
                    task.isCompleted
                )
            }.setView(dialogView)

        dialog.show()
    }

    private fun addTaskToDatabase(
        title: String,
        description: String,
        dueDate: Long,
        category: CategoryType,
        notifications: Boolean = false,
        createTime: Long = System.currentTimeMillis(),
        isCompleted: Boolean = false
    ) {
        if (title.isNotBlank() && description.isNotBlank() && dueDate != 0L) {
            viewModel.onEvent(TaskEvent.SetTitle(title))
            viewModel.onEvent(TaskEvent.SetDescription(description))
            viewModel.onEvent(TaskEvent.SetIsCompleted(isCompleted))
            viewModel.onEvent(TaskEvent.SetCreateTime(createTime))
            viewModel.onEvent(TaskEvent.SetDueTime(dueDate))
            viewModel.onEvent(TaskEvent.SetCategory(category))
            viewModel.onEvent(TaskEvent.SetNotifications(notifications))
            viewModel.onEvent(TaskEvent.AddTask)
        }
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
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_PERMISSION) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Uprawnienia przyznane, możesz kontynuować
//            } else {
//                // Uprawnienia odrzucone, poinformuj użytkownika
//            }
//        }
//    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickFileLauncher.launch(intent)
    }

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            val fileName: String = result.data?.data?.path.toString().substringAfterLast("/")
            if (uri != null) {
                contentResolver.openInputStream(uri)
                copyFileToAppDirectory(uri, fileName)
            }
        }
    }

    private fun copyFileToAppDirectory(uri: Uri, fileName: String?) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val directory = File(filesDir, "files")
                if (!directory.exists()) {
                    directory.mkdir()
                }

                val file = File(directory, fileName ?: "file.txt")
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
                inputStream.close()
                outputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
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
