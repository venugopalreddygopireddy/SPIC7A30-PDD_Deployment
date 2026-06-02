package com.cortisense.app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val title: String,
    val message: String,
    val type: String, // e.g., "stress", "checkin", "achievement", "engagement"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
