package com.example.applock.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtils(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "AppLockPrefs"
        private const val SERVICE_RUNNING = "service_running"
        private const val FIRST_TIME = "first_time"
        private const val UNLOCK_TIMEOUT = "unlock_timeout"
    }

    fun isServiceRunning(): Boolean {
        return prefs.getBoolean(SERVICE_RUNNING, false)
    }

    fun setServiceRunning(running: Boolean) {
        prefs.edit().putBoolean(SERVICE_RUNNING, running).apply()
    }

    fun isFirstTime(): Boolean {
        return prefs.getBoolean(FIRST_TIME, true)
    }

    fun setFirstTime(firstTime: Boolean) {
        prefs.edit().putBoolean(FIRST_TIME, firstTime).apply()
    }

    fun getUnlockTimeout(): Int {
        // Default timeout: 30 seconds
        return prefs.getInt(UNLOCK_TIMEOUT, 30)
    }

    fun setUnlockTimeout(seconds: Int) {
        prefs.edit().putInt(UNLOCK_TIMEOUT, seconds).apply()
    }
}