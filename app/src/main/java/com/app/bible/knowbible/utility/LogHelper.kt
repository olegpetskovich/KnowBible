package com.app.bible.knowbible.utility

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object LogHelper {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logEventToFireBase(event: String) {
        Log.d("MyTag", event)
        firebaseAnalytics.logEvent(event) {}
    }
}