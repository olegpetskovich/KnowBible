package com.app.bible.knowbible.push.accessory

import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import java.util.*

val Int.sec get() = this * 1000L
val Int.min get() = 60 * sec
val Int.hour get() = 60 * min

const val PREFERENCES_KEY = "PREFERENCES_KEY"

fun getPushInterval(time: Long): Long {
    return when (System.currentTimeMillis() - time) {
        // this in RELEASE
        in 1.sec..2.hour -> 5.hour // 4.5h
        in 2.hour..6.hour -> 4.hour // 3.5h
        in 6.hour..12.hour -> 3.hour // 2.5h
        else -> 61.min // 1h
    }
}

fun Context.isRestrictedByTimeAndState(): Boolean {
    if (this.isForegroundVisibility()) return true
    val km = this.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    if (km.inKeyguardRestrictedInputMode()) return true
    val hourOfDay = Calendar.getInstance().time.hours
    if (hourOfDay !in 8..22) return true
    val pm = this.getSystemService(Context.POWER_SERVICE) as PowerManager?
    if (!pm!!.isScreenOn) return true
    return false
}

private fun Context.isForegroundVisibility(): Boolean {
    val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningProc = am.runningAppProcesses
    if (runningProc != null) {
        for (info in runningProc) {
            if (info.processName == this.packageName) {
                val thisProcess = ActivityManager.RunningAppProcessInfo()
                ActivityManager.getMyMemoryState(thisProcess)
                val isInBackground =
                    thisProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                return !isInBackground
            }
        }
    }
    return false
}

const val LAST_REG_PUSH_TIME_KEY = "LAST_REG_PUSH_TIME_KEY"

fun Context.saveLastRegPushTime() {
    val prefs = this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
    prefs.edit().putLong(LAST_REG_PUSH_TIME_KEY, System.currentTimeMillis()).apply()
}

fun Context.getLastRegPushTime(): Long {
    val prefs = this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
    return prefs.getLong(LAST_REG_PUSH_TIME_KEY, 0L)
}

const val LAST_OPTIMIZE_TIME_KEY = "LAST_OPTIMIZE_TIME_KEY"

fun Context.saveLastOptimizeTime() {
    val prefs = this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
    prefs.edit().putLong(LAST_OPTIMIZE_TIME_KEY, System.currentTimeMillis()).apply()
}

fun Context.getLastOptimizeTime(): Long {
    val prefs = this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
    return prefs.getLong(LAST_OPTIMIZE_TIME_KEY, 0L)
}