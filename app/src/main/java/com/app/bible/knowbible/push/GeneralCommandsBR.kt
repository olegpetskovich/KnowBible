package com.app.bible.knowbible.push

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.app.bible.knowbible.push.PushCreator.pushId

class GeneralCommandsBR : BroadcastReceiver() {
    companion object {
        const val R_SERVICE_COMMAND_CANCEL_PUSH = "R_SERVICE_COMMAND_CANCEL_PUSH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            R_SERVICE_COMMAND_CANCEL_PUSH -> {
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .cancel(pushId)
            }
        }
    }
}