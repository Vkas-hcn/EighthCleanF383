package com.september.nine.chong.data

import android.app.Application
import com.september.nine.chong.core.AppCore
import com.september.nine.chong.bridge.ComponentBridge
import com.september.nine.chong.utils.DataSync

/**
 * 处理初始化和配置阶段
 */
class InitializationHandler {

    interface InitCallback {
        fun onInitComplete()
    }

    /**
     * 执行初始化和配置
     * 步骤1: 初始化核心组件
     * 步骤2: 执行安全配置
     * 步骤3: 数据同步第一阶段
     */
    fun execute(app: Application, callback: InitCallback) {
        // 第1步: 初始化核心组件
        AppCore.initFirst(app) {
            // 第2步: 执行安全配置（enableAlias深度隐藏）
            ComponentBridge.processConfig(app) {
                // 第3步: 数据同步第一阶段
                DataSync.syncFirst(app) {
                    callback.onInitComplete()
                }
            }
        }
    }
}

