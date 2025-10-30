package com.eighth.day.lunar.qi

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * 闪屏导航接口
 * 
 * 架构优化:
 * 1. 接口回调 - 解耦导航逻辑
 * 2. 协程扩展 - Kotlin 高级特性
 * 3. DSL 构建器 - 流式 API
 * 4. 泛型约束 - 类型安全
 */
interface SplashNavigator {
    /**
     * 导航到目标页面
     */
    fun navigateTo(destinationClass: KClass<out Activity>)
    
    /**
     * 延迟导航
     */
    suspend fun delayAndNavigate(delayMillis: Long, destinationClass: KClass<out Activity>)
    
    /**
     * 导航完成回调
     */
    fun onNavigationComplete()
}

/**
 * 默认闪屏导航实现
 */
class DefaultSplashNavigator(
    private val activity: Activity,
    private val onComplete: () -> Unit = {}
) : SplashNavigator {
    
    override fun navigateTo(destinationClass: KClass<out Activity>) {
        activity.apply {
            startActivity(Intent(this, destinationClass.java))
            finish()
        }
        onComplete()
    }
    
    override suspend fun delayAndNavigate(delayMillis: Long, destinationClass: KClass<out Activity>) {
        delay(delayMillis)
        navigateTo(destinationClass)
    }
    
    override fun onNavigationComplete() {
        onComplete()
    }
}

/**
 * 导航配置 DSL
 */
class NavigationConfig {
    var delayMillis: Long = 1500L
    var destination: KClass<out Activity>? = null
    var enableBackPress: Boolean = false
    var onNavigate: (() -> Unit)? = null
    var onComplete: (() -> Unit)? = null
    
    fun build(): NavigationConfig = this
}

/**
 * DSL 构建器函数
 */
inline fun navigationConfig(block: NavigationConfig.() -> Unit): NavigationConfig {
    return NavigationConfig().apply(block).build()
}

/**
 * Activity 扩展函数 - 配置导航
 */
fun Activity.configureSplashNavigation(
    navigator: SplashNavigator,
    config: NavigationConfig
) {
    if (this !is LifecycleOwner) return
    
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            config.onNavigate?.invoke()
            
            config.destination?.let { dest ->
                navigator.delayAndNavigate(config.delayMillis, dest)
            }
            
            config.onComplete?.invoke()
        }
    }
}

/**
 * 扩展函数 - 自动导航
 */
inline fun <reified T : Activity> Activity.autoNavigateAfterDelay(
    delayMillis: Long = 1500L,
    crossinline onNavigate: () -> Unit = {}
) {
    if (this !is LifecycleOwner) return
    
    lifecycleScope.launch {
        onNavigate()
        delay(delayMillis)
        startActivity(Intent(this@autoNavigateAfterDelay, T::class.java))
        finish()
    }
}

