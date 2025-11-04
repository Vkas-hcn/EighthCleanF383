package com.september.nine.chong.task

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.september.nine.chong.data.JksGo
import wawa.wkg.Kawm

/**
 * 任务管理器 - 第三阶段初始化
 */
object TaskManager {
    private val taskHandler = Handler(Looper.getMainLooper())
    
    fun executeFirst(callback: () -> Unit) {
        // 第8步: 执行ssPostFun
        JksGo.ssPostFun()
        
        taskHandler.postDelayed({
            callback()
        }, 3)
    }
    
    fun executeSecond(app: Application) {
        // 第9步: 启动所有任务
        Kawm.startAllTasks(app)
    }
}

