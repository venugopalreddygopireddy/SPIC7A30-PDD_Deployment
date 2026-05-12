package com.cortisense.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "stress_records")
data class StressRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val score: Int,          // 0-100 Stress Index
    val level: String,       // "Low Stress" / "Moderate Stress" / "High Stress" / "Critical Stress"
    val reasons: List<String>,
    val timestamp: Long = System.currentTimeMillis(),

    // Phase 1 clinical fields
    val cognitiveScore: Int = 0,          // Raw sum Q1-Q3  (0-12)
    val emotionalScore: Int = 0,          // Raw sum Q4-Q6  (0-12)
    val physicalScore: Int = 0,           // Raw sum Q7-Q9  (0-12)
    val stressIndex: Int = 0,             // Final 0-100 index (same as score)
    val ventText: String = "",            // Vent-box free text
    val quizAnswers: List<Int> = emptyList() // 9 answers, each 0-4
)

class Converters {
    // List<String>
    @TypeConverter
    fun fromStringList(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    // List<Int>
    @TypeConverter
    fun fromIntList(value: List<Int>): String = Gson().toJson(value)

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
