package com.cortisense.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [StressRecord::class, ChatMessageEntity::class, SleepRecord::class, NotificationEntity::class, StressCheckInEntity::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stressRecordDao(): StressRecordDao
    abstract fun chatDao(): ChatMessageDao
    abstract fun sleepDao(): SleepDao
    abstract fun notificationDao(): NotificationDao
    abstract fun stressCheckInDao(): StressCheckInDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cortisense_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
