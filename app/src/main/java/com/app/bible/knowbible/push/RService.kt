package com.app.bible.knowbible.push

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import com.app.bible.knowbible.R
import com.app.bible.knowbible.push.accessory.clickGoal
import com.app.bible.knowbible.push.accessory.sec

class RService : Service() {
    companion object {
        val RELOAD_SERVICE_AFTER = 5.sec
        const val R_SERVICE_CHANNEL_ID = "KnowBible Main Foreground Service Channel"
        const val R_SERVICE_NOTIFICATION_ID = 704202201
    }

    private val mUnlockBR by lazy { UnlockBR() }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithPanel().also {
            getBR()
        }
    }

    private fun RService.getBR() {
        mUnlockBR.selfRegister(this@RService)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true).also {
            forgetBR()
            rServiceReload()
        }
    }

    private fun forgetBR() {
        unregisterReceiver(mUnlockBR)
    }

    private fun startForegroundServiceWithPanel() {
        val nn = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nn.createServiceChannel()
        val panel = setPanelOnClickGoals()
        val builder = NotificationCompat.Builder(this, R_SERVICE_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(android.R.color.transparent)
            .setCustomContentView(panel)
        try {
            startForeground(R_SERVICE_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
        }
    }

    private fun setPanelOnClickGoals() =
        RemoteViews(applicationContext.packageName, R.layout.r_service_panel_view).apply {
            setOnClickPendingIntent(R.id.tvPanelText, clickGoal())
        }

    private fun NotificationManager.createServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                R_SERVICE_CHANNEL_ID,
                R_SERVICE_CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.enableVibration(false)
            this.createNotificationChannel(channel)
        }
    }

    override fun onBind(int: Intent): IBinder? {
        return null
    }

    private fun rServiceReload() {
        val intent = Intent(this, ReloadBR::class.java).also {
            it.action = ReloadBR.RELOAD_R_SERVICE_ACTION
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getBroadcast(this, 0, intent, flags)

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reloadInTime = System.currentTimeMillis() + RELOAD_SERVICE_AFTER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
            AlarmManagerCompat
                .setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, reloadInTime, pending)
        } else {
            AlarmManagerCompat
                .setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, reloadInTime, pending)
        }
    }
}