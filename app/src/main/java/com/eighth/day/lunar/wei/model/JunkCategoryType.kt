package com.eighth.day.lunar.wei.model

import androidx.annotation.DrawableRes
import com.eighth.day.lunar.R

enum class JunkCategoryType(
    val displayName: String,
    @DrawableRes val iconRes: Int,
    val scanExtensions: List<String> = emptyList(),
    val scanKeywords: List<String> = emptyList()
) {
    APP_CACHE(
        displayName = "App Cache",
        iconRes = R.drawable.ic_app_cache,
        scanKeywords = listOf("cache")
    ),
    
    APK_FILES(
        displayName = "APK Files",
        iconRes = R.drawable.ic_apk_files,
        scanExtensions = listOf(".apk")
    ),
    
    LOG_FILES(
        displayName = "Log Files",
        iconRes = R.drawable.ic_log_files,
        scanExtensions = listOf(".log"),
        scanKeywords = listOf("log")
    ),
    
    AD_JUNK(
        displayName = "Ad Junk",
        iconRes = R.drawable.ic_ad_junk,
        scanKeywords = listOf("ad", "ads", "advertising", "admob", "adsense")
    ),
    
    TEMP_FILES(
        displayName = "Temp Files",
        iconRes = R.drawable.ic_temp_files,
        scanExtensions = listOf(".tmp", ".temp", ".cache"),
        scanKeywords = listOf("temp", "tmp")
    ),
    
    APP_RESIDUAL(
        displayName = "App Residual",
        iconRes = R.drawable.ic_app_residial,
        scanKeywords = listOf("residual")
    );
    
    companion object {
        /**
         * 根据显示名称查找分类类型
         */
        fun fromDisplayName(name: String): JunkCategoryType? {
            return values().find { it.displayName == name }
        }
        
        /**
         * 获取所有分类类型
         */
        fun getAllTypes(): List<JunkCategoryType> = values().toList()
    }
}

