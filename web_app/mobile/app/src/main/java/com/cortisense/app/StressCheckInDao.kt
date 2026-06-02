package com.cortisense.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StressCheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: StressCheckInEntity): Long

    @Query("SELECT * FROM clinical_checkins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<StressCheckInEntity>>

    @Query("SELECT * FROM clinical_checkins WHERE id = :id")
    suspend fun getCheckInById(id: Long): StressCheckInEntity?

    @Query("DELETE FROM clinical_checkins")
    suspend fun clearHistory(): Int
}
