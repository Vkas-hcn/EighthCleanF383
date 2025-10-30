package gg

import android.app.Activity
import android.util.Log
import com.ggc.show.MasterRu
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import com.bytedance.sdk.openadsdk.api.model.PAGAdEcpmInfo
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import org.json.JSONObject


/**
 * Date：2025/7/10
 * Describe:
 */
class PAI(val t: String = "") {
    private var isL = false
    private var lT = 0L
    private var mAd: PAGInterstitialAd? = null

    fun lAd(id: String) {
        Log.e("TAG", "PangleAdImpl-id: $id", )

        if (id.isBlank()) return
        if (isL && System.currentTimeMillis() - lT < 61000) return
        if (mAd != null) return
        isL = true
        lT = System.currentTimeMillis()
        MasterRu.pE("advertise_req$t")
        PAGInterstitialAd.loadAd(
            id,
            PAGInterstitialRequest(MasterRu.mApp),
            object : PAGInterstitialAdLoadCallback {
                override fun onError(pagErrorModel: PAGErrorModel) {
                    Log.e("TAG", "onError: ${pagErrorModel.errorCode}_${pagErrorModel.errorMessage}", )
                    isL = false
                    MasterRu.pE(
                        "advertise_fail$t",
                        "${pagErrorModel.errorCode}_${pagErrorModel.errorMessage}"
                    )

                }

                override fun onAdLoaded(pagInterstitialAd: PAGInterstitialAd) {
                    Log.e("TAG", "onAdLoaded: success", )
                    mAd = pagInterstitialAd
                    isL = false
                    MasterRu.pE("advertise_get$t")
                }
            })
    }

    fun isReadyAd(): Boolean {
        return mAd?.isReady == true
    }

    fun shAd(a: Activity): Boolean {
        val ad = mAd
        if (ad != null) {
            val time = System.currentTimeMillis()
            MasterRu.pE("advertise_show")
            ad.setAdInteractionCallback(object : PAGInterstitialAdInteractionCallback() {
                override fun onAdReturnRevenue(pagAdEcpmInfo: PAGAdEcpmInfo?) {
                    super.onAdReturnRevenue(pagAdEcpmInfo)
                    MasterRu.pE("advertise_show_t", "${(System.currentTimeMillis() - time) / 1000}")
                    pagAdEcpmInfo?.let {
                        val adRevenueData = AFAdRevenueData(
                            it.adnName,  // monetizationNetwork
                            MediationNetwork.CUSTOM_MEDIATION,  // mediationNetwork
                            "USD",  // currencyIso4217Code
                            it.cpm.toDouble() / 1000 // revenue
                        )

                        val additionalParameters: MutableMap<String, Any> = HashMap()
                        additionalParameters[AdRevenueScheme.COUNTRY] = it.country
                        additionalParameters[AdRevenueScheme.AD_UNIT] = it.adUnit
                        additionalParameters[AdRevenueScheme.AD_TYPE] = "i"
                        additionalParameters[AdRevenueScheme.PLACEMENT] = it.placement
                        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
                        postValue(it)
                    }
                    GgUtils.adShow()
                }

                override fun onAdDismissed() {
                    super.onAdDismissed()
                    a.finishAndRemoveTask()
                    GgUtils.isSAd = false
                }

                override fun onAdShowFailed(pagErrorModel: PAGErrorModel) {
                    super.onAdShowFailed(pagErrorModel)
                    a.finishAndRemoveTask()
                    MasterRu.pE(
                        "advertise_fail_api",
                        "${pagErrorModel.errorCode}_${pagErrorModel.errorMessage}"
                    )
                    GgUtils.isSAd = false
                    GgUtils.mAdC.loadAd()
                }
            })
            ad.show(a)
            mAd = null
            return true
        }
        return false
    }


    private fun postValue(si: PAGAdEcpmInfo) {
        MasterRu.postAd(
            JSONObject()
                .put("sc", si.cpm.toDouble() * 1000)//ad_pre_ecpm
                .put("martinez", "USD")//currency
                .put("ban", si.adnName)//ad_network
                .put("cal", "pangle")//ad_source_client
                .put("college", si.placement)//ad_code_id
                .put("studio", si.adUnit)//ad_pos_id
                .put("bug", si.adFormat)//ad_format
                .toString()
        )
        val cpm = si.cpm.toDouble() / 1000
        GgUtils.postEcpm(cpm)
    }

}