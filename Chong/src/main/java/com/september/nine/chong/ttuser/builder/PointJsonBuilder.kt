package com.september.nine.chong.ttuser.builder

import android.content.Context
import org.json.JSONObject


class PointJsonBuilder(
    private val baseDataProvider: IBaseDataProvider
) : IJsonBuilder {
    
    private var eventName: String = ""
    private var customKey: String? = null
    private var customValue: Any? = null
    

    fun setEventName(name: String): PointJsonBuilder {
        this.eventName = name
        return this
    }
    

    fun setCustomParam(key: String?, value: Any?): PointJsonBuilder {
        this.customKey = key
        this.customValue = value
        return this
    }

    override fun buildJson(context: Context): JSONObject {
        // 获取基础数据
        val baseJson = baseDataProvider.provideBaseData(context)
        
        // 设置事件名称
        baseJson.put("plugging", eventName)
        
        // 如果提供了自定义参数，添加到JSON中
        if (customValue != null && !customKey.isNullOrEmpty()) {
            val customData = JSONObject().put(customKey!!, customValue)
            baseJson.put(eventName, customData)
        }
        
        return baseJson
    }
    

    fun buildWithParams(
        context: Context,
        name: String,
        key: String? = null,
        value: Any? = null
    ): String {
        return setEventName(name)
            .setCustomParam(key, value)
            .buildJsonString(context)
    }
}

