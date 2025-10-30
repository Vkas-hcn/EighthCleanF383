package gg

import android.app.Activity
import android.util.Log
import com.ggc.show.MasterRu
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.interstitial.api.TUInterstitialListener
import org.json.JSONObject
import kotlin.apply
import kotlin.collections.set
import kotlin.let
import kotlin.text.isBlank


class TAI(val tag: String) : TUInterstitialListener {

    private var isLoading = false
    private var lT = 0L
    private var mAd: TUInterstitial? = null


    // 加载广告
    fun lAd(id: String) {
        Log.e("TAG", "ToponAdImpl-id: $id", )

        if (id.isBlank()) return
        if (isLoading && System.currentTimeMillis() - lT < 61000) return
        if (isReadyAd()) return
        isLoading = true
        lT = System.currentTimeMillis()
        MasterRu.pE("advertise_req$tag")
        mAd = TUInterstitial(MasterRu.mApp, id)
        mAd?.setAdListener(this)
        mAd?.load()
    }

    fun isReadyAd(): Boolean {
        return mAd?.isAdReady == true
    }

    private var call: (() -> Unit)? = null
    private var time = 0L

    // 显示广告
    fun shAd(a: Activity): Boolean {
        val ad = mAd
        if (ad?.isAdReady == true) {
            call = {
                a.finishAndRemoveTask()
                GgUtils.isSAd = false
            }
            time = System.currentTimeMillis()
            MasterRu.pE("advertise_show")
            ad.show(a)
            mAd = null
            return true
        }
        return false
    }

    override fun onInterstitialAdLoaded() {
        isLoading = false
        MasterRu.pE("advertise_get$tag")
        Log.e("TAG", "onInterstitialAdLoaded:")

    }

    override fun onInterstitialAdLoadFail(p0: AdError?) {
        isLoading = false
        MasterRu.pE("advertise_fail$tag", "${p0?.code}")
    }

    override fun onInterstitialAdClicked(p0: TUAdInfo?) {}

    override fun onInterstitialAdShow(p0: TUAdInfo?) {
        MasterRu.pE("advertise_show_t", "${(System.currentTimeMillis() - time) / 1000}")
        p0?.let {
            postP(it)
        }
        GgUtils.adShow()
    }

    override fun onInterstitialAdClose(p0: TUAdInfo?) {
        call?.invoke()
        call = null
    }

    override fun onInterstitialAdVideoStart(p0: TUAdInfo?) {}

    override fun onInterstitialAdVideoEnd(p0: TUAdInfo?) {}

    override fun onInterstitialAdVideoError(p0: AdError?) {
        MasterRu.pE("advertise_fail_api", "${p0?.code}_${p0?.desc}")
        call?.invoke()
        call = null
        GgUtils.mAdC.loadAd()
    }

    private fun postP(ad: TUAdInfo) {
        MasterRu.postAd(JSONObject().apply {
            put("sc", ad.publisherRevenue * 1000000)//ad_pre_ecpm
            put("martinez", ad.currency)//currency
            put("ban", ad.networkName)//ad_network
            put("cal", "topon")//ad_source_client
            put("college", ad.placementId)//ad_code_id
            put("studio", ad.adsourceId)//ad_pos_id
            put("bug", ad.format)//ad_format
        }.toString())

        val cpm = ad.publisherRevenue

        // pangle
        val adRevenueData = AFAdRevenueData(
            ad.networkName,  // monetizationNetwork
            MediationNetwork.TOPON,  // mediationNetwork
            "USD",  // currencyIso4217Code
            cpm // revenue
        )

        val additionalParameters: MutableMap<String, Any> = HashMap()
        additionalParameters[AdRevenueScheme.COUNTRY] = ad.country
        additionalParameters[AdRevenueScheme.AD_UNIT] = ad.adsourceId
        additionalParameters[AdRevenueScheme.AD_TYPE] = "i"
        additionalParameters[AdRevenueScheme.PLACEMENT] = ad.placementId
        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)

        GgUtils.postEcpm(cpm)
    }

}