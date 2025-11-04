package com.eighth.day.lunar

import android.Manifest
import android.app.usage.StorageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.eighth.day.lunar.wei.EcWei
import kotlin.math.max

class EcChu : AppCompatActivity() {

    private lateinit var tvFreeStorage: TextView
    private lateinit var tvFreeUn: TextView
    private lateinit var tvUsedStorage: TextView
    private lateinit var tvUsedUn: TextView
    private lateinit var tvStorageUsed: TextView
    private lateinit var tvStorageUsedUn: TextView
    private lateinit var tvStorageTotal: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardPictureClean: CardView
    private lateinit var cardFileClean: CardView
    private lateinit var ivSettings: ImageView
    private lateinit var permissionRequestLayout: View
    
    private var requestedPermissions: Array<String>? = null
    private var isShowingSettingsGuide = false

    // 权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            executePendingAction()
        } else {
            // 检查是否是永久拒绝
            val permanentlyDenied = requestedPermissions?.any { permission ->
                !ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            } ?: false
            
            if (permanentlyDenied) {
                // 用户选择了"不再询问"，引导用户去设置页面
                showSettingsDialog()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                pendingAction = null
            }
        }
    }
    
    // 应用设置页返回启动器
    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 从设置页返回后，重新检查权限
        checkAndRequestPermission()
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                executePendingAction()
            } else {
                showSettingsDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.ec_chu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        setupClickListeners()
        loadStorageInfo()
    }

    private fun initViews() {
        tvFreeStorage = findViewById(R.id.tvFreeStorage)
        tvFreeUn = findViewById(R.id.tvFreeUn)
        tvUsedStorage = findViewById(R.id.tvUsedStorage)
        tvUsedUn = findViewById(R.id.tvUsedUn)
        tvStorageUsed = findViewById(R.id.tvStorageUsed)
        tvStorageUsedUn = findViewById(R.id.tvStorageUsedUn)
        tvStorageTotal = findViewById(R.id.tvStorageTotal)
        progressBar = findViewById(R.id.progressBar)
        cardPictureClean = findViewById(R.id.cardPictureClean)
        cardFileClean = findViewById(R.id.cardFileClean)
        ivSettings = findViewById(R.id.ivSettings)
        permissionRequestLayout = findViewById(R.id.includePermissionRequest)
        
        setupPermissionRequestView()
    }
    
    private fun setupPermissionRequestView() {
        val tvMessage = permissionRequestLayout.findViewById<TextView>(R.id.tvPermissionMessage)
        val btnCancel = permissionRequestLayout.findViewById<TextView>(R.id.btnPermissionCancel)
        val btnGrant = permissionRequestLayout.findViewById<TextView>(R.id.btnPermissionGrant)
        
        btnCancel.setOnClickListener {
            hidePermissionRequest()
            pendingAction = null
            isShowingSettingsGuide = false
        }
        
        btnGrant.setOnClickListener {
            hidePermissionRequest()
            if (isShowingSettingsGuide) {
                // 如果当前是设置引导模式，跳转到设置
                openAppSettings()
                isShowingSettingsGuide = false
            } else {
                // 正常权限请求流程
                requestActualPermission()
            }
        }
        
        // 点击背景关闭
        permissionRequestLayout.setOnClickListener {
            hidePermissionRequest()
            pendingAction = null
            isShowingSettingsGuide = false
        }
        
        // 防止点击穿透到下面的视图
        permissionRequestLayout.findViewById<View>(R.id.permissionRequestLayout)?.setOnClickListener {
            // 不做任何事，只是阻止点击穿透
        }
    }

    private var pendingAction: (() -> Unit)? = null

    private fun setupClickListeners() {
        // 设置按钮点击事件 - 跳转到设置页面
        ivSettings.setOnClickListener {
            startActivity(Intent(this, EcYan::class.java))
        }

        // Clean 按钮点击事件 - 跳转到 EcWei（需要权限）
        findViewById<TextView>(R.id.btn_clean).setOnClickListener {
            pendingAction = { startActivity(Intent(this, EcWei::class.java)) }
            checkAndRequestPermission()
        }

        // Picture Clean 卡片点击事件 - 跳转到 EcHan（需要权限）
        cardPictureClean.setOnClickListener {
            pendingAction = { startActivity(Intent(this, EcHan::class.java)) }
            checkAndRequestPermission()
        }

        // File Clean 卡片点击事件 - 跳转到 EcZao（需要权限）
        cardFileClean.setOnClickListener {
            pendingAction = { startActivity(Intent(this, EcZao::class.java)) }
            checkAndRequestPermission()
        }
    }

    /**
     * 加载存储信息
     */
    private fun loadStorageInfo() {
        try {
            val totalBytes = getTotalDeviceStorageAccurate()
            val freeBytes = getFreeDeviceStorage()
            val usedBytes = totalBytes - freeBytes

            // 计算进度百分比
            val percentage = if (totalBytes > 0) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()
            } else {
                0
            }

            // 更新UI
            updateStorageUI(freeBytes, usedBytes, totalBytes, percentage)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to obtain storage information", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 更新存储信息UI
     */
    private fun updateStorageUI(freeBytes: Long, usedBytes: Long, totalBytes: Long, percentage: Int) {
        // 格式化显示剩余存储
        val (freeValue, freeUnit) = formatStorage(freeBytes)
        tvFreeStorage.text = freeValue
        tvFreeUn.text = freeUnit

        // 格式化显示已用存储
        val (usedValue, usedUnit) = formatStorage(usedBytes)
        tvUsedStorage.text = usedValue
        tvUsedUn.text = usedUnit

        // 更新中心显示的已用存储
        tvStorageUsed.text = usedValue
        tvStorageUsedUn.text = usedUnit

        // 更新总存储显示
        val (totalValue, totalUnit) = formatStorage(totalBytes)
        tvStorageTotal.text = "/$totalValue$totalUnit"

        // 更新进度条
        progressBar.progress = percentage
    }

    /**
     * 格式化存储大小
     * @return Pair<显示值, 单位>
     */
    private fun formatStorage(bytes: Long): Pair<String, String> {
        val gb = bytes / (1000.0 * 1000.0 * 1000.0)
        val mb = bytes / (1000.0 * 1000.0)

        return if (gb >= 1.0) {
            Pair(String.format("%.1f", gb), "GB")
        } else {
            Pair(String.format("%.0f", mb), "MB")
        }
    }

    /**
     * 获取设备总存储空间
     */
    private fun getTotalDeviceStorageAccurate(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager =
                    getSystemService(STORAGE_STATS_SERVICE) as StorageStatsManager
                return storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            }

            val internalStat = StatFs(Environment.getDataDirectory().path)
            val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong

            val storagePaths = arrayOf(
                Environment.getRootDirectory().absolutePath,      // /system
                Environment.getDataDirectory().absolutePath,      // /data
                Environment.getDownloadCacheDirectory().absolutePath // /cache
            )

            var total: Long = 0
            for (path in storagePaths) {
                val stat = StatFs(path)
                val blockSize = stat.blockSizeLong
                val blockCount = stat.blockCountLong
                total += blockSize * blockCount
            }

            val withSystemOverhead = total + (total * 0.07).toLong()
            max(internalTotal, withSystemOverhead)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val internalStat = StatFs(Environment.getDataDirectory().path)
                val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
                internalTotal + (internalTotal * 0.12).toLong()
            } catch (innerException: Exception) {
                innerException.printStackTrace()
                0L
            }
        }
    }

    /**
     * 获取设备剩余存储空间
     */
    private fun getFreeDeviceStorage(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager =
                    getSystemService(STORAGE_STATS_SERVICE) as StorageStatsManager
                return storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
            }

            val stat = StatFs(Environment.getDataDirectory().path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    /**
     * 检查并请求权限
     */
    private fun checkAndRequestPermission() {
        when {
            // Android 11+ (API 30+) - 需要管理所有文件权限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    executePendingAction()
                } else {
                    showPermissionDialog(
                        null,
                        true
                    )
                }
            }
            // Android 10 (API 29)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    executePendingAction()
                } else {
                    showPermissionDialog(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    )
                }
            }
            // Android 6-9 (API 23-28)
            else -> {
                if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    executePendingAction()
                } else {
                    showPermissionDialog(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            }
        }
    }

    /**
     * 检查是否有权限
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 显示权限请求视图
     */
    private fun showPermissionRequest() {
        // 如果不是设置引导模式，恢复默认文案
        if (!isShowingSettingsGuide) {
            val tvMessage = permissionRequestLayout.findViewById<TextView>(R.id.tvPermissionMessage)
            val btnGrant = permissionRequestLayout.findViewById<TextView>(R.id.btnPermissionGrant)
            
            tvMessage.setText(R.string.permission_message)
            btnGrant.setText(R.string.yes)
        }
        
        permissionRequestLayout.visibility = View.VISIBLE
    }
    
    /**
     * 隐藏权限请求视图
     */
    private fun hidePermissionRequest() {
        permissionRequestLayout.visibility = View.GONE
    }
    
    /**
     * 请求实际的权限
     */
    private fun requestActualPermission() {
        when {
            // Android 11+ (API 30+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                requestedPermissions = null
                requestManageAllFilesPermission()
            }
            // Android 10 (API 29)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestedPermissions = permissions
                requestPermissionLauncher.launch(permissions)
            }
            // Android 6-9 (API 23-28)
            else -> {
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestedPermissions = permissions
                requestPermissionLauncher.launch(permissions)
            }
        }
    }
    
    /**
     * 显示权限对话框（保持兼容性）
     */
    private fun showPermissionDialog(
        permissions: Array<String>? = null,
        isManageAllFiles: Boolean = false
    ) {
        showPermissionRequest()
    }

    /**
     * 请求管理所有文件权限 (Android 11+)
     */
    private fun requestManageAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                manageStorageLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageLauncher.launch(intent)
            }
        }
    }

    /**
     * 执行待处理的操作
     */
    private fun executePendingAction() {
        pendingAction?.invoke()
        pendingAction = null
    }
    
    /**
     * 显示设置引导对话框
     */
    private fun showSettingsDialog() {
        isShowingSettingsGuide = true
        
        // 修改文案为设置引导
        val tvMessage = permissionRequestLayout.findViewById<TextView>(R.id.tvPermissionMessage)
        val btnGrant = permissionRequestLayout.findViewById<TextView>(R.id.btnPermissionGrant)
        
        tvMessage.text = "Permission is required to use this feature. Please grant permission in Settings."
        btnGrant.text = "Settings"
        
        // 显示权限请求布局（现在作为设置引导）
        showPermissionRequest()
    }
    
    /**
     * 打开应用设置页面
     */
    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            settingsLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                // 备用方案：打开设置主页
                val intent = Intent(Settings.ACTION_SETTINGS)
                settingsLauncher.launch(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Toast.makeText(this, "Unable to open settings", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingAction = null
        isShowingSettingsGuide = false
    }
}

