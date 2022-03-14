package com.app.bible.knowbible.ads

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.utility.LogHelper.logEventToFireBase
import com.app.bible.knowbible.utility.Utility
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.app.bible.knowbible.R
import com.app.bible.knowbible.utility.Utility.Companion.log

class RewardedAdsLoader(private val context: Context) {
    var rewardedAd: RewardedAd? = null
        private set

    fun loadRewardedAd() {
        if (!Utility.isNetworkAvailable(context)) return

        if (rewardedAd != null) return

        val adRequest = AdManagerAdRequest.Builder().build()

        logEventToFireBase("REWARDED_WAS_REQUESTED")
        RewardedAd.load(
            context.applicationContext,
            context.getString(R.string.REWARDED_AD_ID),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log("MyTag", adError.message)
                    logEventToFireBase("REWARDED_WAS_FAILED_TO_LOAD")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad : RewardedAd) {
                    logEventToFireBase("REWARDED_WAS_LOADED")
                    rewardedAd = ad
                }
            })
    }

    fun showRewardedAd(activity: AppCompatActivity, onSuccess: (RewardItem) -> Unit = {},onDismiss: () -> Unit = {}) {
        rewardedAd?.apply {
            var isSuccess = false
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    if(!isSuccess)
                        onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    logEventToFireBase("REWARDED_WAS_FAILED_TO_SHOW")
                    onDismiss()
                }

                override fun onAdShowedFullScreenContent() {
                    logEventToFireBase("REWARDED_WAS_SHOWED_FULLSCREEN")
                }
            }

            show(activity){
                isSuccess = true
                onSuccess(it)
            }
            cleanRewardedAd()
        } ?: onDismiss()
    }

    private fun cleanRewardedAd() {
        rewardedAd = null
        log("MyTag", "Inter Ad was cleaned.")
    }
}
