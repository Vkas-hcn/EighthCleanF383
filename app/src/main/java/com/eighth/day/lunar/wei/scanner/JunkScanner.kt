package com.eighth.day.lunar.wei.scanner

import android.content.Context
import android.os.Environment
import com.eighth.day.lunar.wei.callback.ScanCallback
import com.eighth.day.lunar.wei.model.JunkCategoryModel
import com.eighth.day.lunar.wei.model.JunkCategoryType
import com.eighth.day.lunar.wei.model.JunkFileModel
import kotlinx.coroutines.delay
import java.io.File


interface JunkScanStrategy {

    suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel>
    

    fun getCategoryType(): JunkCategoryType
}


class JunkScannerManager(private val context: Context) {
    
    // 使用 LinkedHashMap 保持注册顺序，确保扫描优先级一致
    private val scanStrategies = LinkedHashMap<JunkCategoryType, JunkScanStrategy>()
    
    init {
        registerDefaultStrategies()
    }
    

    private fun registerDefaultStrategies() {
        registerStrategy(AppCacheScanStrategy())
        registerStrategy(ApkFileScanStrategy())
        registerStrategy(LogFileScanStrategy())
        registerStrategy(AdJunkScanStrategy())
        registerStrategy(TempFileScanStrategy())
        registerStrategy(AppResidualScanStrategy())
    }
    

    fun registerStrategy(strategy: JunkScanStrategy) {
        scanStrategies[strategy.getCategoryType()] = strategy
    }
    

    suspend fun scanAll(callback: ScanCallback?): Map<JunkCategoryType, JunkCategoryModel> {
        callback?.onScanStarted(scanStrategies.size)
        
        val results = mutableMapOf<JunkCategoryType, JunkCategoryModel>()
        val scannedFilePaths = mutableSetOf<String>()  // 全局去重集合
        var totalFiles = 0
        var totalSize = 0L
        
        scanStrategies.forEach { (type, strategy) ->
            try {
                val files = strategy.scan(context, callback)
                
                // 过滤已被其他分类扫描到的文件（全局去重）
                val uniqueFiles = files.filter { file ->
                    val path = file.path
                    if (scannedFilePaths.contains(path)) {
                        false  // 文件已被其他分类扫描，跳过
                    } else {
                        scannedFilePaths.add(path)  // 添加到已扫描集合
                        true  // 这是第一次扫描到此文件
                    }
                }
                
                val categoryModel = JunkCategoryModel(type, uniqueFiles.toMutableList())
                results[type] = categoryModel
                
                totalFiles += uniqueFiles.size
                totalSize += uniqueFiles.sumOf { it.size }
                
                callback?.onSizeChanged(totalSize)
            } catch (e: Exception) {
                callback?.onScanError(e)
            }
        }
        
        callback?.onScanCompleted(totalFiles, totalSize)
        return results
    }
    

    suspend fun scanCategory(
        type: JunkCategoryType,
        callback: ScanCallback?
    ): JunkCategoryModel? {
        val strategy = scanStrategies[type] ?: return null
        
        return try {
            val files = strategy.scan(context, callback)
            JunkCategoryModel(type, files.toMutableList())
        } catch (e: Exception) {
            callback?.onScanError(e)
            null
        }
    }
}


abstract class BaseScanStrategy : JunkScanStrategy {
    
    protected suspend fun scanDirectory(
        dir: File,
        categoryType: JunkCategoryType,
        callback: ScanCallback?,
        maxDepth: Int = 2,
        currentDepth: Int = 0
    ): List<JunkFileModel> {
        if (!dir.exists() || !dir.isDirectory || currentDepth > maxDepth) {
            return emptyList()
        }
        
        val files = mutableListOf<JunkFileModel>()
        
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.length() > 0) {
                    val junkFile = JunkFileModel(file)
                    files.add(junkFile)
                    callback?.onFileFound(junkFile, categoryType.displayName)
                } else if (file.isDirectory && currentDepth < maxDepth) {
                    callback?.onScanProgress(file.absolutePath, categoryType.displayName)
                    delay(10)
                    files.addAll(
                        scanDirectory(file, categoryType, callback, maxDepth, currentDepth + 1)
                    )
                }
            }
        } catch (e: Exception) {
            callback?.onScanError(e)
        }
        
        return files
    }
    
    protected suspend fun scanByExtension(
        dir: File?,
        categoryType: JunkCategoryType,
        extensions: List<String>,
        callback: ScanCallback?,
        maxDepth: Int = 2,
        currentDepth: Int = 0,
        nameContains: String? = null
    ): List<JunkFileModel> {
        if (dir == null || !dir.exists() || !dir.isDirectory || currentDepth > maxDepth) {
            return emptyList()
        }
        
        val files = mutableListOf<JunkFileModel>()
        
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val matchesExtension = extensions.any { 
                        file.name.endsWith(it, ignoreCase = true) 
                    }
                    val matchesName = nameContains == null || 
                                     file.name.contains(nameContains, ignoreCase = true)
                    
                    if (matchesExtension && matchesName && file.length() > 0) {
                        val junkFile = JunkFileModel(file)
                        files.add(junkFile)
                        callback?.onFileFound(junkFile, categoryType.displayName)
                    }
                } else if (file.isDirectory && currentDepth < maxDepth) {
                    callback?.onScanProgress(file.absolutePath, categoryType.displayName)
                    delay(10)
                    files.addAll(
                        scanByExtension(
                            file, categoryType, extensions, callback,
                            maxDepth, currentDepth + 1, nameContains
                        )
                    )
                }
            }
        } catch (e: Exception) {
            callback?.onScanError(e)
        }
        
        return files
    }
    
    protected suspend fun scanByKeyword(
        dir: File?,
        categoryType: JunkCategoryType,
        keywords: List<String>,
        callback: ScanCallback?,
        maxDepth: Int = 2,
        currentDepth: Int = 0
    ): List<JunkFileModel> {
        if (dir == null || !dir.exists() || !dir.isDirectory || currentDepth > maxDepth) {
            return emptyList()
        }
        
        val files = mutableListOf<JunkFileModel>()
        
        try {
            dir.listFiles()?.forEach { file ->
                val matchesKeyword = keywords.any { 
                    file.name.contains(it, ignoreCase = true) 
                }
                
                if (matchesKeyword) {
                    if (file.isFile && file.length() > 0) {
                        val junkFile = JunkFileModel(file)
                        files.add(junkFile)
                        callback?.onFileFound(junkFile, categoryType.displayName)
                    } else if (file.isDirectory) {
                        callback?.onScanProgress(file.absolutePath, categoryType.displayName)
                        delay(10)
                        files.addAll(scanDirectory(file, categoryType, callback, maxDepth = 1))
                    }
                }
                
                if (file.isDirectory && currentDepth < maxDepth) {
                    files.addAll(
                        scanByKeyword(
                            file, categoryType, keywords, callback,
                            maxDepth, currentDepth + 1
                        )
                    )
                }
            }
        } catch (e: Exception) {
            callback?.onScanError(e)
        }
        
        return files
    }
}


class AppCacheScanStrategy : BaseScanStrategy() {
    override suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel> {
        val files = mutableListOf<JunkFileModel>()
        val type = getCategoryType()
        
        callback?.onScanProgress("Scanning app cache...", type.displayName)
        delay(100)
        
        // 扫描应用缓存
        context.cacheDir?.let {
            files.addAll(scanDirectory(it, type, callback))
        }
        context.externalCacheDir?.let {
            files.addAll(scanDirectory(it, type, callback))
        }
        
        // 扫描其他应用缓存
        val androidDataDir = File(Environment.getExternalStorageDirectory(), "Android/data")
        if (androidDataDir.exists()) {
            androidDataDir.listFiles()?.forEach { appDir ->
                val appCacheDir = File(appDir, "cache")
                if (appCacheDir.exists()) {
                    files.addAll(scanDirectory(appCacheDir, type, callback, maxDepth = 2))
                }
            }
        }
        
        return files.distinctBy { it.path }
    }
    
    override fun getCategoryType() = JunkCategoryType.APP_CACHE
}

class ApkFileScanStrategy : BaseScanStrategy() {
    override suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel> {
        val files = mutableListOf<JunkFileModel>()
        val type = getCategoryType()
        
        callback?.onScanProgress("Scanning APK files...", type.displayName)
        delay(100)
        
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        files.addAll(scanByExtension(downloadDir, type, listOf(".apk"), callback))
        
        val storageDir = Environment.getExternalStorageDirectory()
        files.addAll(scanByExtension(storageDir, type, listOf(".apk"), callback, maxDepth = 3))
        
        return files.distinctBy { it.path }
    }
    
    override fun getCategoryType() = JunkCategoryType.APK_FILES
}

class LogFileScanStrategy : BaseScanStrategy() {
    override suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel> {
        val files = mutableListOf<JunkFileModel>()
        val type = getCategoryType()
        
        callback?.onScanProgress("Scanning log files...", type.displayName)
        delay(100)
        
        val storageDir = Environment.getExternalStorageDirectory()
        files.addAll(scanByExtension(storageDir, type, listOf(".log"), callback, maxDepth = 3))
        files.addAll(scanByExtension(storageDir, type, listOf(".txt"), callback, maxDepth = 2, nameContains = "log"))
        
        return files.distinctBy { it.path }
    }
    
    override fun getCategoryType() = JunkCategoryType.LOG_FILES
}


class AdJunkScanStrategy : BaseScanStrategy() {
    override suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel> {
        val files = mutableListOf<JunkFileModel>()
        val type = getCategoryType()
        
        callback?.onScanProgress("Scanning ad junk...", type.displayName)
        delay(100)
        
        val storageDir = Environment.getExternalStorageDirectory()
        files.addAll(
            scanByKeyword(
                storageDir, type,
                listOf("ad", "ads", "advertising", "admob", "adsense"),
                callback, maxDepth = 3
            )
        )
        
        return files.distinctBy { it.path }
    }
    
    override fun getCategoryType() = JunkCategoryType.AD_JUNK
}


class TempFileScanStrategy : BaseScanStrategy() {
    override suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel> {
        val files = mutableListOf<JunkFileModel>()
        val type = getCategoryType()
        
        callback?.onScanProgress("Scanning temp files...", type.displayName)
        delay(100)
        
        val storageDir = Environment.getExternalStorageDirectory()
        files.addAll(
            scanByExtension(
                storageDir, type,
                listOf(".tmp", ".temp", ".cache"),
                callback, maxDepth = 3
            )
        )
        files.addAll(
            scanByKeyword(storageDir, type, listOf("temp", "tmp"), callback, maxDepth = 2)
        )
        
        return files.distinctBy { it.path }
    }
    
    override fun getCategoryType() = JunkCategoryType.TEMP_FILES
}


class AppResidualScanStrategy : BaseScanStrategy() {
    override suspend fun scan(context: Context, callback: ScanCallback?): List<JunkFileModel> {
        val files = mutableListOf<JunkFileModel>()
        val type = getCategoryType()
        
        callback?.onScanProgress("Scanning app residual...", type.displayName)
        delay(100)
        
        val androidDataDir = File(Environment.getExternalStorageDirectory(), "Android/data")
        if (androidDataDir.exists()) {
            androidDataDir.listFiles()?.forEach { appDir ->
                if (appDir.isDirectory) {
                    val filesDir = File(appDir, "files")
                    if (filesDir.exists()) {
                        files.addAll(scanDirectory(filesDir, type, callback, maxDepth = 1))
                    }
                }
            }
        }
        
        return files.distinctBy { it.path }
    }
    
    override fun getCategoryType() = JunkCategoryType.APP_RESIDUAL
}

