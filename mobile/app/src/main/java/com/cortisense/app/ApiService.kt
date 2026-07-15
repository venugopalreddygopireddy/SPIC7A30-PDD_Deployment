package com.cortisense.app

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// ===============================
// AUTH MODELS
// ===============================

data class RegisterRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("age") val age: Int,
    @SerializedName("gender") val gender: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class ForgotPasswordRequest(@SerializedName("email") val email: String)
data class VerifyOTPRequest(@SerializedName("email") val email: String, @SerializedName("otp") val otp: String)
data class ResetPasswordRequest(@SerializedName("email") val email: String, @SerializedName("otp") val otp: String, @SerializedName("new_password") val newPassword: String)
data class GenericMessageResponse(@SerializedName("message") val message: String, @SerializedName("otp") val otp: String? = null)

// ===============================
// REQUEST MODEL
// ===============================

data class CheckInRequest(

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("mobile_number")
    val mobileNumber: String = "",

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

data class ActionItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("is_done") val isDone: Boolean
)

data class StressResponse(

    @SerializedName("id")
    val id: Int?,

    @SerializedName("stress_level")
    val stressLevel: String,

    @SerializedName("score")
    val score: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("recommendation")
    val recommendation: String,

    @SerializedName("actions")
    val actions: List<ActionItem>? = emptyList(),

    @SerializedName("is_escalated")
    val isEscalated: Boolean = false
)


data class ProfileResponse(
    val first_name: String,
    val last_name: String,
    val mobile_number: String?,
    val dob: String?,
    val age: Int,
    val gender: String,
    val goal: String?,
    val profile_image: String? = null
)

data class ProfileUpdate(
    val first_name: String,
    val last_name: String,
    val mobile_number: String,
    val dob: String,
    val age: Int,
    val gender: String,
    val goal: String,
    val profile_image: String? = null
)

data class StressCheckInResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("stress_level") val stressLevel: String,
    @SerializedName("score") val score: Int,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("is_escalated") val isEscalated: Boolean,
    @SerializedName("sleep_duration") val sleepDuration: Double,
    @SerializedName("sleep_quality") val sleepQuality: Int,
    @SerializedName("physical_activity") val physicalActivity: Int,
    @SerializedName("screen_time") val screenTime: Double,
    @SerializedName("workload") val workload: String,
    @SerializedName("mood") val mood: String,
    @SerializedName("anxiety") val anxiety: String,
    @SerializedName("actions") val actions: List<ActionItem>? = emptyList()
)

// ===============================
// ANALYTICS MODELS
// ===============================

data class WeeklyAnalyticsResponse(
    @SerializedName("avg_score") val avgScore: Int,
    @SerializedName("highest_score") val highestScore: Int,
    @SerializedName("lowest_score") val lowestScore: Int,
    @SerializedName("total_checkins") val totalCheckins: Int,
    @SerializedName("distribution") val distribution: Map<String, Int>
)

data class MonthlyAnalyticsResponse(
    @SerializedName("avg_score") val avgScore: Int,
    @SerializedName("total_checkins") val totalCheckins: Int,
    @SerializedName("distribution") val distribution: Map<String, Int>,
    @SerializedName("calendar_activity") val calendarActivity: Map<String, Int>
)

data class DailyTrend(
    @SerializedName("date") val date: String,
    @SerializedName("score") val score: Int,
    @SerializedName("level") val level: String
)

data class TrendsResponse(
    @SerializedName("trends") val trends: List<DailyTrend>
)

data class FactorsResponse(
    @SerializedName("sleep_avg") val sleepAvg: Double,
    @SerializedName("screen_time_avg") val screenTimeAvg: Double,
    @SerializedName("caffeine_avg") val caffeineAvg: Double,
    @SerializedName("physical_activity_avg") val physicalActivityAvg: Double,
    @SerializedName("top_mood") val topMood: String,
    @SerializedName("top_workload") val topWorkload: String,
    @SerializedName("top_exercise") val topExercise: String
)

data class DashboardSummaryResponse(
    @SerializedName("total_checkins") val totalCheckins: Int,
    @SerializedName("latest_stress_score") val latestStressScore: Int,
    @SerializedName("latest_sleep_duration") val latestSleepDuration: Double,
    @SerializedName("latest_stress_category") val latestStressCategory: String,
    @SerializedName("current_streak") val currentStreak: Int,
    @SerializedName("longest_streak") val longestStreak: Int,
    @SerializedName("today_checkins_count") val todayCheckinsCount: Int,
    @SerializedName("today_lowest_score") val todayLowestScore: Int,
    @SerializedName("avg_stress_this_week") val avgStressThisWeek: Int,
    @SerializedName("best_day_this_week") val bestDayThisWeek: String
)


// ===============================
// API SERVICE
// ===============================

interface ApiService {

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): TokenResponse

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): GenericMessageResponse

    @POST("/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOTPRequest): GenericMessageResponse

    @POST("/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): GenericMessageResponse

    @POST("/checkin")
    suspend fun sendCheckIn(
        @Body request: CheckInRequest
    ): StressResponse

    @retrofit2.http.PATCH("/checkin/{checkinId}/action/{actionId}/complete")
    suspend fun completeAction(
        @retrofit2.http.Path("checkinId") checkinId: Int,
        @retrofit2.http.Path("actionId") actionId: String
    ): GenericMessageResponse

    @GET("/history")
    suspend fun getHistory(): List<StressCheckInResponse>

    @GET("/users/me/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("/users/me/profile")
    suspend fun updateProfile(@Body profile: ProfileUpdate): ProfileResponse


    @GET("/history/{id}")
    suspend fun getHistoryById(@retrofit2.http.Path("id") id: Int): StressCheckInResponse

    @GET("/analytics/weekly")
    suspend fun getWeeklyAnalytics(): WeeklyAnalyticsResponse

    @GET("/analytics/monthly")
    suspend fun getMonthlyAnalytics(): MonthlyAnalyticsResponse

    @GET("/analytics/trends")
    suspend fun getTrendsAnalytics(): TrendsResponse

    @GET("/analytics/factors")
    suspend fun getFactorsAnalytics(): FactorsResponse

    @GET("/dashboard/summary")
    suspend fun getDashboardSummary(): DashboardSummaryResponse
}


// ===============================
// RETROFIT CLIENT
// ===============================

object RetrofitClient {

    private const val BASE_URL = "https://cortisense-backend.onrender.com/"

    var preferenceManager: PreferenceManager? = null

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        
        // Skip auth for register and login and forgot-password flows
        val path = original.url.encodedPath
        if (path.contains("/login") || path.contains("/register") || path.contains("/forgot-password") || path.contains("/verify-otp") || path.contains("/reset-password")) {
            return@Interceptor chain.proceed(original)
        }
        
        var token: String? = null
        preferenceManager?.let { pm ->
            token = runBlocking { pm.jwtToken.first() }
        }

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
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