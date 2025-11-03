package com.september.nine.chong.user

import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.september.nine.chong.data.KeyCon
import org.json.JSONObject

object GetJkUtils {
    fun getAUTool(jsonObject: JSONObject): Boolean {
        val user = jsonObject.optString("a_u_s")
        return user == "pop"
    }

    fun initFb(jsonObject: JSONObject) {
        try {
            val fbStr = jsonObject.optString("bk_v").split("-")[0]
            val token = jsonObject.optString("bk_v").split("-")[1]
            if (fbStr.isBlank()) return
            if (token.isBlank()) return
            if (FacebookSdk.isInitialized()) return
            FacebookSdk.setApplicationId(fbStr)
            FacebookSdk.setClientToken(token)
            FacebookSdk.sdkInitialize(KeyCon.openEc)
            AppEventsLogger.activateApp(KeyCon.openEc)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postPointFun(
        canRetry: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null
    ) {

    }

    fun ConfigG(bol: Boolean, string2: String) {}
}