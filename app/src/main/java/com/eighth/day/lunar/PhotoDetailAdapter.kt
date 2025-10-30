package com.eighth.day.lunar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eighth.day.lunar.databinding.ItemPicDetailBinding

/**
 * 单个日期下的照片列表适配器
 */
class PhotoDetailAdapter(
    private val photos: MutableList<PhotoItem>,
    private val onPhotoClick: (PhotoItem) -> Unit
) : RecyclerView.Adapter<PhotoDetailAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(private val binding: ItemPicDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: PhotoItem) {
            // 加载图片
            Glide.with(binding.imgPic.context.applicationContext)
                .load(photo.uri)
                .centerCrop()
                .into(binding.imgPic)

            // 设置选中状态
            binding.imgCheck.setImageResource(
                if (photo.isSelected) R.drawable.ic_selete_1 else R.drawable.ic_disselete_1
            )

            // 点击事件
            binding.root.setOnClickListener {
                photo.isSelected = !photo.isSelected
                notifyItemChanged(bindingAdapterPosition)
                onPhotoClick(photo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPicDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    fun updateSelection(selectAll: Boolean) {
        photos.forEach { it.isSelected = selectAll }
        notifyDataSetChanged()
    }
}

