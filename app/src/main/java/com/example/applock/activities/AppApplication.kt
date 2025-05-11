package com.example.applock

import android.app.Application
import com.example.applock.activities.AdManager
import com.example.applock.activities.AppDatabase
import com.example.applock.repository.AppRepository

class AppApplication : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.appInfoDao()) }
    lateinit var adManager: AdManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdMob
        adManager = AdManager.getInstance(this)
        adManager.initialize()
        
        // Preload a rewarded ad for future use
        adManager.loadRewardedAd()
    }
}