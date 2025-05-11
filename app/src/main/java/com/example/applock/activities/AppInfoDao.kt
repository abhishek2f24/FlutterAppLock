package com.example.applock.activities

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.applock.models.AppInfo

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM locked_apps")
    fun getAllLockedApps(): LiveData<List<AppInfo>>
    
    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): AppInfo?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(appInfo: AppInfo)
    
    @Delete
    suspend fun deleteApp(appInfo: AppInfo)
    
    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteAppByPackage(packageName: String)
    
    @Query("DELETE FROM locked_apps")
    suspend fun deleteAllApps()
    
    @Query("SELECT COUNT(*) FROM locked_apps WHERE packageName = :packageName")
    suspend fun isAppLocked(packageName: String): Int
}