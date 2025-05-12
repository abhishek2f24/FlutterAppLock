package com.example.applock.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import com.example.applock.models.AppInfo
import androidx.core.net.toUri

object Utils {
    
    /**
     * Check if the app has usage stats permission
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Check if overlay permission is granted
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Request usage stats permission
     */
    fun requestUsageStatsPermission(activity: Activity) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        activity.startActivity(intent)
    }

    /**
     * Request overlay permission
     */
    fun requestOverlayPermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${activity.packageName}".toUri()
        )
        activity.startActivity(intent)
    }

    /**
     * Get list of installed apps
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = ArrayList<AppInfo>()
        
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (packageInfo in packages) {
            // Skip system apps
            if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                continue
            }
            
            // Skip self
            if (packageInfo.packageName == context.packageName) {
                continue
            }
            
            val appName = packageManager.getApplicationLabel(packageInfo).toString()
            val appIcon = packageManager.getApplicationIcon(packageInfo)
            
            apps.add(
                AppInfo(
                    packageName = packageInfo.packageName,
                    appName = appName,
                    appIcon = appIcon
                )
            )
        }
        
        // Sort by app name
        return apps.sortedBy { it.appName }
    }

    /**
     * Show toast message
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}