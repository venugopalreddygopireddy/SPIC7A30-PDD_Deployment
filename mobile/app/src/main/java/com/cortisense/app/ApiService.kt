package com.cortisense.app

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// ===============================
// REQUEST MODEL
// ===============================

data class CheckInRequest(

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("occupation")
    val occupation: String,

    @SerializedName("marital_status")
    val maritalStatus: String,

    @SerializedName("sleep_duration")
    val sleepDuration: Float,

    @SerializedName("sleep_quality")
    val sleepQuality: Int,

    @SerializedName("wake_up_time")
    val wakeUpTime: String,

    @SerializedName("bed_time")
    val bedTime: String,

    @SerializedName("physical_activity")
    val physicalActivity: Int,

    @SerializedName("screen_time")
    val screenTime: Float,

    @SerializedName("caffeine_intake")
    val caffeineIntake: Int,

    @SerializedName("alcohol_intake")
    val alcoholIntake: String,

    @SerializedName("smoking_habit")
    val smokingHabit: String,

    @SerializedName("work_hours")
    val workHours: Float,

    @SerializedName("travel_time")
    val travelTime: Int,

    @SerializedName("social_interactions")
    val socialInteractions: Int,

    @SerializedName("meditation_practice")
    val meditationPractice: String,

    @SerializedName("exercise_type")
    val exerciseType: String,

    @SerializedName("blood_pressure")
    val bloodPressure: Int,

    @SerializedName("blood_sugar_level")
    val bloodSugarLevel: Int,

    @SerializedName("mood")
    val mood: String,

    @SerializedName("anxiety")
    val anxiety: String,

    @SerializedName("caffeine_dependency")
    val caffeineDependency: String,

    @SerializedName("workload")
    val workload: String,

    @SerializedName("body_feeling")
    val bodyFeeling: String
)


// ===============================
// RESPONSE MODEL
// ===============================

data class StressResponse(

    @SerializedName("stress_level")
    val stressLevel: String,

    @SerializedName("score")
    val score: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("recommendation")
    val recommendation: String,

    @SerializedName("is_escalated")
    val isEscalated: Boolean = false
)


// ===============================
// API SERVICE
// ===============================

interface ApiService {

    @POST("/checkin")
    suspend fun sendCheckIn(
        @Body request: CheckInRequest
    ): StressResponse
}


// ===============================
// RETROFIT CLIENT
// ===============================

object RetrofitClient {

    private const val BASE_URL = "http://10.17.164.160:8000/"

    private val client = OkHttpClient.Builder()

        .connectTimeout(120, TimeUnit.SECONDS)

        .readTimeout(120, TimeUnit.SECONDS)

        .writeTimeout(120, TimeUnit.SECONDS)

        .retryOnConnectionFailure(true)

        .build()

    val instance: ApiService by lazy {

        val retrofit = Retrofit.Builder()

            .baseUrl(BASE_URL)

            .client(client)

            .addConverterFactory(GsonConverterFactory.create())

            .build()

        retrofit.create(ApiService::class.java)
    }
}