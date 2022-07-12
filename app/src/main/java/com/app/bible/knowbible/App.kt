package com.app.bible.knowbible

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.app.bible.knowbible.ads.AdaptiveBannerAdLoader
import com.app.bible.knowbible.ads.NativeAdLoader
import com.app.bible.knowbible.mvvm.model.ArticleModel
import com.app.bible.knowbible.utility.LogHelper
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import kotlin.collections.ArrayList

class App : Application() {
    companion object {
        var articlesData: ArrayList<ArticleModel>? = null

        lateinit var instance: App
            private set

        const val EVENT_ADS_YES = "ADS_YES"
        const val EVENT_ADS_NO = "ADS_NO"
    }

//    val nativeAdLoader by lazy { NativeAdLoader(this) }
//    val bannerAdLoader by lazy { AdaptiveBannerAdLoader(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
//        MobileAds.initialize(this)
        FirebaseApp.initializeApp(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun logToFirebase(event: String) {
        LogHelper.logEventToFireBase(event)
    }
}