package com.ecft.nice

import android.util.Log
import ec.EcLoad
import org.json.JSONObject

class AdminGo {
    var cango = false
     fun refreshLastConfigure() {
         val udec = MasterRu.getStr("gjtird")
         try {
            EcLoad.reConfig(JSONObject(udec))
            if (cango.not()) {
                cango = true
                EcLoad.a2()
            }
        } catch (e: Exception) {
            MasterRu.pE("cf_fail", e.stackTraceToString())
        }
    }


}