package com.september.nine.chong.ttuser.helper

import com.september.nine.chong.ttuser.builder.IJsonMerger
import org.json.JSONObject


class JsonMergeHelper : IJsonMerger {
    

    override fun merge(target: JSONObject, source: JSONObject) {
        val keys = source.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            target.put(key, source.get(key))
        }
    }
    
    companion object {

        fun mergeMultiple(base: JSONObject, vararg others: JSONObject): JSONObject {
            val helper = JsonMergeHelper()
            others.forEach { other ->
                helper.merge(base, other)
            }
            return base
        }
    }
}

