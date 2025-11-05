package com.september.nine.chong.ttuser.builder

import android.content.Context
import org.json.JSONObject


class AdJsonBuilder(
    private val baseDataProvider: IBaseDataProvider,
    private val jsonMerger: IJsonMerger
) : IJsonBuilder {
    
    private var externalAdJson: String = ""
    

    fun setAdData(adJson: String): AdJsonBuilder {
        this.externalAdJson = adJson
        return this
    }
    

    override fun buildJson(context: Context): JSONObject {
        // 获取基础数据
        val baseJson = baseDataProvider.provideBaseData(context)
        
        // 添加plugging字段标识为广告事件
        baseJson.put("plugging", "stadium")
        
        // 如果有外部广告数据，则合并
        if (externalAdJson.isNotEmpty()) {
            val adJsonObject = JSONObject(externalAdJson)
            jsonMerger.merge(baseJson, adJsonObject)
        }
        
        return baseJson
    }
    

    fun buildWithAdData(context: Context, adJson: String): String {
        return setAdData(adJson).buildJsonString(context)
    }
}

