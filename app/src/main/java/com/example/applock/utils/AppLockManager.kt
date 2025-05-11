package com.example.applock.utils

import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.example.applock.activities.LockActivity
import com.example.applock.models.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Manager class that handles app locking functionality
 */
class AppLockManager private constructor(private val context: Context) {
    
    // Room database for storing locked apps
    private val appLockDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppLockDatabase::class.java,
        "app_lock_database"
    ).build()
    
    private val appDao = appLockDatabase.appDao()
    
    companion object {
        @Volatile
        private var instance: AppLockManager? = null
        
        fun getInstance(context: Context): AppLockManager {
            return instance ?: synchronized(this) {
                instance ?: AppLockManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Check if app is locked
     */
    suspend fun isAppLocked(packageName: String): Boolean {
        return withContext(Dispatchers.IO) {
            appDao.isAppLocked(packageName)
        }
    }
    
    /**
     * Get all locked apps
     */
    suspend fun getLockedApps(): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            appDao.getLockedApps()
        }
    }
    
    /**
     * Lock or unlock an app
     */
    suspend fun setAppLock(appInfo: AppInfo, locked: Boolean) {
        withContext(Dispatchers.IO) {
            val updatedApp = appInfo.copy(isLocked = locked)
            if (locked) {
                appDao.insertApp(updatedApp)
            } else {
                appDao.deleteApp(updatedApp.packageName)
            }
        }
    }
    
    /**
     * Get current foreground app package name
     */
    fun getCurrentForegroundApp(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        
        // Get usage stats for the last 5 seconds
        val usageEvents = usageStatsManager.queryEvents(time - 5000, time)
        
        var lastEvent: UsageEvents.Event? = null
        val event = UsageEvents.Event()
        
        // Find the last event
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            
            // Filter for move to foreground events
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastEvent = UsageEvents.Event()
                lastEvent.copyFrom(event)
            }
        }
        
        return lastEvent?.packageName
    }
    
    /**
     * Show the lock screen for an app
     */
    fun showLockScreen(packageName: String) {
        val intent = Intent(context, LockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("package_name", packageName)
        }
        context.startActivity(intent)
    }
    
    /**
     * Close the Room database when the manager is no longer needed
     */
    fun closeDatabase() {
        appLockDatabase.close()
    }
}

/**
 * Room database implementation
 */
@androidx.room.Database(entities = [AppInfo::class], version = 1)
abstract class AppLockDatabase : androidx.room.RoomDatabase() {
    abstract fun appDao(): AppDao
}

/**
 * Data Access Object for AppInfo entities
 */
@androidx.room.Dao
interface AppDao {
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppInfo)
    
    @androidx.room.Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)
    
    @androidx.room.Query("SELECT * FROM locked_apps")
    suspend fun getLockedApps(): List<AppInfo>
    
    @androidx.room.Query("SELECT COUNT(*) > 0 FROM locked_apps WHERE packageName = :packageName")
    suspend fun isAppLocked(packageName: String): Boolean
}