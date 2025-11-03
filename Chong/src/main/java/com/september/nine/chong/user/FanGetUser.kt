package com.september.nine.chong.user

import com.september.nine.chong.data.JksGo

object FanGetUser {
    fun getAUser(){
        GetUserUtils.postAdminData(object : GetUserUtils.CallbackMy {
            override fun onSuccess(response: String) {
            }

            override fun onFailure(error: String) {
            }
        })
    }
}