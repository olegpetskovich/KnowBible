package com.app.bible.knowbible.ads

import android.app.Activity
import android.content.Context
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.utility.LogHelper.logEventToFireBase
import com.app.bible.knowbible.utility.Utility
import com.app.bible.knowbible.utility.Utility.Companion.log
import com.app.bible.knowbible.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdLoader(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null
    private var interLoadedListener: (() -> Unit)? = null

    fun loadInterstitialAd() {
        if (!Utility.isNetworkAvailable(context)) return

        if (interstitialAd != null) return

        val adRequest = AdRequest.Builder().build()

        logEventToFireBase("INTER_WAS_REQUESTED")
        InterstitialAd.load(
            context.applicationContext,
            context.getString(R.string.INTER_AD_ID),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log("MyTag", adError.message)
                    interLoadedListener?.invoke()
                    interLoadedListener = null //Обнуляем объект, чтобы не держать его в памяти

                    logEventToFireBase("INTER_WAS_FAILED_TO_LOAD")
                    this@InterstitialAdLoader.interstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interLoadedListener?.invoke()
                    interLoadedListener = null //Обнуляем объект, чтобы не держать его в памяти

                    logEventToFireBase("INTER_WAS_LOADED")
                    this@InterstitialAdLoader.interstitialAd = interstitialAd
                }
            })
    }

    fun setInterstitialAdLoadedListener(interLoadedListener: () -> Unit) {
        log("MyTag", "setInterstitialAdLoadedListener")
        this.interLoadedListener = interLoadedListener
    }

    fun getInterstitialAd(): InterstitialAd? {
        return interstitialAd
    }

    fun showInterstitialAd(activity: Activity, listener: () -> Unit = {}) {
        interstitialAd?.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {}

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    logEventToFireBase("INTER_WAS_FAILED_TO_SHOW")
                    listener()
                    cleanInterstitialAd()
                }

                override fun onAdShowedFullScreenContent() {
                    logEventToFireBase("INTER_WAS_SHOWED_FULLSCREEN")
                    listener()
                    cleanInterstitialAd()
                }
            }

            show(activity)
        } ?: listener()
    }

    fun cleanInterstitialAd() {
        interstitialAd = null
        log("MyTag", "Inter Ad was cleaned.")
    }
}
