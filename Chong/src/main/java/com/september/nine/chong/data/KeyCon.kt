package com.september.nine.chong.data

import android.app.Application

object KeyCon {
    lateinit var openEc : Application
    var isOpenNotification = false
    var canStringFcm = "CmgjGecsaD"
    // 显示图标
    var launchState by CunUtils.string("mkhju", "")
    // user data
    var udec by CunUtils.string("gjtird", "")
    // ref data
    var rdec by CunUtils.string("brtmkref", "")
    // android id
    var aidec by CunUtils.string("qmkebtwi", "")
    //referrerClickTimestampSeconds
    var rctsec by CunUtils.string("bbgtf", "")
    //referrerClickTimestampServerSeconds
    var rctssec by CunUtils.string("mjkjyq", "")

    var fcToolpo  by CunUtils.boolean("sdfcrewaa", false)

    fun getUpUrl(): String {
        return "https://test-freeing.cleansonicsdajunkvanish.com/steep/hightail/malice"
    }

    fun getAdminUrl(): String {
        return "https://yqs.cleansonicsdajunkvanish.com/apitest/berbe/dest/"
    }

    fun getPangKey(): String {
        return "8580262"
    }

    fun getApplyKey(): String {
        return "5MiZBZBjzzChyhaowfLpyR"
    }
}