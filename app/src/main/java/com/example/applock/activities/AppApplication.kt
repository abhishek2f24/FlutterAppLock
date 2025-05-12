package com.example.applock.activities

import android.app.Application

class AppApplication : Application() {
    
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.appInfoDao()) }
    private lateinit var adManager: AdManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize AdMob
        adManager = AdManager.getInstance(this)
        adManager.initialize()
        
        // Preload a rewarded ad for future use
        adManager.loadRewardedAd()
    }
}