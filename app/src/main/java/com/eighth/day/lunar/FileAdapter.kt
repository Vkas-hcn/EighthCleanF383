package com.eighth.day.lunar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FileAdapter(
    private val files: MutableList<FileItem>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFileIcon: ImageView = itemView.findViewById(R.id.iv_file_icon)
        val tvFileName: TextView = itemView.findViewById(R.id.tv_file_name)
        val tvFileSize: TextView = itemView.findViewById(R.id.tv_file_size)
        val imgFileSelect: ImageView = itemView.findViewById(R.id.iv_select_status)

        fun bind(fileItem: FileItem, position: Int) {
            tvFileName.text = fileItem.name
            tvFileSize.text = formatFileSize(fileItem.size)
            imgFileIcon.setImageResource(fileItem.getIconResId())
            
            // 设置选中状态
            imgFileSelect.setImageResource(
                if (fileItem.isSelected) R.drawable.ic_selete_3
                else R.drawable.ic_disselete_3
            )
            
            // 点击整个项目
            itemView.setOnClickListener {
                fileItem.isSelected = !fileItem.isSelected
                notifyItemChanged(position)
                onSelectionChanged()
            }
            
            // 点击选中图标
            imgFileSelect.setOnClickListener {
                fileItem.isSelected = !fileItem.isSelected
                notifyItemChanged(position)
                onSelectionChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_clean, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position], position)
    }

    override fun getItemCount(): Int = files.size
    
    fun updateFiles(newFiles: List<FileItem>) {
        files.clear()
        files.addAll(newFiles)
        notifyDataSetChanged()
    }
    
    fun getSelectedFiles(): List<FileItem> {
        return files.filter { it.isSelected }
    }
    
    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes < 1000 -> "$sizeInBytes B"
            sizeInBytes < 1000 * 1000 -> String.format("%.1f KB", sizeInBytes / 1000.0)
            sizeInBytes < 1000 * 1000 * 1000 -> String.format("%.1f MB", sizeInBytes / (1000.0 * 1000.0))
            else -> String.format("%.1f GB", sizeInBytes / (1000.0 * 1000.0 * 1000.0))
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }
}
