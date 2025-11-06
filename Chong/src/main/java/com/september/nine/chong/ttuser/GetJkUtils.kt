package com.september.nine.chong.ttuser

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.september.nine.chong.data.CunUtils
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.user.GetUserUtils
import org.json.JSONObject
import kotlin.random.Random

object GetJkUtils {
    private const val TAG = "GetJkUtils"
    private const val REQUIRED_MAX_RETRY = 20
    private const val OPTIONAL_MIN_RETRY = 2
    private const val OPTIONAL_MAX_RETRY = 5
    private const val MIN_DELAY_MS = 10_000L
    private const val MAX_DELAY_MS = 40_000L

    private val handler = Handler(Looper.getMainLooper())
    private val requestingKeys = mutableSetOf<String>()


    var isInstallPostState by CunUtils.boolean("grmkvfsd", false)

    fun getAUTool(jsonObject: JSONObject): Boolean {
        val user = jsonObject.getString("a_u_s")
        return user == "pop"
    }

    fun getShangTool(jsonObject: JSONObject): Boolean {
        try {
            val user = jsonObject.optString("post_type")
            return user == "show"
        } catch (e: Exception) {
            return false
        }
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
            AppEventsLogger.Companion.activateApp(KeyCon.openEc)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 上报事件（带重试）
     */
    fun postPointFun(
        canRetry: Boolean,
        name: String,
        key1: String? = null,
        keyValue1: Any? = null
    ) {
        val requestKey = "point_$name"
        val maxRetry = if (canRetry) REQUIRED_MAX_RETRY else Random.nextInt(
            OPTIONAL_MIN_RETRY,
            OPTIONAL_MAX_RETRY + 1
        )
        if (!canRetry && KeyCon.udec.isNotBlank() && !getShangTool(JSONObject(KeyCon.udec))) {
            return
        }
        executeWithRetry(
            requestKey = requestKey,
            maxRetry = maxRetry,
            taskName = "postPointFun[$name]",
            dataProvider = { EcTtUtils.upPointJson(KeyCon.openEc, name, key1, keyValue1) }
        )
    }

    /**
     * 上报广告事件（必传，带重试）
     */
    fun postAdJson(jsonData: String) {
        val requestKey = "ad_${jsonData.hashCode()}"

        executeWithRetry(
            requestKey = requestKey,
            maxRetry = REQUIRED_MAX_RETRY,
            taskName = "postAdJson",
            dataProvider = { EcTtUtils.upAdJson(KeyCon.openEc, jsonData) }
        )
    }

    /**
     * 上报安装事件（必传，带重试）
     */
    fun postInstallJson() {
        if (isInstallPostState) return

        executeWithRetry(
            requestKey = "install",
            maxRetry = REQUIRED_MAX_RETRY,
            taskName = "postInstallJson",
            dataProvider = { EcTtUtils.upInstallJson(KeyCon.openEc) },
            onSuccessCallback = { isInstallPostState = true }
        )
    }

    /**
     * 带重试机制的请求执行器
     */
    private fun executeWithRetry(
        requestKey: String,
        maxRetry: Int,
        taskName: String,
        dataProvider: () -> String,
        onSuccessCallback: (() -> Unit)? = null,
        currentAttempt: Int = 0
    ) {
        try {
            // 防止重复请求
            if (requestingKeys.contains(requestKey)) {
                return
            }

            // 标记为请求中
            requestingKeys.add(requestKey)

            val jsonData = dataProvider()

            GetUserUtils.postPutData(jsonData, object : GetUserUtils.CallbackMy {
                override fun onSuccess(response: String) {
                    requestingKeys.remove(requestKey)
                    onSuccessCallback?.invoke()
                }

                override fun onFailure(error: String) {

                    if (currentAttempt < maxRetry) {
                        // 计算随机延迟时间
                        val delayMs = Random.nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1)

                        // 延迟后重试
                        handler.postDelayed({
                            requestingKeys.remove(requestKey)
                            executeWithRetry(
                                requestKey,
                                maxRetry,
                                taskName,
                                dataProvider,
                                onSuccessCallback,
                                currentAttempt + 1
                            )
                        }, delayMs)
                    } else {
                        requestingKeys.remove(requestKey)
                    }
                }
            })
        } catch (e: Exception) {
            requestingKeys.remove(requestKey)
        }
    }
}