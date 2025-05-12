package com.example.applock.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable, // ← Add this line
    var isLocked: Boolean = false
)
