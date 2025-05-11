package com.example.applock.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.applock.models.AppInfo

class AppUtils {
    companion object {
        fun getInstalledApps(context: Context): List<AppInfo> {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val appInfoList = mutableListOf<AppInfo>()

            for (appInfo in installedApps) {
                // Filter out system apps
                if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    val appLabel = packageManager.getApplicationLabel(appInfo).toString()
                    val packageName = appInfo.packageName
                    val icon: Drawable = packageManager.getApplicationIcon(appInfo)

                    // Skip the current app
                    if (packageName != context.packageName) {
                        appInfoList.add(AppInfo(packageName, appLabel, icon))
                    }
                }
            }

            return appInfoList.sortedBy { it.appName }
        }

        fun getLaunchIntent(context: Context, packageName: String): Intent? {
            return context.packageManager.getLaunchIntentForPackage(packageName)
        }
    }
}
