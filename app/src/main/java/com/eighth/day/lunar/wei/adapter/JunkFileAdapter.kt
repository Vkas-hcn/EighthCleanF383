package com.eighth.day.lunar.wei.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.eighth.day.lunar.R
import com.eighth.day.lunar.databinding.TssFileBinding
import com.eighth.day.lunar.wei.model.JunkFileModel


class JunkFileAdapter(
    private val files: MutableList<JunkFileModel>,
    private val categoryName: String,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<JunkFileAdapter.FileViewHolder>() {

    inner class FileViewHolder(
        private val binding: TssFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(junkFile: JunkFileModel, position: Int) {
            // 绑定文件名
            binding.tvFileName.text = junkFile.name
            
            // 设置选中状态图标
            binding.imgFileSelect.setImageResource(
                if (junkFile.isSelected) R.drawable.ic_selete_1
                else R.drawable.ic_disselete_2
            )
            
            // 点击整个项目切换选中状态
            binding.root.setOnClickListener {
                toggleSelection(junkFile, position)
            }
            
            // 点击选中图标切换选中状态
            binding.imgFileSelect.setOnClickListener {
                toggleSelection(junkFile, position)
            }
        }
        
        private fun toggleSelection(junkFile: JunkFileModel, position: Int) {
            junkFile.isSelected = !junkFile.isSelected
            notifyItemChanged(position)
            onSelectionChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding: TssFileBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.tss_file,
            parent,
            false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position], position)
    }

    override fun getItemCount(): Int = files.size
    
    /**
     * 获取分类名称
     */
    fun getCategoryName(): String = categoryName
    
    /**
     * 全选
     */
    fun selectAll() {
        files.forEach { it.isSelected = true }
        notifyDataSetChanged()
    }
    
    /**
     * 取消全选
     */
    fun deselectAll() {
        files.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }
    
    /**
     * 获取选中的文件
     */
    fun getSelectedFiles(): List<JunkFileModel> {
        return files.filter { it.isSelected }
    }
}

