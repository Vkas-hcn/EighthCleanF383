package com.eighth.day.lunar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eighth.day.lunar.databinding.ItemPicFlBinding

/**
 * 照片分组适配器（按日期分组）
 */
class PhotoGroupAdapter(
    private val photoGroups: MutableList<PhotoGroup>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<PhotoGroupAdapter.PhotoGroupViewHolder>() {

    inner class PhotoGroupViewHolder(private val binding: ItemPicFlBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoGroup: PhotoGroup) {
            // 设置日期
            binding.tvPicDate.text = photoGroup.date

            // 计算并显示分组文件大小
            val totalSize = photoGroup.photos.sumOf { it.size }.toDouble()
            val (size, unit) = formatFileSize(totalSize)
            binding.tvGroupSize.text = String.format("%.1f %s", size, unit)

            // 设置全选图标
            updateSelectAllIcon(photoGroup)

            // 设置照片列表
            val photoAdapter = PhotoDetailAdapter(photoGroup.photos) {
                // 单个照片选中状态改变时的回调
                updateSelectAllIcon(photoGroup)
                onSelectionChanged()
            }

            binding.rvPicDetail.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = photoAdapter
            }

            // 全选按钮点击事件
            binding.imgCaCheck.setOnClickListener {
                photoGroup.isAllSelected = !photoGroup.isAllSelected
                photoGroup.photos.forEach { it.isSelected = photoGroup.isAllSelected }
                photoAdapter.updateSelection(photoGroup.isAllSelected)
                updateSelectAllIcon(photoGroup)
                onSelectionChanged()
            }
        }
        
        private fun formatFileSize(sizeInBytes: Double): Pair<Double, String> {
            return when {
                sizeInBytes < 1024 -> Pair(sizeInBytes, "B")
                sizeInBytes < 1024 * 1024 -> Pair(sizeInBytes / 1024, "KB")
                sizeInBytes < 1024 * 1024 * 1024 -> Pair(sizeInBytes / (1024 * 1024), "MB")
                else -> Pair(sizeInBytes / (1024 * 1024 * 1024), "GB")
            }
        }

        private fun updateSelectAllIcon(photoGroup: PhotoGroup) {
            val allSelected = photoGroup.photos.all { it.isSelected } && photoGroup.photos.isNotEmpty()
            photoGroup.isAllSelected = allSelected
            binding.imgCaCheck.setImageResource(
                if (allSelected) R.drawable.ic_selete_2 else R.drawable.ic_disselete_1
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoGroupViewHolder {
        val binding = ItemPicFlBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoGroupViewHolder, position: Int) {
        holder.bind(photoGroups[position])
    }

    override fun getItemCount(): Int = photoGroups.size

    fun getAllSelectedPhotos(): List<PhotoItem> {
        return photoGroups.flatMap { group ->
            group.photos.filter { it.isSelected }
        }
    }
}

