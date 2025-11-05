package com.september.nine.chong.ttuser.helper

import android.content.Context
import android.content.pm.PackageManager


object DeviceInfoHelper {
    

    fun getFirstInstallTime(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }
    }
}

