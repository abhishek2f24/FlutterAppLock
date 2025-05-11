package com.example.applock

import android.app.Application
import com.example.applock.database.AppLockDatabase
import com.example.applock.database.AppLockRepository
import com.example.applock.utils.AdsManager
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppLockDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { AppLockRepository(database.appLockDao()) }

    lateinit var adsManager: AdsManager

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Initialize AdsManager
        adsManager = AdsManager(this)
    }
}
