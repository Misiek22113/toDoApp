package com.example.todoapp

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileManager(private val activity: ComponentActivity) {
    private var callback: ((String) -> Unit)? = null
    private lateinit var getContent: ActivityResultLauncher<String>

    fun registerForResult() {
        getContent = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val fileName: String = uri?.path.toString().substringAfterLast("/")
            if (uri != null) {
                val copiedFilePath = copyFileToAppDirectory(uri, fileName)
                callback?.invoke(copiedFilePath ?: "")
            }
        }
    }

    private val pickFileLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                val fileName: String = result.data?.data?.path.toString().substringAfterLast("/")
                if (uri != null) {
                    val copiedFilePath = copyFileToAppDirectory(uri, fileName)
                    callback?.invoke(copiedFilePath ?: "")
                }
            }
        }

    fun pickFile(callback: (String) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickFileLauncher.launch(intent)
        this.callback = callback
    }

    private fun copyFileToAppDirectory(uri: Uri, fileName: String?): String? {
        try {
            val inputStream = activity.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val directory = File(activity.filesDir, "files")
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
                return file.absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}