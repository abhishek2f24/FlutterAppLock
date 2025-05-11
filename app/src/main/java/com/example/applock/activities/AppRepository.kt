package com.example.applock.repository

import androidx.lifecycle.LiveData
import com.example.applock.database.AppInfoDao
import com.example.applock.models.AppInfo

class AppRepository(private val appInfoDao: AppInfoDao) {
    
    val allLockedApps: LiveData<List<AppInfo>> = appInfoDao.getAllLockedApps()
    
    suspend fun insertApp(appInfo: AppInfo) {
        appInfoDao.insertApp(appInfo)
    }
    
    suspend fun deleteApp(appInfo: AppInfo) {
        appInfoDao.deleteApp(appInfo)
    }
    
    suspend fun deleteAppByPackage(packageName: String) {
        appInfoDao.deleteAppByPackage(packageName)
    }
    
    suspend fun deleteAllApps() {
        appInfoDao.deleteAllApps()
    }
    
    suspend fun isAppLocked(packageName: String): Boolean {
        return appInfoDao.isAppLocked(packageName) > 0
    }
    
    suspend fun getAppByPackage(packageName: String): AppInfo? {
        return appInfoDao.getAppByPackage(packageName)
    }
}