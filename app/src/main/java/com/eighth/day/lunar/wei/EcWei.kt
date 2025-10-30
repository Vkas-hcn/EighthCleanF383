package com.eighth.day.lunar.wei

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eighth.day.lunar.R
import com.eighth.day.lunar.ShowEnd
import com.eighth.day.lunar.databinding.EcWeiBinding
import com.eighth.day.lunar.wei.adapter.JunkCategoryAdapter
import com.eighth.day.lunar.wei.callback.ScanCallback
import com.eighth.day.lunar.wei.callback.SelectionCallback
import com.eighth.day.lunar.wei.callback.UIInteractionCallback
import com.eighth.day.lunar.wei.model.JunkCategoryModel
import com.eighth.day.lunar.wei.model.JunkCategoryType
import com.eighth.day.lunar.wei.model.JunkFileModel
import com.eighth.day.lunar.wei.scanner.JunkScannerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EcWei : AppCompatActivity(), 
    ScanCallback,
    SelectionCallback,
    UIInteractionCallback {
    
    // ========== 视图绑定 ==========
    private val binding by lazy { EcWeiBinding.inflate(layoutInflater) }
    
    // ========== 核心组件 ==========
    private lateinit var scannerManager: JunkScannerManager
    private lateinit var categoryAdapter: JunkCategoryAdapter
    
    // ========== 数据模型 ==========
    private val categories = mutableListOf<JunkCategoryModel>()
    private var totalJunkSize: Long = 0
    private var isScanning = false

    // ========== 生命周期 ==========
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        applySystemInsets()
        
        initializeComponents()
        setupUI()
        startScanning()
    }
    
    private fun applySystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.wei)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    // ========== 初始化 ==========
    
    /**
     * 初始化核心组件
     */
    private fun initializeComponents() {
        // 初始化扫描管理器
        scannerManager = JunkScannerManager(this)
        
        // 初始化分类数据
        initializeCategories()
        
        // 初始化适配器
        categoryAdapter = JunkCategoryAdapter(
            categories = categories,
            selectionCallback = this,
            uiCallback = this
        )
    }
    
    /**
     * 初始化分类数据
     */
    private fun initializeCategories() {
        JunkCategoryType.getAllTypes().forEach { type ->
            categories.add(JunkCategoryModel(type))
        }
    }
    
    /**
     * 设置 UI
     */
    private fun setupUI() {
        // 设置返回按钮
        binding.btnBack.setOnClickListener { finish() }
        
        // 设置 RecyclerView
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@EcWei)
            adapter = categoryAdapter
        }
        
        // 设置清理按钮
        binding.btnCleanNow.setOnClickListener {
            performClean()
        }
    }
    
    // ========== 扫描逻辑 ==========
    
    /**
     * 开始扫描
     */
    private fun startScanning() {
        if (isScanning) return
        
        isScanning = true
        binding.tvTitle.text = "Scanning"
        
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                scannerManager.scanAll(this@EcWei)
            }
            
            // 更新分类数据
            updateCategoriesWithResults(results)
        }
    }
    
    /**
     * 更新分类数据
     */
    private fun updateCategoriesWithResults(
        results: Map<JunkCategoryType, JunkCategoryModel>
    ) {
        results.forEach { (type, categoryModel) ->
            categories.find { it.type == type }?.let { existing ->
                existing.files.clear()
                existing.files.addAll(categoryModel.files)
            }
        }
    }
    
    // ========== ScanCallback 实现 ==========
    
    override fun onScanStarted(totalCategories: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.tvScanningPath.text = "Starting scan..."
        }
    }
    
    override fun onScanProgress(currentPath: String, categoryName: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.tvScanningPath.text = currentPath
        }
    }
    
    override fun onFileFound(file: JunkFileModel, categoryName: String) {
        // 文件已在扫描器中添加到分类
    }
    
    override fun onSizeChanged(totalSize: Long) {
        totalJunkSize = totalSize
        lifecycleScope.launch(Dispatchers.Main) {
            updateSizeDisplay()
        }
    }
    
    override fun onScanCompleted(totalFiles: Int, totalSize: Long) {
        isScanning = false
        
        lifecycleScope.launch(Dispatchers.Main) {
            binding.tvTitle.text = "Scan Complete"
            binding.tvScanningPath.text = "Scan completed - $totalFiles files found"
            
            // 如果找到垃圾文件，更改背景
            if (totalSize > 0) {
                binding.wei.setBackgroundResource(R.drawable.bg_junk)
                binding.imgScanBg.setImageResource(R.drawable.icon_junk)
                binding.btnCleanNow.visibility = View.VISIBLE
                binding.btnCleanNow.isEnabled = true
            }
            
            // 刷新列表
            categoryAdapter.notifyDataSetChanged()
        }
    }
    
    override fun onScanError(error: Throwable) {
        lifecycleScope.launch(Dispatchers.Main) {
            error.printStackTrace()
        }
    }
    

    override fun onSelectionChanged(
        selectedCount: Int,
        totalCount: Int,
        selectedSize: Long
    ) {
        binding.btnCleanNow.isEnabled = selectedCount > 0
    }
    
    override fun onCategorySelectionChanged(categoryName: String, isSelected: Boolean) {
        // 分类选择改变的处理
    }
    

    override fun onCategoryExpandChanged(categoryName: String, isExpanded: Boolean) {
        // 分类展开/收起的处理
    }
    
    override fun onCategoryClicked(categoryName: String) {
        // 分类点击的处理
    }
    
    override fun onFileClicked(file: JunkFileModel) {
        // 文件点击的处理
    }
    
    // ========== 清理逻辑 ==========
    
    /**
     * 执行清理
     */
    private fun performClean() {
        val selectedFiles = categories.flatMap { category ->
            category.files.filter { it.isSelected }
        }
        
        if (selectedFiles.isEmpty()) return
        
        val totalSize = selectedFiles.sumOf { it.size }
        
        lifecycleScope.launch(Dispatchers.IO) {
            var deletedSize = 0L
            var deletedCount = 0
            var failedCount = 0
            
            selectedFiles.forEach { file ->
                if (file.delete()) {
                    deletedSize += file.size
                    deletedCount++
                } else {
                    failedCount++
                }
            }
            
            // 跳转到结果页面
            withContext(Dispatchers.Main) {
                navigateToResult(deletedSize)
            }
        }
    }
    
    /**
     * 跳转到结果页面
     */
    private fun navigateToResult(cleanedSize: Long) {
        val intent = Intent(this@EcWei, ShowEnd::class.java).apply {
            putExtra("cleanedSize", cleanedSize.toDouble())
            putExtra("pageType", "junk")
        }
        startActivity(intent)
        finish()
    }
    
    // ========== UI 更新 ==========
    
    /**
     * 更新大小显示
     */
    private fun updateSizeDisplay() {
        val (size, unit) = formatFileSize(totalJunkSize.toDouble())
        binding.tvScannedSize.text = String.format("%.1f", size)
        binding.tvScannedSizeUn.text = unit
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(sizeInBytes: Double): Pair<Double, String> {
        return when {
            sizeInBytes < 1000 -> Pair(sizeInBytes, "B")
            sizeInBytes < 1000 * 1000 -> Pair(sizeInBytes / 1000, "KB")
            sizeInBytes < 1000 * 1000 * 1000 -> Pair(sizeInBytes / (1000 * 1000), "MB")
            else -> Pair(sizeInBytes / (1000 * 1000 * 1000), "GB")
        }
    }
}
