package com.september.nine.chong.core

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.september.nine.chong.data.CunUtils
import com.september.nine.chong.data.JksGo

/**
 * 应用核心组件 - 第一阶段初始化
 */
object AppCore {
    private val mainHandler = Handler(Looper.getMainLooper())
    
    fun initFirst(app: Application, callback: () -> Unit) {
        // 第1步: CunUtils初始化
        CunUtils.init(app)
        
        // 延迟执行下一步，避免调用堆栈连在一起
        mainHandler.postDelayed({
            callback()
        }, 5)
    }
    
    fun initThird(app: Application, callback: () -> Unit) {
        // 第4步: 启动周期性服务
        JksGo.startPeriodicService(app)
        
        mainHandler.postDelayed({
            callback()
        }, 3)
    }
    
    fun initSixth(app: Application, callback: () -> Unit) {
        // 第7步: 初始化Ally
        JksGo.initAlly(app)
        
        mainHandler.postDelayed({
            callback()
        }, 2)
    }
}

