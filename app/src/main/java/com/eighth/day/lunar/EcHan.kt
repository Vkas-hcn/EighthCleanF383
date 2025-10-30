package com.eighth.day.lunar

import android.app.Dialog
import android.content.ContentUris
import android.content.Intent
import android.content.IntentSender
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eighth.day.lunar.databinding.DialogDeleteBinding
import com.eighth.day.lunar.databinding.EcHanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EcHan : AppCompatActivity() {
    private val binding by lazy {
        EcHanBinding.inflate(layoutInflater)
    }

    private var loadingDialog: Dialog? = null
    private lateinit var photoGroupAdapter: PhotoGroupAdapter
    private val photoGroups = mutableListOf<PhotoGroup>()
    private var photosToDelete: List<PhotoItem>? = null
    private var totalDeleteSize: Double = 0.0
    
    // Android 10+ 删除请求启动器
    private val deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest> = 
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 用户同意删除，跳转到结果页
                navigateToShowEnd(totalDeleteSize)
            } else {
                // 用户拒绝删除
                Toast.makeText(this, "Delete cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.han)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 显示加载对话框
        showLoadingDialog()
        
        // 初始化RecyclerView
        setupRecyclerView()
        
        // 设置点击事件
        setupClickListeners()
        
        // 加载照片
        loadPhotos()
    }

    private fun showLoadingDialog() {
        loadingDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_load)
            setCancelable(false)
            
            // 设置窗口为全屏
            window?.apply {
                setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            val imgLoadLogo = findViewById<ImageView>(R.id.img_load_logo)
            
            // 设置图标
            imgLoadLogo.setImageResource(R.drawable.ic_pic_logo)
            
            show()
            
            // 更新进度条
            lifecycleScope.launch {
                for (i in 0..100) {
                    progressBar.progress = i
                    delay(10)
                }
                delay(100)
                dismiss()
            }
        }
    }

    private fun setupRecyclerView() {
        photoGroupAdapter = PhotoGroupAdapter(photoGroups) {
            updateSelectedSize()
        }
        
        binding.rvPic.apply {
            layoutManager = LinearLayoutManager(this@EcHan)
            adapter = photoGroupAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun loadPhotos() {
        lifecycleScope.launch {
            val photos = withContext(Dispatchers.IO) {
                getAllPhotos()
            }
            
            // 按日期分组
            val grouped = groupPhotosByDate(photos)
            photoGroups.clear()
            photoGroups.addAll(grouped)
            photoGroupAdapter.notifyDataSetChanged()
        }
    }

    private fun getAllPhotos(): List<PhotoItem> {
        val photos = mutableListOf<PhotoItem>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val path = it.getString(dataColumn)
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                photos.add(
                    PhotoItem(
                        id = id,
                        uri = contentUri,
                        path = path,
                        size = size,
                        dateAdded = dateAdded
                    )
                )
            }
        }
        
        return photos
    }

    private fun groupPhotosByDate(photos: List<PhotoItem>): List<PhotoGroup> {
        val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH)
        val groups = mutableMapOf<String, MutableList<PhotoItem>>()
        
        photos.forEach { photo ->
            val date = Date(photo.dateAdded * 1000)
            val dateString = dateFormat.format(date)
            
            if (!groups.containsKey(dateString)) {
                groups[dateString] = mutableListOf()
            }
            groups[dateString]?.add(photo)
        }
        
        return groups.map { (date, photoList) ->
            PhotoGroup(date, photoList)
        }
    }

    private fun updateSelectedSize() {
        val selectedPhotos = photoGroupAdapter.getAllSelectedPhotos()
        val totalSize = selectedPhotos.sumOf { it.size }.toDouble()
        
        val (size, unit) = formatFileSize(totalSize)
        binding.tvPicSize.text = String.format("%.1f", size)
        binding.tvPicSizeUn.text = unit
    }

    private fun formatFileSize(sizeInBytes: Double): Pair<Double, String> {
        return when {
            sizeInBytes < 1024 -> Pair(sizeInBytes, "B")
            sizeInBytes < 1024 * 1024 -> Pair(sizeInBytes / 1024, "KB")
            sizeInBytes < 1024 * 1024 * 1024 -> Pair(sizeInBytes / (1024 * 1024), "MB")
            else -> Pair(sizeInBytes / (1024 * 1024 * 1024), "GB")
        }
    }

    private fun showDeleteConfirmDialog() {
        val selectedPhotos = photoGroupAdapter.getAllSelectedPhotos()
        
        if (selectedPhotos.isEmpty()) {
            return
        }
        
        val dialogBinding = DialogDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteSelectedPhotos(selectedPhotos)
        }
        
        dialog.show()
    }

    private fun deleteSelectedPhotos(photos: List<PhotoItem>) {
        if (photos.isEmpty()) {
            Toast.makeText(this, "No photos selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        photosToDelete = photos
        totalDeleteSize = photos.sumOf { it.size }.toDouble()
        
        // Android 10+ (API 29+) 需要使用 MediaStore 删除请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            deletePhotosModern(photos)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29)
            deletePhotosAndroid10(photos)
        } else {
            // Android 9 及以下
            deletePhotosLegacy(photos)
        }
    }
    
    /**
     * Android 11+ (API 30+) 删除方式
     */
    private fun deletePhotosModern(photos: List<PhotoItem>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val uris = photos.map { it.uri }
            val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris)
            
            try {
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                deleteRequestLauncher.launch(intentSenderRequest)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Android 10 (API 29) 删除方式
     */
    private fun deletePhotosAndroid10(photos: List<PhotoItem>) {
        lifecycleScope.launch {
            var deletedCount = 0
            var failedCount = 0
            
            withContext(Dispatchers.IO) {
                photos.forEach { photo ->
                    try {
                        val deleted = contentResolver.delete(photo.uri, null, null)
                        if (deleted > 0) {
                            deletedCount++
                        } else {
                            failedCount++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        failedCount++
                    }
                }
            }
            
            if (failedCount == 0) {
                navigateToShowEnd(totalDeleteSize)
            } else {
                val message = if (deletedCount > 0) {
                    "Deleted: $deletedCount, Failed: $failedCount"
                } else {
                    "Delete failed. Please check permissions."
                }
                Toast.makeText(this@EcHan, message, Toast.LENGTH_LONG).show()
                
                if (deletedCount > 0) {
                    // 部分删除成功也跳转
                    navigateToShowEnd(totalDeleteSize * deletedCount / photos.size)
                }
            }
        }
    }
    
    /**
     * Android 9 及以下删除方式
     */
    private fun deletePhotosLegacy(photos: List<PhotoItem>) {
        lifecycleScope.launch {
            var deletedCount = 0
            
            withContext(Dispatchers.IO) {
                photos.forEach { photo ->
                    try {
                        val deleted = contentResolver.delete(photo.uri, null, null)
                        if (deleted > 0) {
                            deletedCount++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            if (deletedCount > 0) {
                navigateToShowEnd(totalDeleteSize)
            } else {
                Toast.makeText(this@EcHan, "Delete failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 跳转到结果页面
     */
    private fun navigateToShowEnd(size: Double) {
        val intent = Intent(this@EcHan, ShowEnd::class.java).apply {
            putExtra("cleanedSize", size)
            putExtra("pageType", "photo")
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
    }
}
