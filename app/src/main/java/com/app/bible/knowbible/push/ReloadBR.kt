package com.app.bible.knowbible.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ReloadBR : BroadcastReceiver() {
    companion object {
        const val RELOAD_R_SERVICE_ACTION = "RELOAD_R_SERVICE_ACTION"
        val actionSet = setOf(Intent.ACTION_BOOT_COMPLETED, RELOAD_R_SERVICE_ACTION)
    }

    override fun onReceive(context: Context, intent: Intent) {
        //if (!isPolicyAccepted()) return // RELEASE check policy accepted !!!
        if (actionSet.contains(intent.action))
            ContextCompat.startForegroundService(context, Intent(context, RService::class.java))
    }
}