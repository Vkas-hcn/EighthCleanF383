package com.eighth.day.lunar

import android.net.Uri
import java.io.File

/**
 * 文件数据模型
 */
data class FileItem(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val size: Long,
    val lastModified: Long,
    val mimeType: String?,
    var isSelected: Boolean = false
) {
    /**
     * 获取文件类型
     */
    fun getFileType(): FileType {
        return when {
            mimeType?.startsWith("image/") == true -> FileType.IMAGE
            mimeType?.startsWith("video/") == true -> FileType.VIDEO
            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
            name.endsWith(".doc", true) || name.endsWith(".docx", true) -> FileType.DOCS
            name.endsWith(".ppt", true) || name.endsWith(".pptx", true) -> FileType.PPT
            name.endsWith(".xls", true) || name.endsWith(".xlsx", true) -> FileType.EXCEL
            name.endsWith(".pdf", true) -> FileType.PDF
            name.endsWith(".zip", true) || name.endsWith(".rar", true) -> FileType.ZIP
            name.endsWith(".apk", true) -> FileType.APK
            else -> FileType.OTHER
        }
    }
    
    /**
     * 获取文件图标资源ID
     */
    fun getIconResId(): Int {
        return when (getFileType()) {
            FileType.IMAGE -> R.drawable.icon_main_p
            FileType.VIDEO -> R.drawable.icon_viode
            FileType.AUDIO -> R.drawable.icon_mucis
            FileType.DOCS -> R.drawable.icon_word
            FileType.PPT -> R.drawable.icon_ppt
            FileType.EXCEL -> R.drawable.icon_exc
            FileType.PDF -> R.drawable.icon_word
            FileType.ZIP -> R.drawable.icon_ohter
            FileType.APK -> R.drawable.icon_ohter
            FileType.OTHER -> R.drawable.icon_ohter
        }
    }
}

/**
 * 文件类型枚举
 */
enum class FileType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCS,
    PPT,
    EXCEL,
    PDF,
    ZIP,
    APK,
    OTHER
}

/**
 * 文件类型筛选器
 */
enum class FileTypeFilter(val displayName: String) {
    ALL("All Type"),
    IMAGE("Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    DOCS("Docs"),
    PPT("PPT"),
    EXCEL("Excel"),
    OTHER("Other")
}

/**
 * 文件大小筛选器
 */
enum class FileSizeFilter(val displayName: String, val minSize: Long) {
    ALL("All Size", 0),
    SIZE_10MB(">10MB", 10 * 1024 * 1024),
    SIZE_20MB(">20MB", 20 * 1024 * 1024),
    SIZE_50MB(">50MB", 50 * 1024 * 1024),
    SIZE_100MB(">100MB", 100 * 1024 * 1024),
    SIZE_200MB(">200MB", 200 * 1024 * 1024),
    SIZE_500MB(">500MB", 500 * 1024 * 1024)
}

/**
 * 文件时间筛选器
 */
enum class FileTimeFilter(val displayName: String, val daysAgo: Int) {
    ALL("All Time", -1),
    DAY_1("Within 1 day", 1),
    WEEK_1("Within 1 week", 7),
    MONTH_1("Within 1 month", 30),
    MONTH_3("Within 3 month", 90),
    MONTH_6("Within 6 month", 180)
}

