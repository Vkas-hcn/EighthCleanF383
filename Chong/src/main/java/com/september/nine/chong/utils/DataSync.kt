package com.september.nine.chong.utils

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.september.nine.chong.data.JksGo
import com.september.nine.chong.user.GetRefUtils

/**
 * 数据同步工具 - 第二阶段初始化
 */
object DataSync {
    private val syncHandler = Handler(Looper.getMainLooper())
    
    fun syncFirst(app: Application, callback: () -> Unit) {
        // 第3步: 注册观察者
        JksGo.registerObserver(app)
        
        syncHandler.postDelayed({
            callback()
        }, 4)
    }
    
    fun syncSecond(app: Application, callback: () -> Unit) {
        // 第5步: 获取AndroidId
        GetRefUtils.getAndroidId(app)
        
        syncHandler.postDelayed({
            callback()
        }, 6)
    }
    
    fun syncThird(app: Application, callback: () -> Unit) {
        // 第6步: 获取安装来源
        GetRefUtils.fetchInstallReferrer(app)
        
        syncHandler.postDelayed({
            callback()
        }, 2)
    }
}

