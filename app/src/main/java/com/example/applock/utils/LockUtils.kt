package com.example.applock.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.security.MessageDigest
import java.util.*

class LockUtils {
    companion object {
        private const val PREF_NAME = "AppLockPrefs"
        private const val PATTERN_KEY = "pattern_hash"

        // Check if usage stats permission is granted
        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }

        // Open usage stats settings
        fun openUsageAccessSettings(context: Context) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
        }

        // Save pattern hash
        fun savePattern(context: Context, pattern: String) {
            val hash = hashPattern(pattern)
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PATTERN_KEY, hash).apply()
        }

        // Validate pattern
        fun validatePattern(context: Context, input: String): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedHash = prefs.getString(PATTERN_KEY, "") ?: ""
            val inputHash = hashPattern(input)
            return savedHash.isNotEmpty() && savedHash == inputHash
        }

        // Check if pattern is set
        fun isPatternSet(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedHash = prefs.getString(PATTERN_KEY, "") ?: ""
            return savedHash.isNotEmpty()
        }

        // Hash pattern
        private fun hashPattern(pattern: String): String {
            val bytes = pattern.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}
