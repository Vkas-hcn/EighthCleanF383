package com.ggc.show

import gg.GgUtils
import org.json.JSONObject

class AdminGo {
    var cango = false
     fun refreshLastConfigure() {
        try {
            val string = MasterRu.getStr("akv")
            GgUtils.reConfig(JSONObject(string))
            if (cango.not()) {
                cango = true
                GgUtils.a2()
            }
        } catch (e: Exception) {
            MasterRu.pE("cf_fail", e.stackTraceToString())
        }
    }
}