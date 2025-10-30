package com.eighth.day.lunar.yan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * 设置项动作枚举
 * 
 * 使用 sealed class 替代传统枚举，支持携带数据
 */
sealed class SettingsAction {
    data class OpenPrivacy(val url: String = "https://www.google.com/privacy") : SettingsAction()
    data class ShareApp(val shareConfig: ShareConfig) : SettingsAction()
    data class ShowToast(val message: String) : SettingsAction()
    object NavigateBack : SettingsAction()
}

/**
 * 分享配置数据类
 */
data class ShareConfig(
    val title: String,
    val content: String,
    val chooserTitle: String = "Share via"
)

/**
 * 设置动作处理器接口
 * 
 * 架构优化:
 * 1. 策略模式 - 不同动作不同处理
 * 2. 接口回调 - 解耦业务逻辑
 * 3. 高阶函数 - 灵活的回调机制
 */
interface SettingsActionHandler {
    /**
     * 处理设置动作
     */
    fun handle(action: SettingsAction)
    
    /**
     * 动作执行结果回调
     */
    fun onActionResult(success: Boolean, message: String? = null)
}

/**
 * 默认设置动作处理器实现
 */
class DefaultSettingsActionHandler(
    private val activity: Activity,
    private val onResult: (Boolean, String?) -> Unit = { _, _ -> }
) : SettingsActionHandler {
    
    override fun handle(action: SettingsAction) {
        when (action) {
            is SettingsAction.OpenPrivacy -> handleOpenPrivacy(action.url)
            is SettingsAction.ShareApp -> handleShareApp(action.shareConfig)
            is SettingsAction.ShowToast -> handleShowToast(action.message)
            is SettingsAction.NavigateBack -> handleNavigateBack()
        }
    }
    
    override fun onActionResult(success: Boolean, message: String?) {
        onResult(success, message)
    }
    
    private fun handleOpenPrivacy(url: String) {
        activity.runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            onActionResult(true, "Privacy page opened")
        }.onFailure { error ->
            onActionResult(false, "Failed to open privacy page: ${error.message}")
        }
    }
    
    private fun handleShareApp(config: ShareConfig) {
        activity.runCatching {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, config.title)
                putExtra(Intent.EXTRA_TEXT, config.content)
            }
            startActivity(Intent.createChooser(shareIntent, config.chooserTitle))
            onActionResult(true, "Share dialog opened")
        }.onFailure { error ->
            onActionResult(false, "Failed to share: ${error.message}")
        }
    }
    
    private fun handleShowToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        onActionResult(true, message)
    }
    
    private fun handleNavigateBack() {
        activity.finish()
        onActionResult(true, "Navigated back")
    }
}

/**
 * 分享配置 DSL 构建器
 */
class ShareConfigBuilder {
    var title: String = ""
    var content: String = ""
    var chooserTitle: String = "Share via"
    
    private val contentBuilder = StringBuilder()
    
    /**
     * DSL 风格添加内容行
     */
    fun line(text: String) {
        contentBuilder.append(text).append("\n")
    }
    
    /**
     * 从资源 ID 添加
     */
    fun titleFromRes(activity: Activity, @StringRes resId: Int) {
        title = activity.getString(resId)
    }
    
    fun build(): ShareConfig {
        val finalContent = if (contentBuilder.isNotEmpty()) {
            contentBuilder.toString().trimEnd()
        } else {
            content
        }
        return ShareConfig(title, finalContent, chooserTitle)
    }
}

/**
 * DSL 构建函数
 */
inline fun shareConfig(block: ShareConfigBuilder.() -> Unit): ShareConfig {
    return ShareConfigBuilder().apply(block).build()
}

/**
 * Activity 扩展函数 - 执行设置动作
 */
fun Activity.executeSettingsAction(
    action: SettingsAction,
    handler: SettingsActionHandler? = null
) {
    val actualHandler = handler ?: DefaultSettingsActionHandler(this)
    actualHandler.handle(action)
}

/**
 * Activity 扩展函数 - 批量配置设置项
 */
inline fun Activity.configureSettingsItems(
    crossinline block: SettingsItemsConfigScope.() -> Unit
) {
    val scope = SettingsItemsConfigScope(this)
    scope.block()
    scope.apply()
}

/**
 * 设置项配置作用域
 */
class SettingsItemsConfigScope(private val activity: Activity) {
    private val configurations = mutableListOf<Pair<Int, () -> Unit>>()
    
    /**
     * 配置单个设置项
     */
    fun item(viewId: Int, action: () -> Unit) {
        configurations.add(viewId to action)
    }
    
    /**
     * 应用所有配置
     */
    fun apply() {
        configurations.forEach { (viewId, action) ->
            activity.findViewById<android.view.View>(viewId)?.setOnClickListener {
                action()
            }
        }
    }
}

/**
 * 高阶函数 - 安全执行动作
 */
inline fun <T> Activity.safeExecute(
    crossinline action: Activity.() -> T,
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onError: (Throwable) -> Unit = {}
): Result<T> {
    return runCatching { action() }
        .onSuccess { onSuccess(it) }
        .onFailure { onError(it) }
}

