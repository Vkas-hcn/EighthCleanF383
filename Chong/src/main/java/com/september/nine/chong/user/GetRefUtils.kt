package com.september.nine.chong.user

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.september.nine.chong.data.JksGo
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.ttuser.GetJkUtils.postInstallJson
import java.util.UUID

object GetRefUtils {
    private var referrerClient: InstallReferrerClient? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var isGettingReferrer = false


    fun fetchInstallReferrer(context: Context) {
        // 如果已经有 referrer，不再获取
        if (KeyCon.rdec.isNotEmpty()) {
            Log.e("TAG", "fetchInstallReferrer: 已有referrer，无需获取 - ${KeyCon.rdec}")
            FanGetUser.getAUser(context)
            return
        }

        // 如果正在获取中，不重复执行
        if (isGettingReferrer) {
            Log.e("TAG", "fetchInstallReferrer: 正在获取中，跳过")
            return
        }

        Log.e("TAG", "fetchInstallReferrer: 开始获取referrer")
        isGettingReferrer = true

        try {
            // 清理之前的 client
            referrerClient?.endConnection()
            
            referrerClient = InstallReferrerClient.newBuilder(context).build()
            
            // 设置60秒超时
            timeoutRunnable = Runnable {
                Log.e("TAG", "fetchInstallReferrer: 超时，60秒内未获取到结果")
                cleanup()
                // 延迟5秒后重试
                handler.postDelayed({
                    fetchInstallReferrer(context)
                }, 5000)
            }
            handler.postDelayed(timeoutRunnable!!, 60000)
            
            referrerClient?.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                val response = referrerClient?.installReferrer
                                if (response != null) {
                                    val referrer = response.installReferrer
                                    KeyCon.rctsec = response.referrerClickTimestampSeconds.toString()
                                    KeyCon.rctssec = response.referrerClickTimestampServerSeconds.toString()
                                    KeyCon.rdec = referrer
                                    FanGetUser.getAUser(context)
                                    Log.e("TAG", "fetchInstallReferrer: 成功获取referrer - $referrer")
                                    // 成功获取，取消超时并清理
                                    handler.removeCallbacks(timeoutRunnable!!)
                                    cleanup()
                                } else {
                                    Log.e("TAG", "fetchInstallReferrer: response为null")
                                    onFailed("response为null")
                                }
                            } catch (e: Exception) {
                                Log.e("TAG", "fetchInstallReferrer: 处理referrer异常 - ${e.message}")
                                e.printStackTrace()
                                onFailed("处理referrer异常: ${e.message}")
                            }
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            Log.e("TAG", "fetchInstallReferrer: 功能不支持")
                            onFailed("功能不支持")
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            Log.e("TAG", "fetchInstallReferrer: 服务不可用")
                            onFailed("服务不可用")
                        }
                        else -> {
                            Log.e("TAG", "fetchInstallReferrer: 未知响应码 - $responseCode")
                            onFailed("未知响应码: $responseCode")
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    Log.e("TAG", "fetchInstallReferrer: 服务断开连接")
                    onFailed("服务断开连接")
                }
                
                private fun onFailed(reason: String) {
                    // 取消超时
                    handler.removeCallbacks(timeoutRunnable!!)
                    cleanup()
                    
                    // 延迟5秒后重试
                    Log.e("TAG", "fetchInstallReferrer: 失败 - $reason，5秒后重试")
                    handler.postDelayed({
                        fetchInstallReferrer(context)
                    }, 5000)
                }
            })
        } catch (e: Exception) {
            Log.e("TAG", "fetchInstallReferrer: 初始化异常 - ${e.message}")
            e.printStackTrace()
            cleanup()
            
            // 延迟5秒后重试
            handler.postDelayed({
                fetchInstallReferrer(context)
            }, 5000)
        }
    }

    /**
     * 清理资源
     */
    private fun cleanup() {
        isGettingReferrer = false
        try {
            referrerClient?.endConnection()
        } catch (e: Exception) {
            // 忽略清理时的异常
        }
        referrerClient = null
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        // 如果已经获取过，直接返回
        if (KeyCon.aidec.isNotEmpty()) {
            Log.e("TAG", "getAndroidId: 已有Android ID - ${KeyCon.aidec}")
            return KeyCon.aidec
        }

        var androidId = ""
        
        try {
            // 尝试获取 Android ID
            androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            // 检查 Android ID 是否有效
            if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
                // 9774d56d682e549c 是模拟器或某些设备的默认值，视为无效
                Log.e("TAG", "getAndroidId: Android ID无效或为空，使用UUID")
                androidId = ""
            } else {
                Log.e("TAG", "getAndroidId: 成功获取Android ID - $androidId")
            }
        } catch (e: SecurityException) {
            // 权限异常
            Log.e("TAG", "getAndroidId: 权限异常 - ${e.message}")
            e.printStackTrace()
            androidId = ""
        } catch (e: Exception) {
            // 其他异常
            Log.e("TAG", "getAndroidId: 获取Android ID异常 - ${e.message}")
            e.printStackTrace()
            androidId = ""
        }

        // 如果 Android ID 获取失败，使用 UUID
        if (androidId.isEmpty()) {
            try {
                androidId = UUID.randomUUID().toString()
                Log.e("TAG", "getAndroidId: 使用UUID - $androidId")
            } catch (e: Exception) {
                // UUID 生成异常（极少发生）
                Log.e("TAG", "getAndroidId: UUID生成异常 - ${e.message}")
                e.printStackTrace()
                // 使用时间戳作为最后的备选方案
                androidId = "fallback_${System.currentTimeMillis()}"
                Log.e("TAG", "getAndroidId: 使用时间戳备选方案 - $androidId")
            }
        }

        // 保存到 KeyCon
        try {
            KeyCon.aidec = androidId
            Log.e("TAG", "getAndroidId: 已保存到KeyCon.aidec")
        } catch (e: Exception) {
            Log.e("TAG", "getAndroidId: 保存到KeyCon异常 - ${e.message}")
            e.printStackTrace()
        }

        return androidId
    }
}