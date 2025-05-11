package com.example.applock.services

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.applock.R
import com.example.applock.activities.LockActivity
import com.example.applock.activities.MainActivity
import com.example.applock.database.AppLockRepository
import com.example.applock.utils.PreferenceUtils
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class AppLockService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: AppLockRepository
    private lateinit var preferenceUtils: PreferenceUtils

    private var lastLockedTime: Map<String, Long> = mutableMapOf()
    private var lastUnlockedPackage: String = ""
    private var lastUnlockedTime: Long = 0

    override fun onCreate() {
        super.onCreate()

        repository = (application as com.example.applock.App).repository
        preferenceUtils = PreferenceUtils(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Start monitoring
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        preferenceUtils.setServiceRunning(true)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        serviceJob.cancel()
        preferenceUtils.setServiceRunning(false)
        super.onDestroy()
    }

    private fun startMonitoring() {
        serviceScope.launch {
            while (isActive) {
                checkCurrentApp()
                delay(500) // Check every 500ms
            }
        }
    }

    private suspend fun checkCurrentApp() {
        val currentApp = getCurrentApp()

        if (currentApp.isNotEmpty() && currentApp != lastUnlockedPackage) {
            if (repository.isAppLocked(currentApp)) {
                val timeoutSeconds = preferenceUtils.getUnlockTimeout()
                val unlockTimeout = TimeUnit.SECONDS.toMillis(timeoutSeconds.toLong())

                // Check if this app was recently unlocked
                if (currentApp != lastUnlockedPackage ||
                    System.currentTimeMillis() - lastUnlockedTime > unlockTimeout) {
                    // Show lock screen
                    val lockIntent = Intent(this, LockActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("package_name", currentApp)
                    }
                    startActivity(lockIntent)
                }
            }
        }
    }

    private fun getCurrentApp(): String {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 5000, time)
        val event = UsageEvents.Event()
        var packageName = ""

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                packageName = event.packageName
            }
        }

        return packageName
    }

    fun setLastUnlockedApp(packageName: String) {
        lastUnlockedPackage = packageName
        lastUnlockedTime = System.currentTimeMillis()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Lock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running in background to lock your apps"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Lock")
            .setContentText("App Lock service is running")
            .setSmallIcon(R.drawable.ic_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "app_lock_service_channel"
    }
}
