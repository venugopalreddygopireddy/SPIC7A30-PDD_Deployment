package com.cortisense.app

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val date: String, // yyyy-MM-dd
    val durationMinutes: Int,
    val startTime: Long,
    val endTime: Long
)

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records WHERE userEmail = :email ORDER BY startTime DESC")
    fun getAllRecords(email: String): Flow<List<SleepRecord>>

    @Query("SELECT * FROM sleep_records WHERE userEmail = :email AND date = :date")
    suspend fun getRecordsForDate(email: String, date: String): List<SleepRecord>

    @Insert
    suspend fun insertRecord(record: SleepRecord): Long

    @Update
    suspend fun updateRecord(record: SleepRecord): Int

    @Delete
    suspend fun deleteRecord(record: SleepRecord): Int
}
