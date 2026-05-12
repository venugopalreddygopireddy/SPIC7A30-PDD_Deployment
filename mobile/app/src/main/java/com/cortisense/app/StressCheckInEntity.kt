package com.cortisense.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "clinical_checkins")
data class StressCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val stressLevel: String,
    val score: Int,
    val recommendation: String,
    val isEscalated: Boolean,

    // 25 Parameters
    val age: Int,
    val gender: String,
    val occupation: String,
    val maritalStatus: String,
    val sleepDuration: Float,
    val sleepQuality: Int,
    val wakeUpTime: String,
    val bedTime: String,
    val physicalActivity: Int,
    val screenTime: Float,
    val caffeineIntake: Int,
    val alcoholIntake: Int,
    val smokingHabit: String,
    val workHours: Float,
    val travelTime: Float,
    val socialInteractions: Int,
    val meditationPractice: Int,
    val exerciseType: String,
    val bloodPressure: String,
    val bloodSugarLevel: Int,
    val mood: String,
    val anxiety: String,
    val caffeineDependency: String,
    val workload: String,
    val bodyFeeling: String
)
