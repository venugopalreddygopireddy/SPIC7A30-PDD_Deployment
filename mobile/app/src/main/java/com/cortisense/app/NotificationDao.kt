package com.cortisense.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userEmail = :email AND timestamp >= :cutoff ORDER BY timestamp DESC")
    fun getNotificationsForUser(email: String, cutoff: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity): Int

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Int): Int

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email")
    suspend fun markAllAsRead(email: String): Int
}
