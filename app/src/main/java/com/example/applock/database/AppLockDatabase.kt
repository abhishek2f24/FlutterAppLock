package com.example.applock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.applock.models.LockInfo
import kotlinx.coroutines.CoroutineScope

@Database(entities = [LockInfo::class], version = 1, exportSchema = false)
abstract class AppLockDatabase : RoomDatabase() {

    abstract fun appLockDao(): AppLockDao

    companion object {
        @Volatile
        private var INSTANCE: AppLockDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppLockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLockDatabase::class.java,
                    "app_lock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
