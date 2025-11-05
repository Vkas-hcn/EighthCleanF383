package ec

import android.app.Activity
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ecf.jk.Kac
import com.ecft.nice.MasterRu
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class EcTool {
    private val mPAH = PangShi()// 高价值
    private val mPangleAdImpl = PangShi("1") // 低价值
    private var idH = ""
    private var idL = ""

    fun setAdId(high: String, lowId: String) {
        idH = high
        idL = lowId
    }

    fun loadAd() {
        mPAH.lAd(idH)
        mPangleAdImpl.lAd(idL)
    }

    private var job: Job? = null
    fun showAd(ac: Activity) {
        EcLoad.sNumJump(0)
        if (ac is AppCompatActivity) {
            ac.onBackPressedDispatcher.addCallback {}
            job?.cancel()
            job = ac.lifecycleScope.launch {
                MasterRu.pE("ad_done")
                delay(Random.nextLong(EcLoad.gDTime()))
                if (EcLoad.isLoadH) {
                    Kac.nneCp(ac)
                }
                var isS = showAdIndex(ac)
                if (isS.not()) {
                    isS = showAdIndex(ac)
                }
                if (isS.not()) {
                    delay(500)
                    ac.finishAndRemoveTask()
                }
            }
        }
    }

    private var index = 0
    private fun showAdIndex(ac: Activity): Boolean {
        return when (index) {
            1 -> {
                index = 0
                mPangleAdImpl.shAd(ac)
            }

            else -> {
                index = 1
                mPAH.shAd(ac)
            }
        }
    }
}
