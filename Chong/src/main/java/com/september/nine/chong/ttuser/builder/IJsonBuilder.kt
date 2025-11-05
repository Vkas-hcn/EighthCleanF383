package com.september.nine.chong.ttuser.builder

import android.content.Context
import org.json.JSONObject

interface IJsonBuilder {

    fun buildJson(context: Context): JSONObject
    

    fun buildJsonString(context: Context): String {
        return buildJson(context).toString()
    }
}


interface IBaseDataProvider {

    fun provideBaseData(context: Context): JSONObject
}


interface IJsonMerger {

    fun merge(target: JSONObject, source: JSONObject)
}

