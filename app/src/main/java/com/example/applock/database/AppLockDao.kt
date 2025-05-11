package com.example.applock.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.applock.models.LockInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lockInfo: LockInfo)

    @Delete
    suspend fun delete(lockInfo: LockInfo)

    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedApps(): Flow<List<LockInfo>>

    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName")
    suspend fun getLockInfoByPackage(packageName: String): LockInfo?

    @Query("SELECT COUNT(*) > 0 FROM locked_apps WHERE packageName = :packageName")
    suspend fun isAppLocked(packageName: String): Boolean
}
