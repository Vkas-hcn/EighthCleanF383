package com.september.nine.chong.data

import android.app.Application
import com.september.nine.chong.core.AppCore
import com.september.nine.chong.utils.DataSync

/**
 * 处理数据同步和中间初始化阶段
 */
class DataSyncHandler {

    interface SyncCallback {
        fun onSyncComplete()
    }

    /**
     * 执行第三阶段初始化和数据同步
     * 步骤4: 初始化第三阶段
     * 步骤5: 数据同步第二阶段
     * 步骤6: 数据同步第三阶段
     */
    fun execute(app: Application, callback: SyncCallback) {
        // 第4步: 初始化第三阶段
        AppCore.initThird(app) {
            // 第5步: 数据同步第二阶段
            DataSync.syncSecond(app) {
                // 第6步: 数据同步第三阶段
                DataSync.syncThird(app) {
                    callback.onSyncComplete()
                }
            }
        }
    }
}

