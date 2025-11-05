package com.september.nine.chong.ttuser.provider

import android.content.Context
import android.os.Build
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.ttuser.builder.IBaseDataProvider
import com.september.nine.chong.user.GetUserUtils
import org.json.JSONObject
import java.util.UUID


class BaseJsonDataProvider : IBaseDataProvider {
    

    override fun provideBaseData(context: Context): JSONObject {
        return JSONObject().apply {
            // os_version - 操作系统版本
            put("tic", Build.VERSION.RELEASE)
            
            // distinct_id - 唯一设备ID
            put("obelisk", KeyCon.aidec)
            
            // log_id - 日志ID，每次生成唯一
            put("folio", UUID.randomUUID().toString())
            
            // device_model - 设备品牌
            put("hearse", Build.BRAND)
            
            // os - 操作系统类型
            put("tanh", "sellout")
            
            // gaid - Google广告ID（暂时为空）
            put("powell", "")
            
            // client_ts - 客户端时间戳
            put("waylay", System.currentTimeMillis())
            
            // bundle_id - 应用包名
            put("avionic", context.packageName)
            
            // system_language - 系统语言（假值）
            put("muriel", "ggfd_wqesad")
            
            // operator - 运营商信息（假值）
            put("employee", "ceas")
            
            // app_version - 应用版本号
            put("captive", GetUserUtils.showAppVersion())
            
            // manufacturer - 设备制造商
            put("farrell", Build.MANUFACTURER)
            
            // android_id - Android ID
            put("definite", KeyCon.aidec)
        }
    }
}

