package com.ecft.nice

import android.util.Log
import gg.GgUtils
import org.json.JSONObject

class AdminGo {
    var cango = false
     fun refreshLastConfigure() {
         Log.e("TAG", "cesh-1---:", )

         try {
//            val string = MasterRu.getStr("akv")
            val stringtest = testJsonData()
            GgUtils.reConfig(JSONObject(stringtest))
            if (cango.not()) {
                cango = true
                GgUtils.a2()
            }
        } catch (e: Exception) {
            MasterRu.pE("cf_fail", e.stackTraceToString())
        }
    }

    fun testJsonData(): String {
       return  """
           {
             "a_time": "60-60-1000",
             "post_type": "show",
             "a_u_s": "pop",
             "bk_v": "3616318175247400-3616318175247400",
             "popSnn": "30-30-60-8-16-80-500-800-40-10",
             "showGV": "981772962-981772963",
             "jmk": "fd2grg5ds4a4r4ef-bfjkl87fk6efhwf6",
             "all_kg": "qpwo-eiru-tyal-skdj-polk"
           }
       """.trimIndent()
    }
}