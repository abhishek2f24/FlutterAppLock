package com.example.applock.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.applock.R
import com.example.applock.activities.MainActivity
import com.example.applock.utils.AppLockManager
import com.example.applock.utils.PrefManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppMonitorService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    
    private lateinit var appLockManager: AppLockManager
    private lateinit var prefManager: PrefManager
    
    private var lastCheckedPackage: String? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_lock_service_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        appLockManager = AppLockManager.getInstance(this)
        prefManager = PrefManager(this)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        startMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
    
    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                if (prefManager.isServiceEnabled()) {
                    checkCurrentApp()
                }
                delay(300) // Check every 300ms
            }
        }
    }
    
    private suspend fun checkCurrentApp() {
        val currentPackage = appLockManager.getCurrentForegroundApp()
        
        if (currentPackage != null && 
            currentPackage != lastCheckedPackage && 
            currentPackage != packageName) {
            
            // Check if the app is locked
            if (appLockManager.isAppLocked(currentPackage)) {
                appLockManager.showLockScreen(currentPackage)
            }
            
            lastCheckedPackage = currentPackage
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Lock Service"
            val descriptionText = "Monitors app usage to enforce app locks"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Lock Active")
            .setContentText("Monitoring apps to keep your data secure")
            .setSmallIcon(R.drawable.ic_lock)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }
}