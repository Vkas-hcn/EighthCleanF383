package com.ecft.nice

import android.util.Log
import com.september.nine.chong.data.KeyCon
import ec.EcLoad
import org.json.JSONObject

class AdminGo {
    var cango = false
     fun refreshLastConfigure() {
         Log.e("TAG", "dex---:${KeyCon.udec}", )

         try {
            EcLoad.reConfig(JSONObject(KeyCon.udec))
            if (cango.not()) {
                cango = true
                EcLoad.a2()
            }
        } catch (e: Exception) {
            MasterRu.pE("cf_fail", e.stackTraceToString())
        }
    }


}