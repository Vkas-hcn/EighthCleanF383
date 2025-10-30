package com.eighth.day.lunar

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.eighth.day.lunar.databinding.ShowEndBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShowEnd : AppCompatActivity() {
    private val binding by lazy {
        ShowEndBinding.inflate(layoutInflater)
    }
    
    private var loadingDialog: Dialog? = null
    private var cleanedSize: Double = 0.0
    private var pageType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.show_end)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 获取传递的数据
        cleanedSize = intent.getDoubleExtra("cleanedSize", 0.0)
        pageType = intent.getStringExtra("pageType") ?: ""
        
        // 显示加载对话框
        showLoadingDialog()
        
        // 设置点击事件
        setupClickListeners()
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
            val tvLoadingText = findViewById<TextView>(R.id.tv_loading_text)
            val tvTitle = findViewById<TextView>(R.id.tv_title)
            // 根据页面类型设置图标
            imgLoadLogo.setImageResource(R.drawable.ic_pic_logo)
            
            // 设置文案为 "Cleaning"
            tvLoadingText.text = "Cleaning"
            tvTitle.text = "Clean"
            show()
            
            // 更新进度条
            lifecycleScope.launch {
                for (i in 0..100) {
                    progressBar.progress = i
                    delay(10)
                }
                delay(100)
                dismiss()
                displayResults()
            }
        }
    }
    
    private fun displayResults() {
        // 格式化显示清理大小
        val (size, unit) = formatFileSize(cleanedSize)
        binding.tvCleanedSize.text = "Saved ${ String.format("%.1f", size)}${unit} Space For You"
    }
    
    private fun formatFileSize(sizeInBytes: Double): Pair<Double, String> {
        return when {
            sizeInBytes < 1000 -> Pair(sizeInBytes, "B")
            sizeInBytes < 1000 * 1000 -> Pair(sizeInBytes / 1000, "KB")
            sizeInBytes < 1000 * 1000 * 1000 -> Pair(sizeInBytes / (1000 * 1000), "MB")
            else -> Pair(sizeInBytes / (1000 * 1000 * 1000), "GB")
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.cardFileClean.setOnClickListener {
            startActivity(Intent(this, EcZao::class.java))
            finish()
        }
        
        binding.cardPictureClean.setOnClickListener {
            startActivity(Intent(this, EcHan::class.java))
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        loadingDialog?.dismiss()
    }
}

