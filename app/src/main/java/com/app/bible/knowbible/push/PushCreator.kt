package com.app.bible.knowbible.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.bible.knowbible.R
import com.app.bible.knowbible.push.accessory.cancelGoal
import com.app.bible.knowbible.push.accessory.clickGoal
import com.app.bible.knowbible.push.accessory.isRestrictedByTimeAndState

object PushCreator {
    private const val notifChannelName = "Notification Channel"
    val pushId = 777

    fun send(context: Context, app: String = "") {
        if (context.isRestrictedByTimeAndState()) return

        val nm = context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        nm.createPushChannel()
        val body = RemoteViews(context.packageName, R.layout.push_layout)
        body.apply {
            setOnClickPendingIntent(R.id.btnRead, context.clickGoal())
            setOnClickPendingIntent(R.id.rootR, context.clickGoal())
            setOnClickPendingIntent(R.id.btnCancel, context.cancelGoal())
        }

        val ncb = context.returnBuilder(body)
        nm.notify(pushId, ncb.build())
    }

    private fun NotificationManager.createPushChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    notifChannelName,
                    notifChannelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
            channel.enableVibration(true)
            channel.enableLights(true)
            this.createNotificationChannel(channel)
        }
    }

    private fun Context.returnBuilder(
        body: RemoteViews,
    ): NotificationCompat.Builder {
        return when (Build.MANUFACTURER.toString()) {
            "HUAWEI" -> NotificationCompat.Builder(this, notifChannelName)
                .setSmallIcon(android.R.color.transparent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomBigContentView(body)
                .setCustomHeadsUpContentView(body)

            else -> NotificationCompat.Builder(this, notifChannelName)
                .setSmallIcon(android.R.color.transparent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setFullScreenIntent(clickGoal(), true)
                .setCustomBigContentView(body)
                .setCustomHeadsUpContentView(body)
        }
    }
}