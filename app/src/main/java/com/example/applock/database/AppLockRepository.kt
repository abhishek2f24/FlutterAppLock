package com.example.applock.database

import androidx.lifecycle.LiveData
import com.example.applock.models.LockInfo
import kotlinx.coroutines.flow.Flow

class AppLockRepository(private val appLockDao: AppLockDao) {

    val allLockedApps: Flow<List<LockInfo>> = appLockDao.getAllLockedApps()

    suspend fun insert(lockInfo: LockInfo) {
        appLockDao.insert(lockInfo)
    }

    suspend fun delete(lockInfo: LockInfo) {
        appLockDao.delete(lockInfo)
    }

    suspend fun isAppLocked(packageName: String): Boolean {
        return appLockDao.isAppLocked(packageName)
    }

    suspend fun getLockInfoByPackage(packageName: String): LockInfo? {
        return appLockDao.getLockInfoByPackage(packageName)
    }
}
