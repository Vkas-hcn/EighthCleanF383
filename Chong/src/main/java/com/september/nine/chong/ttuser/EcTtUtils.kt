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
    

    private val baseDataProvider by lazy { BaseJsonDataProvider() }
    
    private val jsonMerger by lazy { JsonMergeHelper() }
    
    private val installJsonBuilder by lazy {
        InstallJsonBuilder(baseDataProvider)
    }
    
    private val adJsonBuilder by lazy {
        AdJsonBuilder(baseDataProvider, jsonMerger)
    }
    
    private val pointJsonBuilder by lazy {
        PointJsonBuilder(baseDataProvider)
    }
    

    fun upInstallJson(context: Context): String {
        return installJsonBuilder.buildJsonString(context)
    }
    

    fun upAdJson(context: Context, adJson: String): String {
        return adJsonBuilder.buildWithAdData(context, adJson)
    }
    

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