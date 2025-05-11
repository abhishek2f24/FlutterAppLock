package com.example.applock.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.applock.services.AppMonitorService
import com.example.applock.utils.PrefManager

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefManager = PrefManager(context)
            
            // Only start the service if it was enabled before reboot
            if (prefManager.isServiceEnabled()) {
                val serviceIntent = Intent(context, AppMonitorService::class.java)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }