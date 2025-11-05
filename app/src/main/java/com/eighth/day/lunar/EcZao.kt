package com.eighth.day.lunar

import android.app.Dialog
import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eighth.day.lunar.databinding.DialogDeleteBinding
import com.eighth.day.lunar.databinding.EcZaoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class EcZao : AppCompatActivity() {
    private val binding by lazy {
        EcZaoBinding.inflate(layoutInflater)
    }

    private var loadingDialog: Dialog? = null
    private lateinit var fileAdapter: FileAdapter
    private val allFiles = mutableListOf<FileItem>()
    private val filteredFiles = mutableListOf<FileItem>()
    
    // 筛选器状态
    private var currentTypeFilter = FileTypeFilter.ALL
    private var currentSizeFilter = FileSizeFilter.ALL
    private var currentTimeFilter = FileTimeFilter.ALL
    
    private var filesToDelete: List<FileItem>? = null
    private var totalDeleteSize: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.file)) { v, insets ->
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
        
        // 加载文件
        loadFiles()
    }

    private fun showLoadingDialog() {
        loadingDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_load)
            setCancelable(false)
            
            window?.apply {
                setLayout(
                    android.view.WindowManager.LayoutParams.MATCH_PARENT,
                    android.view.WindowManager.LayoutParams.MATCH_PARENT
                )
                setBackgroundDrawableResource(android.R.color.transparent)
            }
            
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            val imgLoadLogo = findViewById<ImageView>(R.id.img_load_logo)
            
            // 设置文件功能图标
            imgLoadLogo.setImageResource(R.drawable.ic_file_logo)
            
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
        fileAdapter = FileAdapter(filteredFiles) {
            updateDeleteButtonState()
        }
        
        binding.rvFiles.apply {
            layoutManager = LinearLayoutManager(this@EcZao)
            adapter = fileAdapter
        }
    }

    private fun setupClickListeners() {
        // 返回按钮
        binding.imgBack.setOnClickListener {
            finish()
        }
        
        // 类型筛选
        binding.tvType.setOnClickListener {
            showTypeFilterMenu(it)
        }
        
        // 大小筛选
        binding.tvSize.setOnClickListener {
            showSizeFilterMenu(it)
        }
        
        // 时间筛选
        binding.tvTime.setOnClickListener {
            showTimeFilterMenu(it)
        }
        
        // 删除按钮
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    /**
     * 加载文件列表
     */
    private fun loadFiles() {
        lifecycleScope.launch {
            val files = withContext(Dispatchers.IO) {
                scanAllFiles()
            }
            
            allFiles.clear()
            allFiles.addAll(files)
            applyFilters()
        }
    }

    /**
     * 扫描所有文件
     */
    private fun scanAllFiles(): List<FileItem> {
        val files = mutableListOf<FileItem>()
        val seenPaths = mutableSetOf<String>()
        
        // 查询外部存储的文件
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )
        
        // 只查询文件，不查询目录
        val selection = "${MediaStore.Files.FileColumns.SIZE} > 0"
        val sortOrder = "${MediaStore.Files.FileColumns.SIZE} DESC"
        
        try {
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val modifiedColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                
                while (it.moveToNext() && files.size < 5000) { // 限制最多5000个文件防止内存问题
                    try {
                        val id = it.getLong(idColumn)
                        val name = it.getString(nameColumn) ?: continue
                        val path = it.getString(dataColumn) ?: continue
                        val size = it.getLong(sizeColumn)
                        val modified = it.getLong(modifiedColumn)
                        val mimeType = it.getString(mimeColumn)
                        
                        // 去重
                        if (seenPaths.contains(path)) continue
                        seenPaths.add(path)
                        
                        // 过滤掉系统文件和隐藏文件
                        if (name.startsWith(".") || path.contains("/.")) continue
                        
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Files.getContentUri("external"),
                            id
                        )
                        
                        files.add(
                            FileItem(
                                id = id,
                                name = name,
                                path = path,
                                uri = contentUri,
                                size = size,
                                lastModified = modified * 1000, // 转换为毫秒
                                mimeType = mimeType
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return files
    }

    /**
     * 应用所有筛选器
     */
    private fun applyFilters() {
        lifecycleScope.launch(Dispatchers.Default) {
            val currentTime = System.currentTimeMillis()
            
            val filtered = allFiles.filter { file ->
                // 类型筛选
                val typeMatch = when (currentTypeFilter) {
                    FileTypeFilter.ALL -> true
                    FileTypeFilter.IMAGE -> file.getFileType() == FileType.IMAGE
                    FileTypeFilter.VIDEO -> file.getFileType() == FileType.VIDEO
                    FileTypeFilter.AUDIO -> file.getFileType() == FileType.AUDIO
                    FileTypeFilter.DOCS -> file.getFileType() == FileType.DOCS
                    FileTypeFilter.PPT -> file.getFileType() == FileType.PPT
                    FileTypeFilter.EXCEL -> file.getFileType() == FileType.EXCEL
                    FileTypeFilter.OTHER -> file.getFileType() == FileType.OTHER
                }
                
                // 大小筛选
                val sizeMatch = file.size >= currentSizeFilter.minSize
                
                // 时间筛选
                val timeMatch = if (currentTimeFilter == FileTimeFilter.ALL) {
                    true
                } else {
                    val daysAgoMillis = TimeUnit.DAYS.toMillis(currentTimeFilter.daysAgo.toLong())
                    val cutoffTime = currentTime - daysAgoMillis
                    file.lastModified >= cutoffTime
                }
                
                typeMatch && sizeMatch && timeMatch
            }
            
            withContext(Dispatchers.Main) {
                filteredFiles.clear()
                filteredFiles.addAll(filtered)
                fileAdapter.updateFiles(filtered)
                updateEmptyState()
            }
        }
    }

    /**
     * 显示类型筛选菜单
     */
    private fun showTypeFilterMenu(anchor: View) {
        val options = FileTypeFilter.values().map { it.displayName }
        showFilterPopup(anchor, options) { selected ->
            currentTypeFilter = FileTypeFilter.values()[selected]
            binding.tvType.text = currentTypeFilter.displayName
            applyFilters()
        }
    }

    /**
     * 显示大小筛选菜单
     */
    private fun showSizeFilterMenu(anchor: View) {
        val options = FileSizeFilter.values().map { it.displayName }
        showFilterPopup(anchor, options) { selected ->
            currentSizeFilter = FileSizeFilter.values()[selected]
            binding.tvSize.text = currentSizeFilter.displayName
            applyFilters()
        }
    }

    /**
     * 显示时间筛选菜单
     */
    private fun showTimeFilterMenu(anchor: View) {
        val options = FileTimeFilter.values().map { it.displayName }
        showFilterPopup(anchor, options) { selected ->
            currentTimeFilter = FileTimeFilter.values()[selected]
            binding.tvTime.text = currentTimeFilter.displayName
            applyFilters()
        }
    }

    /**
     * 显示筛选弹窗
     */
    private fun showFilterPopup(anchor: View, options: List<String>, onSelected: (Int) -> Unit) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_filter_menu, null)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.rv_filter_items)
        
        val popupWindow = PopupWindow(
            popupView,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FilterOptionAdapter(options) { position ->
            onSelected(position)
            popupWindow.dismiss()
        }
        
        popupWindow.showAsDropDown(anchor, 0, 10)
    }

    /**
     * 更新空状态显示
     */
    private fun updateEmptyState() {
        if (filteredFiles.isEmpty()) {
            binding.tvNodata.visibility = View.VISIBLE
            binding.rvFiles.visibility = View.GONE
        } else {
            binding.tvNodata.visibility = View.GONE
            binding.rvFiles.visibility = View.VISIBLE
        }
    }

    /**
     * 更新删除按钮状态
     */
    private fun updateDeleteButtonState() {
        val selectedCount = fileAdapter.getSelectedFiles().size
        binding.btnDelete.isEnabled = selectedCount > 0
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog() {
        val selectedFiles = fileAdapter.getSelectedFiles()
        
        if (selectedFiles.isEmpty()) {
            return
        }
        
        val dialogBinding = DialogDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // 修改文案为文件删除相关
        dialogBinding.tvDeleteTitle.text = "Delete Files"
        dialogBinding.tvDeleteMessage.text = "Are you sure you want to\ndelete selected files?"
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnDelete.setOnClickListener {
            dialog.dismiss()
            deleteSelectedFiles(selectedFiles)
        }
        
        dialog.show()
    }

    /**
     * 删除选中的文件
     */
    private fun deleteSelectedFiles(files: List<FileItem>) {
        if (files.isEmpty()) {
            Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        filesToDelete = files
        totalDeleteSize = files.sumOf { it.size }.toDouble()
        
        // Android 10+ 需要使用 MediaStore 删除请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteFilesModern(files)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deleteFilesAndroid10(files)
        } else {
            deleteFilesLegacy(files)
        }
    }


    private fun deleteFilesModern(files: List<FileItem>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            lifecycleScope.launch {
                val (deleted, failed) = deleteFilesDirectly(files)
                showDeleteResult(deleted, failed, files.size)
            }
        }
    }
    

    private suspend fun deleteFilesDirectly(files: List<FileItem>): Pair<Int, Int> {
        return withContext(Dispatchers.IO) {
            var deleted = 0
            var failed = 0
            
            files.forEachIndexed { index, file ->
                try {
                    val javaFile = java.io.File(file.path)
                    
                    if (javaFile.exists()) {
                        val deleteSuccess = javaFile.delete()
                        
                        if (deleteSuccess) {
                            try {
                                contentResolver.delete(file.uri, null, null)
                            } catch (e: Exception) {
                                // 忽略MediaStore清理失败
                            }
                            deleted++
                        } else {
                            try {
                                val rows = contentResolver.delete(file.uri, null, null)
                                if (rows > 0) {
                                    deleted++
                                } else {
                                    failed++
                                }
                            } catch (e: Exception) {
                                failed++
                            }
                        }
                    } else {
                        // 文件不存在，清理MediaStore记录
                        try {
                            contentResolver.delete(file.uri, null, null)
                        } catch (e: Exception) {
                            // 忽略
                        }
                        deleted++
                    }
                } catch (e: Exception) {
                    failed++
                }
            }
            Pair(deleted, failed)
        }
    }
    

    private fun showDeleteResult(deleted: Int, failed: Int, total: Int) {

        if (failed == 0 && deleted > 0) {
            // 全部删除成功
            navigateToShowEnd(totalDeleteSize)
        } else if (deleted > 0 && failed > 0) {
            // 部分删除成功
            Toast.makeText(this, "Deleted: $deleted, Failed: $failed", Toast.LENGTH_LONG).show()
            val deletedSize = totalDeleteSize * deleted / total
            navigateToShowEnd(deletedSize)
        } else if (deleted == 0 && failed > 0) {
            // 全部失败
            Toast.makeText(this, "Delete failed. Deleted: 0, Failed: $failed", Toast.LENGTH_LONG).show()
        } else {
            // 没有文件需要删除
            Toast.makeText(this, "No files to delete", Toast.LENGTH_SHORT).show()
        }
    }


    private fun deleteFilesAndroid10(files: List<FileItem>) {
        lifecycleScope.launch {
            var deletedCount = 0
            var failedCount = 0
            
            withContext(Dispatchers.IO) {
                files.forEach { file ->
                    try {
                        val deleted = contentResolver.delete(file.uri, null, null)
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
                Toast.makeText(this@EcZao, message, Toast.LENGTH_LONG).show()
                
                if (deletedCount > 0) {
                    navigateToShowEnd(totalDeleteSize * deletedCount / files.size)
                }
            }
        }
    }


    private fun deleteFilesLegacy(files: List<FileItem>) {
        lifecycleScope.launch {
            var deletedCount = 0
            
            withContext(Dispatchers.IO) {
                files.forEach { file ->
                    try {
                        val deleted = contentResolver.delete(file.uri, null, null)
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
                Toast.makeText(this@EcZao, "Delete failed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun navigateToShowEnd(size: Double) {
        val intent = Intent(this@EcZao, ShowEnd::class.java).apply {
            putExtra("cleanedSize", size)
            putExtra("pageType", "file")
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
    }
}


class FilterOptionAdapter(
    private val options: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<FilterOptionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv_option)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = options[position]
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = options.size
}
