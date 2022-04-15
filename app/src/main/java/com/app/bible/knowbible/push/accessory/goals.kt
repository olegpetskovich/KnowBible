package com.app.bible.knowbible.push.accessory

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.app.bible.knowbible.mvvm.view.activity.MainActivity
import com.app.bible.knowbible.push.GeneralCommandsBR

fun Context.clickGoal(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    return PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
}

fun Context.cancelGoal(): PendingIntent {
    val intent = Intent(this, GeneralCommandsBR::class.java).apply {
        action = GeneralCommandsBR.R_SERVICE_COMMAND_CANCEL_PUSH
    }
    return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}
