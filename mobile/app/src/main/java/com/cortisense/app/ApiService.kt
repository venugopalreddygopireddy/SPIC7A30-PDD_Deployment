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

    @GET("/history")
    suspend fun getHistory(): List<StressResponse> // We map StressResponse partially for history. Wait, history schema returns StressCheckInResponse.
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