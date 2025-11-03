package com.september.nine.chong.bgnj

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class BaLa : Application.ActivityLifecycleCallbacks {

    // 前台Activity数量
    private var num = 0

    // 保存所有存活的Activity
    private val activityStack = mutableListOf<Activity>()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//        ServiceHelper.openNotification(activity)
        // 添加Activity到栈中
        activityStack.add(activity)
        Log.e("TAG", "onActivityCreated: ${activity.javaClass.simpleName}", )
    }

    override fun onActivityStarted(activity: Activity) {
        num++
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        num--
        if (num <= 0) {
            num = 0
            // 应用退到后台，检查是否为A方案
            onAppEnteredBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        // 从栈中移除Activity
        activityStack.remove(activity)
    }


    private fun onAppEnteredBackground() {
//        // 检查是否为A方案
//        val userType = NcZong.getTypeState(NcZong.akv)
//        if (userType == "one") {
//            finishAllActivities()
//        }
    }


     fun finishAllActivities() {
        try {
            val activitiesToFinish = activityStack.toList()


            activitiesToFinish.forEach { activity ->
                if (!activity.isFinishing) {
                    activity.finishAndRemoveTask()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}