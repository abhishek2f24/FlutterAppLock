package com.example.applock.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockInfo(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val timeStamp: Long = System.currentTimeMillis()
)
