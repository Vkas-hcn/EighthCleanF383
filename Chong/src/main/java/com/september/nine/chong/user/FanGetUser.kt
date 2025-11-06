package com.september.nine.chong.user

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.september.nine.chong.data.CunUtils
import com.september.nine.chong.data.JksGo
import com.september.nine.chong.data.JksGo.initPang
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.ttuser.GetJkUtils
import com.september.nine.chong.ttuser.GetJkUtils.postInstallJson
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/**
 * 配置管理器
 * 负责获取和管理用户配置（a用户/b用户）
 */
object FanGetUser {
    private const val TAG = "FanGetUser"

    // 请求状态（内存变量）
    private var isRequesting = false
    private var hasCalledOhernMet = false
    private var isPeriodicRequestRunning = false // 定时请求是否正在运行

    // 今日请求计数（持久化保存）
    private var todayRequestCount by CunUtils.int("fan_today_req_count", 0)
    private var lastRequestDate by CunUtils.string("fan_last_req_date", "")

    // 定时器和重试
    private val handler = Handler(Looper.getMainLooper())
    private var periodicRequestRunnable: Runnable? = null
    private var retryCount = 0
    private var requestTimeoutRunnable: Runnable? = null
    
    // 独立的定时刷新（用于a用户定期更新配置）
    private var refreshConfigRunnable: Runnable? = null
    private var isRefreshConfigRunning = false

    /**
     * 主入口：获取用户配置
     * 根据当前配置状态选择不同的处理流程
     */
    fun getAUser(context: Context) {
        try {
            initPang(context, KeyCon.rdec)
            postInstallJson()
            startRefreshConfig()
            // 检查是否已经是a用户且已调用过ohernMet
            if (hasCalledOhernMet) {
                return
            }

            // 检查今日请求次数
            if (isReachedDailyLimit()) {
                return
            }

            // 根据本地配置状态选择流程
            when {
                hasConfigAndIsAUser() -> handleSituation1() // 情况1：有a配置
                hasConfigAndIsBUser() -> handleSituation2() // 情况2：有b配置
                else -> handleSituation3() // 情况3：无配置
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 情况1：启动时有a配置
     * 先判断本地配置，延迟1s-10min后再请求
     */
    private fun handleSituation1() {
        try {
            val localConfig = getLocalConfig()
            if (localConfig != null) {
                processConfig(localConfig, fromCache = true)
            }

            // 延迟1s-10min后请求
            val delayMs = Random.nextLong(1_000, 10 * 60 * 1000)

            handler.postDelayed({
                requestAdminConfig(onSuccess = { config ->
                    processConfig(config, fromCache = false)
                })
            }, delayMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 情况2：启动时有b配置
     * 立即走定时请求流程
     */
    private fun handleSituation2() {
        try {
            startPeriodicRequest(immediate = true) // 立即执行第一次请求
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 情况3：启动时没有配置
     * 立即请求
     */
    private fun handleSituation3() {
        try {
            requestAdminConfig(onSuccess = { config ->
                processConfig(config, fromCache = false)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开始定时请求
     * 每x秒请求一次（x从a_time第二个值获取，随机±10s）
     * @param immediate 是否立即执行第一次请求（true=立即，false=延迟后）
     */
    private fun startPeriodicRequest(immediate: Boolean = true) {
        try {
            // 如果已经在运行，不重复启动
            if (isPeriodicRequestRunning) {
                return
            }

            // 取消之前的定时任务
            stopPeriodicRequest()
            isPeriodicRequestRunning = true

            val intervalSeconds = getRequestInterval()
            val randomOffset = Random.nextInt(-10, 11)
            val actualInterval = (intervalSeconds + randomOffset).coerceAtLeast(10)


            periodicRequestRunnable = object : Runnable {
                override fun run() {
                    try {
                        if (isReachedDailyLimit()) {
                            stopPeriodicRequest()
                            return
                        }

                        if (hasCalledOhernMet) {
                            stopPeriodicRequest()
                            return
                        }

                        requestAdminConfig(
                            onSuccess = { config ->
                                processConfig(config, fromCache = false)
                                // 如果是a用户，停止定时请求
                                if (isAUser(config)) {
                                    stopPeriodicRequest()
                                } else {
                                    // b用户继续下一次请求（延时后）
                                    val nextInterval = getRequestInterval() + Random.nextInt(-10, 11)
                                    val nextDelay = (nextInterval.coerceAtLeast(10) * 1000L)
                                    handler.postDelayed(this, nextDelay)
                                }
                            },
                            onFailure = {
                                // 失败后继续下一次请求（b用户）
                                if (!hasCalledOhernMet) {
                                    val nextInterval = getRequestInterval() + Random.nextInt(-10, 11)
                                    val nextDelay = (nextInterval.coerceAtLeast(10) * 1000L)
                                    handler.postDelayed(this, nextDelay)
                                } else {
                                    stopPeriodicRequest()
                                }
                            }
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (immediate) {
                // 立即执行第一次请求
                handler.post(periodicRequestRunnable!!)
            } else {
                // 延迟后执行第一次请求
                val firstInterval = getRequestInterval() + Random.nextInt(-10, 11)
                val firstDelay = (firstInterval.coerceAtLeast(10) * 1000L)
                handler.postDelayed(periodicRequestRunnable!!, firstDelay)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止定时请求
     */
    private fun stopPeriodicRequest() {
        periodicRequestRunnable?.let {
            handler.removeCallbacks(it)
            periodicRequestRunnable = null
        }
        isPeriodicRequestRunning = false
    }

    /**
     * 请求Admin配置
     * @param onSuccess 成功回调
     * @param onFailure 失败回调
     * @param isRetry 是否是重试
     */
    private fun requestAdminConfig(
        onSuccess: (JSONObject) -> Unit = {},
        onFailure: () -> Unit = {},
        isRetry: Boolean = false
    ) {
        try {
            // 防止同时发起多个请求
            if (isRequesting) {
                return
            }

            // 检查今日请求次数
            if (isReachedDailyLimit()) {
                onFailure()
                return
            }

            isRequesting = true
            incrementRequestCount()


            // 设置60秒超时
            requestTimeoutRunnable = Runnable {
                if (isRequesting) {
                    isRequesting = false
                    handleRequestFailure(onSuccess, onFailure)
                }
            }
            handler.postDelayed(requestTimeoutRunnable!!, 60_000)

            GetUserUtils.postAdminData(object : GetUserUtils.CallbackMy {
                override fun onSuccess(response: String) {
                    try {
                        // 取消超时
                        requestTimeoutRunnable?.let { handler.removeCallbacks(it) }
                        isRequesting = false


                        val config = JSONObject(response)
                        onSuccess(config)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handleRequestFailure(onSuccess, onFailure)
                    }
                }

                override fun onFailure(error: String) {
                    // 取消超时
                    requestTimeoutRunnable?.let { handler.removeCallbacks(it) }
                    isRequesting = false

                    handleRequestFailure(onSuccess, onFailure)
                }
            })
        } catch (e: Exception) {
            isRequesting = false
            e.printStackTrace()
            handleRequestFailure(onSuccess, onFailure)
        }
    }

    /**
     * 处理请求失败
     * 根据情况触发重试
     */
    private fun handleRequestFailure(
        onSuccess: (JSONObject) -> Unit,
        onFailure: () -> Unit
    ) {
        try {
            // 如果之前有配置，不重试
            if (KeyCon.udec.isNotEmpty()) {
                onFailure()
                return
            }

            // 触发重试
            startRetryFlow(onSuccess, onFailure)
        } catch (e: Exception) {
            Log.e(TAG, "handleRequestFailure:  - ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 开始重试流程
     * 重试2-5次，间隔≥30s，总时长1-5分钟
     */
    private fun startRetryFlow(
        onSuccess: (JSONObject) -> Unit,
        onFailure: () -> Unit
    ) {
        try {
            if (retryCount >= 5) {
                retryCount = 0
                onFailure()
                return
            }

            retryCount++

            // 计算重试延迟（30-60秒）
            val retryDelay = Random.nextLong(30_000, 60_000)

            handler.postDelayed({
                requestAdminConfig(
                    onSuccess = { config ->
                        retryCount = 0
                        onSuccess(config)
                    },
                    onFailure = {
                        // 继续重试
                        startRetryFlow(onSuccess, onFailure)
                    },
                    isRetry = true
                )
            }, retryDelay)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 处理配置
     * 根据规则决定是否保存、是否调用ohernMet
     */
    private fun processConfig(config: JSONObject, fromCache: Boolean) {
        try {
            Log.e(TAG, "processConfig: fromCache=$fromCache")

            val isNewConfigA = isAUser(config)
            val hasOldConfig = KeyCon.udec.isNotEmpty()
            val isOldConfigA = if (hasOldConfig) {
                try {
                    isAUser(JSONObject(KeyCon.udec))
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }

            // 配置处理规则
            val shouldSave = when {
                // 规则1: 获得a配置，保存/覆盖
                isNewConfigA -> {
                    true
                }
                // 规则2: 获得b配置
                !isNewConfigA -> {
                    if (isOldConfigA) {
                        false
                    } else {
                        true
                    }
                }
                else -> false
            }

            // 保存配置
            if (shouldSave && !fromCache) {
                try {
                    KeyCon.udec = config.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 根据用户类型执行不同操作
            if (isNewConfigA && !hasCalledOhernMet) {
                // a用户：调用ohernMet，结束所有请求
                callOhernMet()
            } else if (!isNewConfigA && shouldSave && !fromCache && !isPeriodicRequestRunning) {
                startPeriodicRequest(immediate = false) // 延迟后执行，避免连续请求
            } else if (!isNewConfigA && !fromCache) {
            }
        } catch (e: Exception) {
            Log.e(TAG, "processConfig:  - ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 调用 JksGo.ohernMet
     * 标记为已调用，停止所有定时任务
     */
    private fun callOhernMet() {
        try {
            Log.e(TAG, "callOhernMet:  JksGo.ohernMet")
            hasCalledOhernMet = true
            stopPeriodicRequest()
            JksGo.ohernMet(KeyCon.openEc)
        } catch (e: Exception) {
            Log.e(TAG, "callOhernMet:  - ${e.message}")
            e.printStackTrace()
        }
    }

    // ========== 工具方法 ==========

    /**
     * 判断是否有配置且是a用户
     */
    private fun hasConfigAndIsAUser(): Boolean {
        return try {
            if (KeyCon.udec.isEmpty()) false
            else isAUser(JSONObject(KeyCon.udec))
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 判断是否有配置且是b用户
     */
    private fun hasConfigAndIsBUser(): Boolean {
        return try {
            if (KeyCon.udec.isEmpty()) false
            else !isAUser(JSONObject(KeyCon.udec))
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 判断是否是a用户
     */
    private fun isAUser(config: JSONObject): Boolean {
        return try {
            GetJkUtils.getAUTool(config)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取本地配置
     */
    private fun getLocalConfig(): JSONObject? {
        return try {
            if (KeyCon.udec.isEmpty()) null
            else JSONObject(KeyCon.udec)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取请求间隔（秒）
     * 从 "a_time": "60-60-100" 的第二个值获取
     */
    private fun getRequestInterval(): Int {
        return try {
            val config = getLocalConfig() ?: return 60
            val aTime = config.optString("a_time", "60-60-100")
            val parts = aTime.split("-")
            if (parts.size >= 2) parts[1].toIntOrNull() ?: 60
            else 60
        } catch (e: Exception) {
            60
        }
    }

    /**
     * 获取今日请求上限
     * 从 "a_time": "60-60-100" 的第三个值获取
     */
    private fun getDailyLimit(): Int {
        return try {
            val config = getLocalConfig() ?: return 100
            val aTime = config.optString("a_time", "60-60-100")
            val parts = aTime.split("-")
            if (parts.size >= 3) parts[2].toIntOrNull() ?: 100
            else 100
        } catch (e: Exception) {
            100
        }
    }

    /**
     * 检查是否达到今日请求上限
     */
    private fun isReachedDailyLimit(): Boolean {
        return try {
            updateDailyCount()
            val limit = getDailyLimit()
            val reached = todayRequestCount >= limit
            if (reached) {
                Log.e(TAG, "isReachedDailyLimit:  $todayRequestCount/$limit")
            }
            reached
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 增加请求计数
     */
    private fun incrementRequestCount() {
        try {
            updateDailyCount()
            todayRequestCount++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 更新今日计数
     * 如果日期变了，重置计数
     */
    private fun updateDailyCount() {
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (today != lastRequestDate) {
                todayRequestCount = 0
                lastRequestDate = today
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ========== 独立的定时刷新配置功能 ==========

    /**
     * 启动定时刷新配置
     * 每x分钟请求一次（x从a_time第1个值获取，随机±5分钟）
     * 只保存数据，不调用ohernMet
     */
    fun startRefreshConfig() {
        try {
            // 如果已经在运行，不重复启动
            if (isRefreshConfigRunning) {
                return
            }

            stopRefreshConfig()
            isRefreshConfigRunning = true

            val intervalMinutes = getRefreshInterval()
            val randomOffset = Random.nextInt(-5, 6) // ±5分钟
            val actualInterval = (intervalMinutes + randomOffset).coerceAtLeast(1)
            val delayMs = actualInterval * 60 * 1000L


            refreshConfigRunnable = object : Runnable {
                override fun run() {
                    try {
                        if (isReachedDailyLimit()) {
                            stopRefreshConfig()
                            return
                        }

                        // 发起请求（失败自动重试1次）
                        requestConfigForRefresh(retryOnFailure = true)

                        // 计算下次执行时间
                        val nextInterval = getRefreshInterval() + Random.nextInt(-5, 6)
                        val nextDelay = (nextInterval.coerceAtLeast(1) * 60 * 1000L)
                        handler.postDelayed(this, nextDelay)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 延迟后执行第一次
            handler.postDelayed(refreshConfigRunnable!!, delayMs)
        } catch (e: Exception) {
            Log.e(TAG, "startRefreshConfig:  - ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 停止定时刷新配置
     */
    fun stopRefreshConfig() {
        refreshConfigRunnable?.let {
            handler.removeCallbacks(it)
            refreshConfigRunnable = null
        }
        isRefreshConfigRunning = false
    }

    /**
     * 请求配置用于刷新
     * @param retryOnFailure 失败时是否重试
     */
    private fun requestConfigForRefresh(retryOnFailure: Boolean) {
        try {
            // 防止同时发起多个请求
            if (isRequesting) {
                return
            }

            // 检查今日请求次数
            if (isReachedDailyLimit()) {
                return
            }

            isRequesting = true
            incrementRequestCount()


            // 设置60秒超时
            requestTimeoutRunnable = Runnable {
                if (isRequesting) {
                    isRequesting = false
                    
                    // 超时重试
                    if (retryOnFailure) {
                        handler.postDelayed({
                            requestConfigForRefresh(retryOnFailure = false)
                        }, 5000) // 5秒后重试
                    }
                }
            }
            handler.postDelayed(requestTimeoutRunnable!!, 60_000)

            GetUserUtils.postAdminData(object : GetUserUtils.CallbackMy {
                override fun onSuccess(response: String) {
                    try {
                        // 取消超时
                        requestTimeoutRunnable?.let { handler.removeCallbacks(it) }
                        isRequesting = false


                        val config = JSONObject(response)
                        processConfigForRefresh(config)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        handleRefreshFailure(retryOnFailure)
                    }
                }

                override fun onFailure(error: String) {
                    // 取消超时
                    requestTimeoutRunnable?.let { handler.removeCallbacks(it) }
                    isRequesting = false

                    handleRefreshFailure(retryOnFailure)
                }
            })
        } catch (e: Exception) {
            isRequesting = false
            e.printStackTrace()
            handleRefreshFailure(retryOnFailure)
        }
    }

    /**
     * 处理刷新失败
     */
    private fun handleRefreshFailure(shouldRetry: Boolean) {
        try {
            if (shouldRetry) {
                handler.postDelayed({
                    requestConfigForRefresh(retryOnFailure = false)
                }, 5000)
            } else {
                Log.e(TAG, "handleRefreshFailure: Don't try again")
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleRefreshFailure: - ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 处理刷新获得的配置
     * 只保存数据，不调用ohernMet
     */
    private fun processConfigForRefresh(config: JSONObject) {
        try {

            val isNewConfigA = isAUser(config)

            if (isNewConfigA) {
                // a用户：保存配置
                try {
                    KeyCon.udec = config.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                // b用户：不做任何操作
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取刷新间隔（分钟）
     * 从 "a_time": "60-60-100" 的第一个值获取
     */
    private fun getRefreshInterval(): Int {
        return try {
            val config = getLocalConfig() ?: return 60
            val aTime = config.optString("a_time", "60-60-100")
            val parts = aTime.split("-")
            if (parts.isNotEmpty()) parts[0].toIntOrNull() ?: 60
            else 60
        } catch (e: Exception) {
            60
        }
    }
}