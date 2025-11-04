package com.september.nine.chong.ttuser

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.applovin.impl.b7
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.user.GetUserUtils
import org.json.JSONObject
import java.util.UUID

object EcTtUtils {
    private fun topJsonData(context: Context): JSONObject {
        return JSONObject().apply {
            //os_version
            put("tic", Build.VERSION.RELEASE)
            //distinct_id
            put("obelisk", KeyCon.aidec)
            //log_id
            put("folio", UUID.randomUUID().toString())
            //device_model-最新需要传真实值
            put("hearse", Build.BRAND)
            //os
            put("tanh", "sellout")
            //gaid
            put("powell", "")
            //client_ts
            put("waylay", System.currentTimeMillis())
            //bundle_id
            put("avionic", context.packageName)
            //system_language//假值
            put("muriel", "ggfd_wqesad")
            //operator 传假值字符串
            put("employee", "ceas")
            //app_version
            put("captive", GetUserUtils.showAppVersion())
            //manufacturer
            put("farrell", Build.MANUFACTURER)
            //android_id
            put("definite", KeyCon.aidec)
        }

    }

    fun upInstallJson(context: Context): String {
        val cross = JSONObject().apply {
            //build
            put("snick", "build/${Build.ID}")

            //referrer_url
            put("carne", KeyCon.rdec)

            //user_agent
            put("casebook", "")

            //lat
            put("amos", "spaniel")

            //referrer_click_timestamp_seconds
            put("clot", 0)

            //install_begin_timestamp_seconds
            put("dallas", 0)

            //referrer_click_timestamp_server_seconds
            put("muezzin", 0)

            //install_begin_timestamp_server_seconds
            put("dempsey", 0)

            //install_first_seconds
            put("mobster", getFirstInstallTime(context))

            //last_update_seconds
            put("pursuit", 0)
        }
        return topJsonData(context).apply {
            put("cross", cross)
        }.toString()
    }

    fun upAdJson(context: Context, adJson: String): String {
        val baseJson = topJsonData(context)
        baseJson.put("plugging", "stadium")

        val adJsonObject = JSONObject(adJson)
        val keys = adJsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            baseJson.put(key, adJsonObject.get(key))
        }

        return baseJson.toString()
    }



    fun upPointJson(
        context: Context,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null,
    ): String {
        return topJsonData(context).apply {
            put("plugging", name)
            if (keyValue1 != null) put(name, JSONObject().put(key1, keyValue1))
        }.toString()
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    fun ConfigG(typeUser: Boolean, codeInt: String?) {
        var isuserData: String? = null
        isuserData = if (codeInt == null) {
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