package com.eighth.day.lunar.wei.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eighth.day.lunar.R
import com.eighth.day.lunar.databinding.ItemCategoryBinding
import com.eighth.day.lunar.wei.callback.SelectionCallback
import com.eighth.day.lunar.wei.callback.UIInteractionCallback
import com.eighth.day.lunar.wei.model.JunkCategoryModel


class JunkCategoryAdapter(
    private val categories: MutableList<JunkCategoryModel>,
    private val selectionCallback: SelectionCallback? = null,
    private val uiCallback: UIInteractionCallback? = null
) : RecyclerView.Adapter<JunkCategoryAdapter.CategoryViewHolder>() {
    
    // 共享的 ViewHolder 池，提高嵌套 RecyclerView 的性能
    private val sharedViewPool = RecyclerView.RecycledViewPool()

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(category: JunkCategoryModel, position: Int) {
            // 绑定基本信息
            binding.tvTitle.text = category.displayName
            binding.imageView2.setImageResource(category.iconRes)
            
            // 更新选中状态
            category.updateSelectionState()
            
            // 设置选中图标
            binding.imgSelect.setImageResource(
                if (category.isSelected) R.drawable.ic_selete_1
                else R.drawable.ic_disselete_2
            )
            
            // 设置文件列表
            setupFileList(category, position)
            
            // 设置点击事件
            setupClickListeners(category, position)
        }
        

        private fun setupFileList(category: JunkCategoryModel, position: Int) {
            val recyclerView = binding.rvItemFile
            
            // 每次都创建新的 FileAdapter，确保数据正确
            val fileAdapter = JunkFileAdapter(
                files = category.files,
                categoryName = category.displayName,
                onSelectionChanged = {
                    category.updateSelectionState()
                    notifyItemChanged(position)
                    notifySelectionChanged()
                }
            )
            
            // 初始化 RecyclerView（只在首次需要）
            if (recyclerView.layoutManager == null) {
                recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
                // 使用共享 ViewPool，提高嵌套 RecyclerView 的性能
                recyclerView.setRecycledViewPool(sharedViewPool)
                // 优化嵌套滚动
                recyclerView.isNestedScrollingEnabled = false
            }
            
            // ✅ 关键修复：每次都强制设置 adapter，避免复用导致的数据错乱
            recyclerView.adapter = fileAdapter
            
            // 控制展开/收起
            recyclerView.visibility = if (category.isExpanded) View.VISIBLE else View.GONE
        }
        
        private fun setupClickListeners(category: JunkCategoryModel, position: Int) {
            // 分类展开/收起
            binding.llCategory.setOnClickListener {
                category.isExpanded = !category.isExpanded
                binding.rvItemFile.visibility = if (category.isExpanded) View.VISIBLE else View.GONE
                notifyItemChanged(position)
                
                uiCallback?.onCategoryExpandChanged(category.displayName, category.isExpanded)
            }
            
            // 选中/取消选中整个分类
            binding.imgSelect.setOnClickListener {
                category.toggleSelectAll()
                
                // 通过 RecyclerView 获取当前的 adapter 并刷新
                (binding.rvItemFile.adapter as? JunkFileAdapter)?.notifyDataSetChanged()
                
                notifyItemChanged(position)
                notifySelectionChanged()
                
                selectionCallback?.onCategorySelectionChanged(
                    category.displayName,
                    category.isSelected
                )
            }
        }
        
        private fun notifySelectionChanged() {
            val selectedCount = categories.sumOf { it.getSelectedCount() }
            val totalCount = categories.sumOf { it.getFileCount() }
            val selectedSize = categories.sumOf { it.getSelectedSize() }
            
            selectionCallback?.onSelectionChanged(selectedCount, totalCount, selectedSize)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding: ItemCategoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_category,
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size
    
    /**
     * 更新数据
     */
    fun updateCategories(newCategories: List<JunkCategoryModel>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }
    
    /**
     * 获取选中文件总数
     */
    fun getSelectedFileCount(): Int {
        return categories.sumOf { it.getSelectedCount() }
    }
    
    /**
     * 获取选中文件总大小
     */
    fun getSelectedFileSize(): Long {
        return categories.sumOf { it.getSelectedSize() }
    }
}

