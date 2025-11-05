package com.september.nine.chong.ttuser

import android.content.Context
import com.september.nine.chong.ttuser.builder.AdJsonBuilder
import com.september.nine.chong.ttuser.builder.InstallJsonBuilder
import com.september.nine.chong.ttuser.builder.PointJsonBuilder
import com.september.nine.chong.ttuser.helper.JsonMergeHelper
import com.september.nine.chong.ttuser.provider.BaseJsonDataProvider

/**
 * EcTtUtils - JSON事件数据工具类（门面类）
 * 
 * 提供统一的API接口用于构建各种类型的事件JSON数据
 * 内部使用构建器模式和依赖注入，实现代码的高内聚低耦合
 * 
 * 架构说明：
 * - BaseJsonDataProvider: 提供基础数据
 * - InstallJsonBuilder: 构建安装事件JSON
 * - AdJsonBuilder: 构建广告事件JSON
 * - PointJsonBuilder: 构建埋点事件JSON
 * - JsonMergeHelper: JSON合并工具
 * 
 * @author 自动重构优化
 * @date 2025-11-05
 */
object EcTtUtils {
    
    // ==================== 依赖注入 ====================
    
    // 基础数据提供者
    private val baseDataProvider by lazy { BaseJsonDataProvider() }
    
    // JSON合并助手
    private val jsonMerger by lazy { JsonMergeHelper() }
    
    // 安装事件构建器
    private val installJsonBuilder by lazy {
        InstallJsonBuilder(baseDataProvider)
    }
    
    // 广告事件构建器
    private val adJsonBuilder by lazy {
        AdJsonBuilder(baseDataProvider, jsonMerger)
    }
    
    // 埋点事件构建器
    private val pointJsonBuilder by lazy {
        PointJsonBuilder(baseDataProvider)
    }
    
    // ==================== 公共API方法 ====================
    
    /**
     * 构建安装事件的JSON字符串
     * 
     * 用途：应用首次安装时上报安装信息
     * 包含：基础设备信息 + 安装相关信息（referrer、时间戳等）
     * 
     * @param context Android上下文
     * @return 安装事件的JSON字符串
     */
    fun upInstallJson(context: Context): String {
        return installJsonBuilder.buildJsonString(context)
    }
    
    /**
     * 构建广告事件的JSON字符串
     * 
     * 用途：广告展示、点击等事件上报
     * 包含：基础设备信息 + plugging标识 + 外部广告数据
     * 
     * @param context Android上下文
     * @param adJson 外部广告数据的JSON字符串
     * @return 广告事件的JSON字符串
     */
    fun upAdJson(context: Context, adJson: String): String {
        return adJsonBuilder.buildWithAdData(context, adJson)
    }
    
    /**
     * 构建埋点事件的JSON字符串
     * 
     * 用途：用户行为埋点上报
     * 包含：基础设备信息 + 事件名称 + 可选的自定义参数
     * 
     * @param context Android上下文
     * @param name 事件名称
     * @param key1 可选的自定义参数键名
     * @param keyValue1 可选的自定义参数值
     * @return 埋点事件的JSON字符串
     */
    fun upPointJson(
        context: Context,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
    ): String {
        return pointJsonBuilder.buildWithParams(context, name, key1, keyValue1)
    }

    fun ConfigG(typeUser: Boolean, codeInt: String?) {
        val isuserData: String? = if (codeInt == null) {
            null
        } else if (codeInt != "200") {
            codeInt
        } else if (typeUser) {
            "a"
        } else {
            "b"
        }
        GetJkUtils.postPointFun(true, "config_G", "getstring", isuserData)
    }

}