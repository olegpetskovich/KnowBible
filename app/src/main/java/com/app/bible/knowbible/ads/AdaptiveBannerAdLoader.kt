package com.app.bible.knowbible.ads

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import com.app.bible.knowbible.utility.LogHelper.logEventToFireBase
import com.google.android.gms.ads.*
import com.app.bible.knowbible.R


class AdaptiveBannerAdLoader(private val context: Context) {

    fun loadBanner(activity: Activity, adViewContainer: View, banner: AdView) {
        banner.adUnitId = context.getString(R.string.BANNER_AD_ID)
        banner.adSize = getAdaptiveBannerSize(activity, adViewContainer)

        val adRequest = AdRequest
            .Builder().build()

        banner.adListener = object : AdListener() {
            override fun onAdLoaded() {
                logEventToFireBase("BANNER_LOADED")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdOpened() {
            }

            override fun onAdClicked() {
                logEventToFireBase("BANNER_CLICKED")
            }

            override fun onAdClosed() {
            }

            override fun onAdImpression() {
                logEventToFireBase("BANNER_SHOWED")
            }
        }

        logEventToFireBase("BANNER_REQUESTED")
        banner.loadAd(adRequest)
    }

    private fun getAdaptiveBannerSize(activity: Activity, adViewContainer: View): AdSize {
        // вычисление размера адаптивного баннера по контейнеру и параметрам дисплея
        val display = activity.windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)

        val density = displayMetrics.density

        var adWidthPixels = adViewContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = displayMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}