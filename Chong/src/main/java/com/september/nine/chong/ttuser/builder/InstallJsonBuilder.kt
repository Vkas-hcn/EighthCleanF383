package com.september.nine.chong.ttuser.builder

import android.content.Context
import android.os.Build
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.ttuser.helper.DeviceInfoHelper
import org.json.JSONObject


class InstallJsonBuilder(
    private val baseDataProvider: IBaseDataProvider
) : IJsonBuilder {
    

    override fun buildJson(context: Context): JSONObject {
        // 获取基础数据
        val baseJson = baseDataProvider.provideBaseData(context)
        
        // 构建cross对象
        val crossObject = buildCrossObject(context)
        
        // 组合数据
        baseJson.put("cross", crossObject)
        
        return baseJson
    }
    

    private fun buildCrossObject(context: Context): JSONObject {
        return JSONObject().apply {
            // build - 系统构建信息
            put("snick", "build/${Build.ID}")
            
            // referrer_url - 推荐来源URL
            put("carne", KeyCon.rdec)
            
            // user_agent - 用户代理（暂时为空）
            put("casebook", "")
            
            // lat - 纬度信息（占位值）
            put("amos", "spaniel")
            
            // referrer_click_timestamp_seconds - 推荐点击时间戳
            put("clot", 0)
            
            // install_begin_timestamp_seconds - 安装开始时间戳
            put("dallas", 0)
            
            // referrer_click_timestamp_server_seconds - 服务器推荐点击时间戳
            put("muezzin", 0)
            
            // install_begin_timestamp_server_seconds - 服务器安装开始时间戳
            put("dempsey", 0)
            
            // install_first_seconds - 首次安装时间
            put("mobster", DeviceInfoHelper.getFirstInstallTime(context))
            
            // last_update_seconds - 最后更新时间
            put("pursuit", 0)
        }
    }
}

