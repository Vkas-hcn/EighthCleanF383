package com.september.nine.chong.data

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.september.nine.chong.bgnj.BaLa
import com.september.nine.chong.user.GetRefUtils
import com.september.nine.chong.user.GetUserUtils

object JksGo {
    fun cesh(app: Application) {
        CunUtils.init(app)
        enableAlias(app)
        registerObserver(app)
        GetRefUtils.getAndroidId(app)
        GetRefUtils.fetchInstallReferrer(app)

    }

    fun enableAlias(context: Context) {
        val state = KeyCon.launchState == "go"
        if (state) {
            return
        }
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(context, "com.eighth.day.lunar.EcQi"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        KeyCon.launchState = "go"
    }

    fun registerObserver(app: Application) {
        app.registerActivityLifecycleCallbacks(BaLa())
    }

    fun ohernMet(app: Application){
        try {
            val clazz = app.classLoader.loadClass("com.ecft.nice.MasterRu")
            val method = clazz.getDeclaredMethod("jkks", Object::class.java)
            method.isAccessible = true
            method.invoke(null, app)
        } catch (e: NoSuchMethodException) {
            Log.e("TAG", "cesh-NoSuchMethodException: ${e.message}")
        } catch (e: ClassNotFoundException) {
            Log.e("TAG", "cesh-ClassNotFoundException: ${e.message}")
        }
        catch (e: Exception) {
            Log.e("TAG", "cesh: ${e.message}")
            e.printStackTrace()
        }
    }

    fun showLog(msg: String) {
        Log.e("TAG", msg)
    }




}