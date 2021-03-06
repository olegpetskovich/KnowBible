package com.app.bible.knowbible.push

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.app.bible.knowbible.utility.SaveLoadData
import com.app.bible.knowbible.utility.Utils.Companion.getCurrentTime
import com.app.bible.knowbible.utility.Utils.Companion.log
import java.text.SimpleDateFormat
import java.util.*

class UnlockBR : BroadcastReceiver() {
    companion object {
        const val SHOWED_PUSH_TIME_KEY = "SHOWED_PUSH_TIME_KEY"
    }

    private val dayLength = 24 //hours
    private val startPushTime = 9 //hours. То есть пуши должны приходить не раньше 9 часов утра
    private val endPushTime = 21 //hours. То есть пуши должны приходить не позже 21 часов вечера

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val kgMan = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (kgMan.isKeyguardLocked.not()) {
                val saveLoadData = SaveLoadData(context)

                val timeInHours = SimpleDateFormat("H", Locale.getDefault()).format(Date()).toInt()
                log(timeInHours.toString())

                if (isNewDay(saveLoadData) && timeInHours >= startPushTime && timeInHours <= endPushTime) {
                    PushCreator.send(context)
                    saveLoadData.saveLong(SHOWED_PUSH_TIME_KEY, getCurrentTime())
                }
            }
        }
    }

    fun selfRegister(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(this, filter)
    }

    private fun isNewDay(saveLoadData: SaveLoadData): Boolean {
        val curTime = getCurrentTime()
        val savedTime = saveLoadData.loadLong(SHOWED_PUSH_TIME_KEY)
        val result = curTime - savedTime
        return result >= dayLength
    }
}