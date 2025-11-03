package com.eighth.day.lunar.ais.cuo.naot

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.webkit.WebView
import com.september.nine.chong.data.JksGo

fun Context.getCurrentProcessName(): String? {
    val pid = Process.myPid()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    return activityManager?.runningAppProcesses
        ?.firstOrNull { it.pid == pid }
        ?.processName
}


fun Context.isMainProcess(): Boolean {
    return packageName == getCurrentProcessName()
}

class Ais : Application() {
    override fun onCreate() {
        super.onCreate()
        if (isMainProcess()) {
            JksGo.cesh(this)
        } else {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    WebView.setDataDirectorySuffix(
                        getProcessName() ?: "default"
                    )
                }
            }
        }
    }
}