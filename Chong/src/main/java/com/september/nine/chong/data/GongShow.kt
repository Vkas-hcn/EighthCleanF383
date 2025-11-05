package com.september.nine.chong.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.september.nine.chong.data.KeyCon.launchState
import d.D

object GongShow {
    fun d2(context: Context) {
        try {
            val ctx = context
            // 检查启动状态 - 直接使用KeyCon
            if ("go" == launchState) {
                return
            }

            // 启用组件
            val pm = ctx.packageManager
            val componentName = ComponentName(ctx, "com.eighth.day.lunar.EcQi")
            pm.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Log.e("TAG", "d2: -go")
            // 更新状态 - 直接使用KeyCon
            launchState = "go"
        } catch (e: Exception) {
            Log.e("TAG", "Error in d2: " + e.message)
            e.printStackTrace()
        }
    }
}