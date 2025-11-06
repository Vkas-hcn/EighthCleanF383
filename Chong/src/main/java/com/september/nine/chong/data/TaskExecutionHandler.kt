package com.september.nine.chong.data

import android.app.Application
import com.september.nine.chong.core.AppCore
import com.september.nine.chong.task.TaskManager

/**
 * 处理最终初始化和任务执行阶段
 */
class TaskExecutionHandler {

    interface TaskCallback {
        fun onTaskComplete()
    }

    /**
     * 执行最终初始化和任务
     * 步骤7: 初始化第六阶段
     * 步骤8: 任务执行第一阶段
     * 步骤9: 任务执行第二阶段
     */
    fun execute(app: Application, callback: TaskCallback) {
        // 第7步: 初始化第六阶段
        AppCore.initSixth(app) {
            // 第8步: 任务执行第一阶段
            TaskManager.executeFirst {
                // 第9步: 任务执行第二阶段
                TaskManager.executeSecond(app)
                callback.onTaskComplete()
            }
        }
    }
}

