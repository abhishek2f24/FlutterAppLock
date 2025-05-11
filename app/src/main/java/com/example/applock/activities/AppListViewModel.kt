package com.example.applock.viewmodels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.applock.database.AppDatabase
import com.example.applock.models.AppInfo
import com.example.applock.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AppRepository
    val allLockedApps: LiveData<List<AppInfo>>
    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps
    
    init {
        val appInfoDao = AppDatabase.getDatabase(application).appInfoDao()
        repository = AppRepository(appInfoDao)
        allLockedApps = repository.allLockedApps
        loadInstalledApps()
    }
    
    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = getInstalledApps()
        }
    }
    
    private suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = getApplication<Application>().packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        // Filter system apps if needed
        return@withContext packages
            .filter { appInfo ->
                // Exclude system apps that aren't updated and our own app
                val isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                val isUpdatedSystemApp = appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                val isOurApp = appInfo.packageName == getApplication<Application>().packageName
                
                (!isSystemApp || isUpdatedSystemApp) && !isOurApp
            }
            .sortedBy { pm.getApplicationLabel(it).toString() }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isLocked = false
                )
            }
    }
    
    fun toggleAppLock(appInfo: AppInfo) {
        viewModelScope.launch {
            if (repository.isAppLocked(appInfo.packageName)) {
                repository.deleteAppByPackage(appInfo.packageName)
            } else {
                repository.insertApp(appInfo)
            }
        }
    }
    
    fun getLockedStatus(packageName: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isLocked = repository.isAppLocked(packageName)
            callback(isLocked)
        }
    }
}