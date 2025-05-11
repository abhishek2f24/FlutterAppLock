package com.example.applock.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Utility class to manage encrypted SharedPreferences for storing sensitive information
 */
class PrefManager(context: Context) {
    
    companion object {
        private const val PREFS_FILE_NAME = "app_lock_preferences"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_FIRST_TIME = "first_time"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // Pin code management
    fun setPinCode(pin: String) {
        prefs.edit().putString(KEY_PIN_CODE, pin).apply()
    }
    
    fun getPinCode(): String {
        return prefs.getString(KEY_PIN_CODE, "") ?: ""
    }
    
    fun isPinCodeSet(): Boolean {
        return !getPinCode().isNullOrEmpty()
    }
    
    // Biometric authentication
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
    
    // Service status
    fun setServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }
    
    fun isServiceEnabled(): Boolean {
        return prefs.getBoolean(KEY_SERVICE_ENABLED, true)
    }
    
    // First time launch
    fun isFirstTime(): Boolean {
        return prefs.getBoolean(KEY_FIRST_TIME, true)
    }
    
    fun setFirstTimeDone() {
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply()
    }
}