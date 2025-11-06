package com.september.nine.chong.user


import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import com.september.nine.chong.data.JksGo
import com.september.nine.chong.data.KeyCon
import com.september.nine.chong.ttuser.EcTtUtils
import com.september.nine.chong.ttuser.GetJkUtils
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.nio.charset.StandardCharsets

object GetUserUtils {

    interface CallbackMy {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }

    fun showAppVersion(): String {
        return KeyCon.openEc.packageManager.getPackageInfo(KeyCon.openEc.packageName, 0).versionName
            ?: ""
    }


    @SuppressLint("HardwareIds")
    fun adminData(): String {
        return JSONObject().apply {
            put("bbYKUh", "com.cleansonic.sda.junkvanish")
            put("eKJwHKc", KeyCon.aidec)
            put("ZSzgJQ", KeyCon.rdec)
//            put("ZSzgJQ", "555")
            put("Qag", showAppVersion())
            //referrerClickTimestampSeconds
            put("geJFjw", KeyCon.rctsec)
            //referrerClickTimestampServerSeconds
            put("mkJGew", KeyCon.rctssec)
            //installerPackageName
            put("dvJFueW", getISData(KeyCon.openEc))
        }.toString()
    }

    fun getISData(context: Context): String {
        val installerPackageName: String? = context.packageManager
            .getInstallerPackageName(context.packageName)
        return installerPackageName ?: ""
    }

    // Ktor client with 60 second timeout
    private val client = HttpClient(Android) {
        engine {
            connectTimeout = 60_000
            socketTimeout = 60_000
        }
        expectSuccess = false
    }

    fun postAdminData(callback: CallbackMy) {
        JksGo.showLog("postAdminData=${adminData()}")
        val jsonBodyString = JSONObject(adminData()).toString()
        val timestamp = System.currentTimeMillis().toString()
        val xorEncryptedString = jxData(jsonBodyString, timestamp)
        val base64EncodedString = Base64.encodeToString(
            xorEncryptedString.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )

        GetJkUtils.postPointFun(false, "config_R")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse = client.post(KeyCon.getAdminUrl()) {
                    header("timestamp", timestamp)
                    contentType(ContentType.Application.Json)
                    setBody(base64EncodedString)
                }

                if (response.status.value != 200) {
                    withContext(Dispatchers.Main) {
                        callback.onFailure("Unexpected code ${response.status.value}")
                        EcTtUtils.ConfigG(true, response.status.value.toString())
                    }
                    return@launch
                }

                try {
                    val timestampResponse = response.headers["timestamp"]
                        ?: throw IllegalArgumentException("Timestamp missing in headers")

                    val responseBody = response.bodyAsText()
                    val decodedBytes = Base64.decode(responseBody, Base64.DEFAULT)
                    val decodedString = String(decodedBytes, Charsets.UTF_8)
                    val finalData = jxData(decodedString, timestampResponse)
                    val jsonResponse = JSONObject(finalData)
                    val stringData = parseAdminRefData(jsonResponse.toString())
                    val jsonData = JSONObject(stringData)
                    GetJkUtils.initFb(jsonData)
                    Log.e("TAG", "onResponse-adminData: ${stringData}")

                    EcTtUtils.ConfigG(GetJkUtils.getAUTool(jsonData), "200")

                    withContext(Dispatchers.Main) {
                        callback.onSuccess(jsonData.toString())
                    }
                } catch (e: Exception) {
                    GetJkUtils.postPointFun(true, "cf_fail")
                    withContext(Dispatchers.Main) {
                        callback.onFailure("Decryption failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure("Request failed: ${e.message}")
                    GetJkUtils.postPointFun(true, "config_G", "getstring", "timeout")
                }
            }
        }
    }

    private fun jxData(text: String, timestamp: String): String {
        val cycleKey = timestamp.toCharArray()
        val keyLength = cycleKey.size
        return text.mapIndexed { index, char ->
            char.toInt().xor(cycleKey[index % keyLength].toInt()).toChar()
        }.joinToString("")
    }

    private fun parseAdminRefData(jsonString: String): String {
        try {
            val confString = JSONObject(jsonString).getJSONObject("ZZnUrCB").getString("conf")
            return confString
        } catch (e: Exception) {
            return ""
        }
    }

    fun postPutData(body: Any, callbackData: CallbackMy) {
        val jsonBodyString = JSONObject(body.toString()).toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: HttpResponse = client.post(KeyCon.getUpUrl()) {
                    contentType(ContentType.Application.Json)
                    setBody(jsonBodyString)
                }

                val responseData = response.bodyAsText()

                withContext(Dispatchers.Main) {
                    if (response.status.value !in 200..299) {
                        callbackData.onFailure("Unexpected code ${response.status.value}")
                    } else {
                        callbackData.onSuccess(responseData)
                    }
                }
            } catch (e: Exception) {
                JksGo.showLog("tba-Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    callbackData.onFailure(e.message ?: "Unknown error")
                }
            }
        }
    }

}
