package com.app.bible.knowbible.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.utility.LogHelper.logEventToFireBase
import com.app.bible.knowbible.utility.Utility
import com.app.bible.knowbible.utility.Utility.Companion.log
import com.app.bible.knowbible.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class NativeAdLoader(private val context: Context) {
    private var nativeAdLoader: AdLoader? = null
    private val nativeAdLiveDataValue: MutableLiveData<NativeAd?> = MutableLiveData(null)
    val nativeAdLiveData: LiveData<NativeAd?> get() = nativeAdLiveDataValue

    fun loadNativeAd() {
        if (!Utility.isNetworkAvailable(context)) return

        if (nativeAdLiveDataValue.value != null || nativeAdLoader != null && nativeAdLoader!!.isLoading) return

        nativeAdLoader =
            AdLoader.Builder(context, context.getString(R.string.NATIVE_AD_ID))
                .forNativeAd { nativeAd: NativeAd? ->
                    logEventToFireBase("NATIVE_WAS_LOADED")
                    nativeAdLiveDataValue.value = nativeAd
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError?) {

                    }

                    override fun onAdClicked() {
                        logEventToFireBase("NATIVE_WAS_CLICKED")
                    }

                    override fun onAdLoaded() {

                    }

                    override fun onAdImpression() {
                        logEventToFireBase("NATIVE_WAS_SHOWED")
                        nativeAdLiveDataValue.value = null
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build()).build()
                )
                .build()

        log("MyTag", "N---------------------------N")
        logEventToFireBase("NATIVE_WAS_REQUESTED")
        nativeAdLoader!!.loadAd(AdRequest.Builder().build())
    }

    fun showNativeAd(nativeLayout: FrameLayout) {
        if (nativeAdLiveDataValue.value != null) {
            val adView = LayoutInflater.from(context)
                .inflate(R.layout.native_layout, null) as NativeAdView
            fillNativeView(nativeAdLiveDataValue.value!!, adView)
            nativeLayout.removeAllViews()
            nativeLayout.addView(adView) // добавляем населённую вьюху во фрагмент
        }
    }

    fun cleanAppOpenAd() {
        nativeAdLiveDataValue.value = null
        log("MyTag", "Native Ad was cleaned.")
    }


    private fun fillNativeView(nativeAd: NativeAd, adView: NativeAdView) {
        val mediaView: MediaView = adView.findViewById(R.id.ad_media)
        adView.mediaView = mediaView

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline is guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        if (nativeAd.price == null) {
            adView.priceView?.visibility = View.INVISIBLE
        } else {
            adView.priceView?.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }
        if (nativeAd.store == null) {
            adView.storeView?.visibility = View.INVISIBLE
        } else {
            adView.storeView?.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView?.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toFloat()
            adView.starRatingView?.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }
}
