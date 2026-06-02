package com.cortisense.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StressRecordDao {
    @Query("SELECT * FROM stress_records WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getRecordsForUser(email: String): Flow<List<StressRecord>>

    @Query("SELECT * FROM stress_records WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 7")
    fun getLatest7Records(email: String): Flow<List<StressRecord>>

    @Insert
    suspend fun insertRecord(record: StressRecord): Long

    @Query("DELETE FROM stress_records WHERE userEmail = :email")
    suspend fun clearRecordsForUser(email: String): Int
}
