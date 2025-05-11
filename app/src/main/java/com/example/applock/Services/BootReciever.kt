package com.example.applock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.applock.utils.LockUtils
import com.example.applock.utils.PreferenceUtils

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = PreferenceUtils(context)

            // Start app lock service if pattern is set and service was running
            if (LockUtils.isPatternSet(context) && prefs.isServiceRunning()) {
                val serviceIntent = Intent(context, AppLockService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}