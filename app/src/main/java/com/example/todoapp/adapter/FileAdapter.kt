package com.example.todoapp.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.FileCardBinding

class FileAdapter(
    private var files: List<String>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: FileCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = FileCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.binding.fileName.text = file.substringAfterLast("/")
        holder.binding.deleteButton.setOnClickListener {
            onDelete(file)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = files.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateFiles(newFiles: List<String>) {
        files = newFiles
        notifyDataSetChanged()
        Log.i("Logcat", "Files: $files")
    }
}