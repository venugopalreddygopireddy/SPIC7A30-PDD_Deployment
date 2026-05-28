package com.cortisense.app

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.text.SimpleDateFormat

data class ChatMessage(
    val text: String,
    val isAI: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val stressDao = database.stressRecordDao()
    private val chatDao = database.chatDao()
    private val checkInDao = database.stressCheckInDao()
    private val checkInRepository = StressCheckInRepository(checkInDao)

    val clinicalHistory = checkInRepository.allCheckIns

    val chatMessages = mutableStateListOf<ChatMessage>()
    var isAiTyping = mutableStateOf(false)
        private set

    // --- Persistent States from PreferenceManager ---
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    private val _themeMode = MutableStateFlow("light")
    val themeMode = _themeMode.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()

    private val _isUserRegistered = MutableStateFlow(false)
    val isUserRegistered = _isUserRegistered.asStateFlow()

    private val _isProfileCreated = MutableStateFlow(false)
    val isProfileCreated = _isProfileCreated.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _jwtToken = MutableStateFlow("")
    val jwtToken = _jwtToken.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _userPassword = MutableStateFlow("")
    val userPassword = _userPassword.asStateFlow()

    private val _userAge = MutableStateFlow("25")
    val userAge = _userAge.asStateFlow()

    private val _userGender = MutableStateFlow("")
    val userGender = _userGender.asStateFlow()

    private val _userGoal = MutableStateFlow("")
    val userGoal = _userGoal.asStateFlow()

    private val _profileImageUri = MutableStateFlow("")
    val profileImageUri = _profileImageUri.asStateFlow()

    private val _currentStreak = MutableStateFlow("0")
    val currentStreak = _currentStreak.asStateFlow()

    private val _longestStreak = MutableStateFlow("0")
    val longestStreak = _longestStreak.asStateFlow()

    private val _totalCheckins = MutableStateFlow("0")
    val totalCheckins = _totalCheckins.asStateFlow()

    private val _lastCheckinDate = MutableStateFlow("")
    val lastCheckinDate = _lastCheckinDate.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium = _isPremium.asStateFlow()

    private val _userCoins = MutableStateFlow("0")
    val userCoins = _userCoins.asStateFlow()
    
    private val _completedTasks = MutableStateFlow<Set<String>>(emptySet())
    val completedTasks = _completedTasks.asStateFlow()

    // --- Runtime UI States ---
    var currentUserEmail by mutableStateOf("")
    var currentUserName by mutableStateOf("")
    var currentSessionMinutes by mutableStateOf(0)
    var heartRate by mutableStateOf("72 bpm")
    var sleepHours by mutableStateOf("7.5 hrs")

    var cognitiveScore by mutableStateOf(0)
    var emotionalScore by mutableStateOf(0)
    var physicalScore by mutableStateOf(0)
    var triggerSafetyBreathing by mutableStateOf(false)
    val responseTimes = mutableStateListOf<Long>()

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount = _unreadNotificationsCount.asStateFlow()

    private val _avgStressThisWeek = MutableStateFlow(0)
    val avgStressThisWeek = _avgStressThisWeek.asStateFlow()

    private val _bestDayThisWeek = MutableStateFlow("--")
    val bestDayThisWeek = _bestDayThisWeek.asStateFlow()

    private val _trends = MutableStateFlow<List<StressRecord>>(emptyList())
    val trends = _trends.asStateFlow()

    private val _history = MutableStateFlow<List<StressRecord>>(emptyList())
    val history = _history.asStateFlow()

    private val _todayCheckinsCount = MutableStateFlow(0)
    val todayCheckinsCount = _todayCheckinsCount.asStateFlow()

    private val _todaySleepDuration = MutableStateFlow("0h 0m")
    val todaySleepDuration = _todaySleepDuration.asStateFlow()

    private val _todayLowestScore = MutableStateFlow(0)
    val todayLowestScore = _todayLowestScore.asStateFlow()

    private val _weeklyHistory = MutableStateFlow<List<StressRecord>>(emptyList())
    val weeklyHistory = _weeklyHistory.asStateFlow()

    private val _monthlyHistory = MutableStateFlow<List<StressRecord>>(emptyList())
    val monthlyHistory = _monthlyHistory.asStateFlow()

    private val _weeklyAnalytics = MutableStateFlow<WeeklyAnalyticsResponse?>(null)
    val weeklyAnalytics = _weeklyAnalytics.asStateFlow()

    private val _monthlyAnalytics = MutableStateFlow<MonthlyAnalyticsResponse?>(null)
    val monthlyAnalytics = _monthlyAnalytics.asStateFlow()

    private val _trendsAnalytics = MutableStateFlow<TrendsResponse?>(null)
    val trendsAnalytics = _trendsAnalytics.asStateFlow()

    private val _factorsAnalytics = MutableStateFlow<FactorsResponse?>(null)
    val factorsAnalytics = _factorsAnalytics.asStateFlow()

    private val _selectedRecord = MutableStateFlow<StressRecord?>(null)
    val selectedRecord = _selectedRecord.asStateFlow()

    private val _isSleepTracking = MutableStateFlow(false)
    val isSleepTracking = _isSleepTracking.asStateFlow()

    private val _sleepHistory = MutableStateFlow<List<SleepRecord>>(emptyList())
    val sleepHistory = _sleepHistory.asStateFlow()

    private val _isSafeMode = MutableStateFlow(false)
    val isSafeMode = _isSafeMode.asStateFlow()

    var stressScore by mutableStateOf(0)
    var stressLevel by mutableStateOf("Low Stress")
    var reasons by mutableStateOf<List<String>>(emptyList())
    var quizAnswers by mutableStateOf(List(9) { -1 })
    var ventText by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // --- New 25 Check-In Parameters ---
    var checkInAge by mutableStateOf("25")
    var checkInGender by mutableStateOf("Male")
    var checkInOccupation by mutableStateOf("Student")
    var checkInMaritalStatus by mutableStateOf("Single")
    var checkInSleepDuration by mutableStateOf(7.0f)
    var checkInSleepQuality by mutableStateOf(3)
    var checkInWakeUpTime by mutableStateOf("07:00")
    var checkInBedTime by mutableStateOf("23:00")
    var checkInPhysicalActivity by mutableStateOf(3)
    var checkInScreenTime by mutableStateOf(4.0f)
    var checkInCaffeineIntake by mutableStateOf(1)
    var checkInAlcoholIntake by mutableStateOf(0)
    var checkInSmokingHabit by mutableStateOf("Non-smoker")
    var checkInWorkHours by mutableStateOf(8.0f)
    var checkInTravelTime by mutableStateOf(1.0f)
    var checkInSocialInteractions by mutableStateOf(3)
    var checkInWorkload by mutableStateOf("Moderate")
    var checkInMeditationPractice by mutableStateOf(0)
    var checkInExerciseType by mutableStateOf("None")
    var checkInBloodPressure by mutableStateOf("120/80")
    var checkInBloodSugarLevel by mutableStateOf(90)
    var checkInMood by mutableStateOf("Neutral")
    var checkInAnxiety by mutableStateOf("Low")
    var checkInBodyFeeling by mutableStateOf("Normal")
    var checkInCaffeineDependency by mutableStateOf("No")

    var aiRecommendation by mutableStateOf("")
    var isEscalated by mutableStateOf(false)

    // Notification states
    private val _notifCheckin = MutableStateFlow(true)
    val notifCheckin = _notifCheckin.asStateFlow()
    private val _notifStressAlerts = MutableStateFlow(true)
    val notifStressAlerts = _notifStressAlerts.asStateFlow()
    private val _notifRecommendations = MutableStateFlow(true)
    val notifRecommendations = _notifRecommendations.asStateFlow()

    private val _notifAchievements = MutableStateFlow(false)
    val notifAchievements = _notifAchievements.asStateFlow()

    init {
        RetrofitClient.preferenceManager = preferenceManager
        viewModelScope.launch {
            launch { preferenceManager.isDarkTheme.collect { _isDarkTheme.value = it } }
            launch { preferenceManager.themeMode.collect { _themeMode.value = it } }
            launch { preferenceManager.language.collect { _language.value = it } }
            launch { preferenceManager.isUserRegistered.collect { _isUserRegistered.value = it } }
            launch { preferenceManager.isProfileCreated.collect { _isProfileCreated.value = it } }
            launch { preferenceManager.isLoggedIn.collect { _isLoggedIn.value = it } }
            launch { preferenceManager.jwtToken.collect { _jwtToken.value = it } }
            launch { preferenceManager.userEmail.collect { 
                _userEmail.value = it
                currentUserEmail = it
                if (it.isNotEmpty()) {
                    loadUserData(it)
                    loadNotifications(it)
                    val token = preferenceManager.jwtToken.firstOrNull()
                    if (!token.isNullOrEmpty()) {
                        fetchAnalytics()
                        fetchHistory()
                        fetchDashboardSummary()
                    }
                }
            } }
            launch { preferenceManager.userName.collect { 
                _userName.value = it
                currentUserName = it
            } }
            launch { preferenceManager.userPassword.collect { _userPassword.value = it } }
            launch { preferenceManager.userAge.collect { _userAge.value = it } }
            launch { preferenceManager.userGender.collect { _userGender.value = it } }
            launch { preferenceManager.userGoal.collect { _userGoal.value = it } }
            launch { preferenceManager.profileImageUri.collect { _profileImageUri.value = it } }
            launch { preferenceManager.currentStreak.collect { _currentStreak.value = it } }
            launch { preferenceManager.longestStreak.collect { _longestStreak.value = it } }
            launch { preferenceManager.totalCheckins.collect { _totalCheckins.value = it } }
            launch { preferenceManager.lastCheckinDate.collect { _lastCheckinDate.value = it } }
            launch { preferenceManager.isPremium.collect { _isPremium.value = it } }
            launch { preferenceManager.userCoins.collect { _userCoins.value = it } }
            launch { preferenceManager.isSleepTracking.collect { _isSleepTracking.value = it } }
            launch { preferenceManager.notifCheckin.collect { _notifCheckin.value = it } }
            launch { preferenceManager.notifStressAlerts.collect { _notifStressAlerts.value = it } }
            launch { preferenceManager.notifRecommendations.collect { _notifRecommendations.value = it } }
            launch { preferenceManager.notifAchievements.collect { _notifAchievements.value = it } }
            
            // Load Sleep History
            launch {
                database.sleepDao().getAllRecords(currentUserEmail).collect {
                    _sleepHistory.value = it
                }
            }
            
            launch { 
                kotlinx.coroutines.flow.combine(preferenceManager.completedTasks, preferenceManager.lastTaskDate) { tasksStr, dateStr ->
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    if (dateStr != today) {
                        preferenceManager.updateCompletedTasks("", today)
                        emptySet<String>()
                    } else {
                        if (tasksStr.isEmpty()) emptySet() else tasksStr.split(",").toSet()
                    }
                }.collect { _completedTasks.value = it }
            }
            
            // Auto-clear dummy state if we have no JWT token
            val currentToken = preferenceManager.jwtToken.firstOrNull() ?: ""
            val currentlyLoggedIn = preferenceManager.isLoggedIn.firstOrNull() ?: false
            if (currentlyLoggedIn && currentToken.isEmpty()) {
                logout()
            }
        }
    }

    private fun loadNotifications(email: String) {
        viewModelScope.launch {
            database.notificationDao().getNotificationsForUser(email).collect {
                _notifications.value = it
                _unreadNotificationsCount.value = it.count { notif -> !notif.isRead }
            }
        }
    }

    private fun loadUserData(email: String) {
        // Disabled to prevent overriding PostgreSQL remote backend data
        /*
        viewModelScope.launch {
            stressDao.getRecordsForUser(email).collect { 
                // _history.value = it 
                // updateDashboardMetrics(it)
            }
        }
        
        viewModelScope.launch {
            clinicalHistory.collect { checkIns ->
                if (checkIns.isNotEmpty()) {
                    val latest = checkIns.first()
                    // Dynamically set UI stats based on the latest real data
                    // sleepHours = "${latest.sleepDuration} hrs"
                    
                    // Since heart rate isn't directly input, we dynamically simulate it based on stress score (higher stress = higher HR)
                    val calculatedHr = 65 + (latest.score / 2)
                    heartRate = "$calculatedHr bpm"
                    
                    // Dynamic wellness scores based on AI stress evaluation
                    cognitiveScore = (100 - (latest.score * 0.9)).toInt().coerceIn(0, 100)
                    emotionalScore = (100 - (latest.score * 1.2)).toInt().coerceIn(0, 100)
                    physicalScore = (100 - (latest.score * 0.8)).toInt().coerceIn(0, 100)
                    
                    val hours = latest.sleepDuration.toInt()
                    val minutes = ((latest.sleepDuration - hours) * 60).toInt()
                    // _todaySleepDuration.value = "${hours}h ${minutes}m"
                }
            }
        }
        */
    }

    private fun updateDashboardMetrics(records: List<StressRecord>) {
        if (records.isNotEmpty()) {
            val latest = records.first()
            stressScore = latest.score
            stressLevel = latest.level
            reasons = latest.reasons
        }
        
        // Count today's checkins
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        
        _todayCheckinsCount.value = records.count {
            val recCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            recCal.get(Calendar.DAY_OF_YEAR) == today && recCal.get(Calendar.YEAR) == year
        }

        // Calculate today's lowest score
        val todayRecords = records.filter {
            val recCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            recCal.get(Calendar.DAY_OF_YEAR) == today && recCal.get(Calendar.YEAR) == year
        }
        _todayLowestScore.value = todayRecords.minByOrNull { it.score }?.score ?: 0

        // Calculate avg stress this week
        val last7Days = records.take(7)
        if (last7Days.isNotEmpty()) {
            _avgStressThisWeek.value = last7Days.map { it.score }.average().toInt()
            _trends.value = last7Days.reversed()
            
            val bestRecord = last7Days.minByOrNull { it.score }
            bestRecord?.let {
                val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
                _bestDayThisWeek.value = sdf.format(Date(it.timestamp))
            }
        }
        
        val cal = Calendar.getInstance()
        var currentDay = cal.get(Calendar.DAY_OF_WEEK)
        if (currentDay == Calendar.SUNDAY) {
            currentDay = 8
        }
        cal.add(Calendar.DAY_OF_YEAR, -(currentDay - Calendar.MONDAY))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeek = cal.timeInMillis
        
        cal.add(Calendar.DAY_OF_YEAR, 7)
        val endOfWeek = cal.timeInMillis
        
        _weeklyHistory.value = records.filter { it.timestamp in startOfWeek until endOfWeek }
        
        val monthCal = Calendar.getInstance()
        monthCal.set(Calendar.DAY_OF_MONTH, 1)
        monthCal.set(Calendar.HOUR_OF_DAY, 0)
        monthCal.set(Calendar.MINUTE, 0)
        monthCal.set(Calendar.SECOND, 0)
        monthCal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = monthCal.timeInMillis
        
        monthCal.add(Calendar.MONTH, 1)
        val endOfMonth = monthCal.timeInMillis
        
        _monthlyHistory.value = records.filter { it.timestamp in startOfMonth until endOfMonth }
    }

    // --- AI Logic ---
    private val systemPrompt = """
        You are Corti, a warm, friendly and empathetic AI Stress Coach for the CortiSense app.
        Act like Snapchat MyAI or Meta AI - casual, supportive, and helpful.
        Always validate the user's feelings first. Give practical stress relief tips, breathing exercises, mindfulness suggestions, and lifestyle recommendations.
        Be concise and encouraging. Use light emojis (🌿 ✨ 🧘♂️).
        If the user shows signs of severe distress or suicidal thoughts, gently recommend professional help like calling 988 (US Suicide & Crisis Lifeline).
    """.trimIndent()

    private fun getApiKeyFromAssets(): String {
        return try {
            val key = getApplication<Application>().assets.open("gemini_key.txt")
                .bufferedReader().use { it.readText().trim() }
            android.util.Log.d("CortiAI", "API Key loaded successfully (starts with: ${key.take(4)})")
            key
        } catch (e: Exception) {
            android.util.Log.e("CortiAI", "Failed to read gemini_key.txt from assets", e)
            ""
        }
    }

    private val generativeModel by lazy {
        val apiKey = getApiKeyFromAssets()
        if (apiKey.isEmpty()) {
            errorMessage = "API Key not found. Please check gemini_key.txt in assets folder."
            null
        } else {
            try {
                GenerativeModel(
                    modelName = "gemini-1.5-flash-latest",
                    apiKey = apiKey,
                    systemInstruction = content { text(systemPrompt) },
                    generationConfig = generationConfig {
                        temperature = 0.7f
                        maxOutputTokens = 800
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to initialize AI: ${e.message}"
                android.util.Log.e("CortiAI", "Model Init Error", e)
                null
            }
        }
    }

    fun sendChatMessage(message: String) {
        if (message.isBlank()) return
        chatMessages.add(ChatMessage(text = message, isAI = false))
        
        val model = generativeModel
        if (model == null) {
            chatMessages.add(ChatMessage(text = errorMessage ?: "AI is not initialized properly. Please check your API key.", isAI = true))
            return
        }

        viewModelScope.launch {
            isAiTyping.value = true
            try {
                val response = withContext(Dispatchers.IO) {
                    model.generateContent(message)
                }
                val reply = response.text ?: "I'm here to help. Could you tell me a bit more about what's on your mind?"
                chatMessages.add(ChatMessage(text = reply, isAI = true))
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                android.util.Log.e("CortiAI", "Gemini Runtime Error: $errorMsg", e)
                
                val friendlyMessage = when {
                    errorMsg.contains("API_KEY_INVALID", ignoreCase = true) || errorMsg.contains("403") -> 
                        "The AI model could not be found. Please check your internet or API key settings (Key invalid)."
                    errorMsg.contains("NOT_FOUND", ignoreCase = true) || errorMsg.contains("404") ->
                        "The AI model could not be found. Please check your internet or API key settings (Model not found)."
                    errorMsg.contains("SAFETY", ignoreCase = true) ->
                        "I'm sorry, I can't discuss that. Let's talk about something else related to your wellness."
                    else -> 
                        "The AI model could not be found. Please check your internet or API key settings (Error: $errorMsg)"
                }
                chatMessages.add(ChatMessage(text = friendlyMessage, isAI = true))
            } finally {
                isAiTyping.value = false
            }
        }
    }

    fun clearChatHistory() {
        chatMessages.clear()
    }

    // --- Authentication & Profile Logic ---
    fun setLoggedIn(value: Boolean) {
        viewModelScope.launch { preferenceManager.setLoggedIn(value) }
    }

    fun saveRegistration(name: String, email: String, pass: String) {
        // Kept for local compatibility
        viewModelScope.launch {
            preferenceManager.saveRegistration(name, email, pass)
            currentUserEmail = email
            currentUserName = name
        }
    }

    fun registerWithApi(firstName: String, lastName: String, age: Int, gender: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val req = RegisterRequest(firstName, lastName, age, gender, email.trim(), pass)
                RetrofitClient.instance.register(req)
                // DO NOT automatically log in or save JWT. Force user to sign in.
                onSuccess()
            } catch(e: Exception) {
                errorMessage = "Registration Failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val req = LoginRequest(email.trim(), pass)
                val res = RetrofitClient.instance.login(req)
                preferenceManager.saveJwtToken(res.accessToken)
                
                preferenceManager.setLoggedIn(true)
                currentUserEmail = email
                _userEmail.value = email
                fetchAnalytics()
                fetchHistory()
                fetchDashboardSummary()
                
                onSuccess()
            } catch(e: Exception) {
                errorMessage = "Login Failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun forgotPasswordApi(email: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val res = RetrofitClient.instance.forgotPassword(ForgotPasswordRequest(email.trim()))
                onSuccess(res.otp ?: "")
            } catch(e: Exception) {
                errorMessage = "Failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun verifyOtpApi(email: String, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                RetrofitClient.instance.verifyOtp(VerifyOTPRequest(email.trim(), otp.trim()))
                onSuccess()
            } catch(e: Exception) {
                errorMessage = "Failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetPasswordApi(email: String, otp: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                RetrofitClient.instance.resetPassword(ResetPasswordRequest(email.trim(), otp.trim(), newPass))
                onSuccess()
            } catch(e: Exception) {
                errorMessage = "Failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveProfileSetup(name: String, age: String, gender: String, goal: String) {
        viewModelScope.launch {
            preferenceManager.saveProfileSetup(age, gender, goal)
            preferenceManager.updateProfile(name, age, gender, goal, _profileImageUri.value)
        }
    }

    fun updateProfile(name: String, age: String, gender: String, goal: String, imageUri: String) {
        viewModelScope.launch {
            preferenceManager.updateProfile(name, age, gender, goal, imageUri)
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferenceManager.saveJwtToken("")
            preferenceManager.setLoggedIn(false)
            preferenceManager.saveRegistration("", "", "")
            currentUserEmail = ""
            currentUserName = ""
            _userEmail.value = ""
            _userName.value = ""
            _jwtToken.value = ""
        }
    }

    fun resetInputs() {
        quizAnswers = List(9) { -1 }
        ventText = ""
        reasons = emptyList()
    }

    fun initializeCheckInForm() {
        viewModelScope.launch {
            val allList = checkInRepository.allCheckIns.firstOrNull() ?: emptyList()
            val latest = allList.firstOrNull()
            if (latest != null) {
                checkInAge = latest.age.toString()
                checkInGender = latest.gender
                checkInOccupation = latest.occupation
                checkInMaritalStatus = latest.maritalStatus
                checkInSleepDuration = latest.sleepDuration
                checkInSleepQuality = latest.sleepQuality
                checkInWakeUpTime = latest.wakeUpTime
                checkInBedTime = latest.bedTime
                checkInPhysicalActivity = latest.physicalActivity
                checkInScreenTime = latest.screenTime
                checkInCaffeineIntake = latest.caffeineIntake
                checkInAlcoholIntake = latest.alcoholIntake
                checkInSmokingHabit = latest.smokingHabit
                checkInWorkHours = latest.workHours
                checkInTravelTime = latest.travelTime
                checkInSocialInteractions = latest.socialInteractions
                checkInWorkload = latest.workload
                checkInMeditationPractice = latest.meditationPractice
                checkInExerciseType = latest.exerciseType
                checkInBloodPressure = latest.bloodPressure
                checkInBloodSugarLevel = latest.bloodSugarLevel
            } else {
                checkInAge = userAge.value.ifEmpty { "25" }
                checkInGender = userGender.value.ifEmpty { "Male" }
            }
        }
    }

    // --- Analysis Logic ---
    fun submitCheckIn(onSuccess: (StressResponse) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = CheckInRequest(
                    age = checkInAge.toIntOrNull() ?: 25,
                    gender = checkInGender,
                    occupation = checkInOccupation,
                    maritalStatus = checkInMaritalStatus,
                    sleepDuration = checkInSleepDuration,
                    sleepQuality = checkInSleepQuality,
                    wakeUpTime = checkInWakeUpTime,
                    bedTime = checkInBedTime,
                    physicalActivity = checkInPhysicalActivity,
                    screenTime = checkInScreenTime,
                    caffeineIntake = checkInCaffeineIntake,
                    alcoholIntake = checkInAlcoholIntake.toString(),
                    smokingHabit = checkInSmokingHabit,
                    workHours = checkInWorkHours,
                    travelTime = checkInTravelTime.toInt(),
                    socialInteractions = checkInSocialInteractions,
                    meditationPractice = checkInMeditationPractice.toString(),
                    exerciseType = checkInExerciseType,
                    bloodPressure = 120,
                    bloodSugarLevel = checkInBloodSugarLevel,
                    mood = checkInMood,
                    anxiety = checkInAnxiety,
                    caffeineDependency = checkInCaffeineDependency,
                    workload = checkInWorkload,
                    bodyFeeling = checkInBodyFeeling
                )
                
                val response = RetrofitClient.instance.sendCheckIn(request)
                
                val finalScore = response.score
                val finalLevel = when {
                    finalScore < 40 -> "Low Stress"
                    finalScore < 70 -> "Moderate Stress"
                    else -> "High Stress"
                }
                
                // Update Local UI State
                stressScore = finalScore
                stressLevel = finalLevel
                aiRecommendation = response.recommendation
                isEscalated = response.isEscalated
                
                // Save to Database
                val record = StressRecord(
                    userEmail = currentUserEmail,
                    score = finalScore,
                    level = finalLevel,
                    reasons = listOf(response.message),
                    stressIndex = finalScore,
                    ventText = ventText
                )
                stressDao.insertRecord(record)
                
                // --- Save to Clinical Database (The New History) ---
                val clinicalEntity = StressCheckInEntity(
                    stressLevel = finalLevel,
                    score = response.score,
                    recommendation = response.recommendation,
                    isEscalated = response.isEscalated,
                    age = checkInAge.toIntOrNull() ?: 25,
                    gender = checkInGender,
                    occupation = checkInOccupation,
                    maritalStatus = checkInMaritalStatus,
                    sleepDuration = checkInSleepDuration,
                    sleepQuality = checkInSleepQuality,
                    wakeUpTime = checkInWakeUpTime,
                    bedTime = checkInBedTime,
                    physicalActivity = checkInPhysicalActivity,
                    screenTime = checkInScreenTime,
                    caffeineIntake = checkInCaffeineIntake,
                    alcoholIntake = checkInAlcoholIntake,
                    smokingHabit = checkInSmokingHabit,
                    workHours = checkInWorkHours,
                    travelTime = checkInTravelTime,
                    socialInteractions = checkInSocialInteractions,
                    meditationPractice = checkInMeditationPractice,
                    exerciseType = checkInExerciseType,
                    bloodPressure = checkInBloodPressure,
                    bloodSugarLevel = checkInBloodSugarLevel,
                    mood = checkInMood,
                    anxiety = checkInAnxiety,
                    caffeineDependency = checkInCaffeineDependency,
                    workload = checkInWorkload,
                    bodyFeeling = checkInBodyFeeling
                )
                checkInRepository.insert(clinicalEntity)
                
                updateStreak()
                fetchAnalytics()
                fetchHistory()
                fetchDashboardSummary()
                onSuccess(response)
            } catch (e: Exception) {
                errorMessage = "API Error: ${e.message}"
                android.util.Log.e("CheckIn", "Failed to submit", e)
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun getCheckInById(id: Long): StressCheckInEntity? {
        return withContext(Dispatchers.IO) {
            checkInRepository.getById(id)
        }
    }

    private suspend fun updateStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newTotal = (_totalCheckins.value.toIntOrNull() ?: 0) + 1
        
        var newStreak = (_currentStreak.value.toIntOrNull() ?: 0)
        var newLongest = (_longestStreak.value.toIntOrNull() ?: 0)
        
        if (_lastCheckinDate.value != today) {
            newStreak += 1
            
            // Check if last checkin was yesterday
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            
            if (_lastCheckinDate.value != yesterday && _lastCheckinDate.value.isNotEmpty()) {
                newStreak = 1
            }
            
            newLongest = maxOf(newStreak, newLongest)
        }
        
        preferenceManager.updateStreakData(
            newStreak.toString(),
            newLongest.toString(),
            newTotal.toString(),
            today
        )
    }

    fun checkAndResetStreak() {
        viewModelScope.launch {
            val date = preferenceManager.lastCheckinDate.firstOrNull() ?: ""
            val streak = preferenceManager.currentStreak.firstOrNull() ?: "0"
            val longest = preferenceManager.longestStreak.firstOrNull() ?: "0"
            val total = preferenceManager.totalCheckins.firstOrNull() ?: "0"
            
            if (date.isNotEmpty() && streak != "0") {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                
                if (date != today && date != yesterday) {
                    preferenceManager.updateStreakData("0", longest, total, date)
                }
            }
        }
    }

    fun completeTask(taskId: String, coinReward: Int) {
        val current = _completedTasks.value
        if (current.contains(taskId)) return
        val newSet = current + taskId
        val newStr = newSet.joinToString(",")
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            preferenceManager.updateCompletedTasks(newStr, today)
            val coins = _userCoins.value.toIntOrNull() ?: 0
            preferenceManager.updateUserCoins((coins + coinReward).toString())
            
            // Fire local notification for task completion
            NotificationHelper.showNotification(
                context = getApplication<Application>(),
                channelId = NotificationHelper.CHANNEL_ENGAGEMENT,
                notificationId = (1000..9999).random(),
                title = "Task Completed! 🎉",
                message = "Great job completing your stress relief task! You earned $coinReward coins."
            )
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch { preferenceManager.setTheme(isDark) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferenceManager.setThemeMode(mode) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { preferenceManager.setLanguage(lang) }
    }

    fun updateNotificationSetting(key: String, enabled: Boolean) {
        viewModelScope.launch { preferenceManager.updateNotificationSetting(key, enabled) }
    }

    fun triggerNeedHelpFlow() {
        // Redirection to 988 or similar safety flow
        chatMessages.add(ChatMessage(text = "It sounds like you're going through a lot right now. Please remember that you're not alone. If you need immediate support, you can reach out to the 988 Suicide & Crisis Lifeline by calling or texting 988 anytime. They are here to help.", isAI = true))
    }
    
    // Additional stubs for compatibility
    fun selectRecord(record: StressRecord) {
        _selectedRecord.value = record
    }
    
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            database.notificationDao().markAllAsRead(currentUserEmail)
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            database.notificationDao().deleteNotificationById(id)
        }
    }

    fun startSleepTracking() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis().toString()
            preferenceManager.setSleepTracking(true, startTime)
        }
    }

    fun pauseSleepTracking() {
        viewModelScope.launch {
            preferenceManager.setSleepTracking(false, "0")
        }
    }

    fun exportReport(period: String, format: String, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val records = _history.value
                val checkins = checkInRepository.allCheckIns.firstOrNull() ?: emptyList()
                
                // Filter by period
                val now = System.currentTimeMillis()
                val cutoff = when (period) {
                    "Last 7 Days" -> now - 7L * 24 * 60 * 60 * 1000
                    "Last 30 Days" -> now - 30L * 24 * 60 * 60 * 1000
                    "Last 3 Months" -> now - 90L * 24 * 60 * 60 * 1000
                    else -> 0L
                }
                val filtered = if (cutoff > 0) checkins.filter { it.timestamp >= cutoff } else checkins
                
                // Build text report
                val sb = StringBuilder()
                sb.appendLine("=== CortiSense Wellness Report ===")
                sb.appendLine("User: ${_userName.value}")
                sb.appendLine("Period: $period | Format: $format")
                sb.appendLine("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
                sb.appendLine("Current Streak: ${_currentStreak.value} days")
                sb.appendLine("Total Check-ins: ${_totalCheckins.value}")
                sb.appendLine("")
                sb.appendLine("--- Check-in History (${filtered.size} records) ---")
                filtered.forEach { c ->
                    sb.appendLine("[${c.date} ${c.time}] Stress: ${c.score} (${c.stressLevel}) | Mood: ${c.mood} | Anxiety: ${c.anxiety} | Sleep: ${c.sleepDuration}h | Workload: ${c.workload}")
                }
                if (records.isNotEmpty()) {
                    val avg = records.map { it.score }.average().toInt()
                    sb.appendLine("")
                    sb.appendLine("--- Summary ---")
                    sb.appendLine("Average Stress Score: $avg")
                    sb.appendLine("Best Day: ${_bestDayThisWeek.value}")
                }
                
                val shareText = sb.toString()
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, "CortiSense Wellness Report - $period")
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val shareIntent = Intent.createChooser(sendIntent, "Export Report").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                getApplication<Application>().startActivity(shareIntent)
                onComplete("Report exported successfully!")
            } catch (e: Exception) {
                onComplete("Export failed: ${e.message}")
            }
        }
    }

    fun exportData(format: String) {
        // Implementation for data export logic
    }

    fun deleteUserAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            database.clearAllTables()
            preferenceManager.clearAll()
            onSuccess()
        }
    }

    fun fetchDashboardSummary() {
        viewModelScope.launch {
            try {
                val token = preferenceManager.jwtToken.firstOrNull()
                if (!token.isNullOrEmpty()) {
                    val summary = RetrofitClient.instance.getDashboardSummary()
                    
                    _totalCheckins.value = summary.totalCheckins.toString()
                    _currentStreak.value = summary.currentStreak.toString()
                    _longestStreak.value = summary.longestStreak.toString()
                    _todayCheckinsCount.value = summary.todayCheckinsCount
                    _todayLowestScore.value = summary.todayLowestScore
                    _avgStressThisWeek.value = summary.avgStressThisWeek
                    _bestDayThisWeek.value = summary.bestDayThisWeek
                    
                    if (summary.totalCheckins > 0) {
                        stressScore = summary.latestStressScore
                        stressLevel = summary.latestStressCategory
                        sleepHours = "${summary.latestSleepDuration} hrs"
                        
                        val sleepDec = summary.latestSleepDuration
                        val hrs = sleepDec.toInt()
                        val mins = ((sleepDec - hrs) * 60).toInt()
                        _todaySleepDuration.value = "${hrs}h ${mins}m"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            try {
                val token = preferenceManager.jwtToken.firstOrNull()
                if (!token.isNullOrEmpty()) {
                    val apiHistory = RetrofitClient.instance.getHistory()
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                    val mappedRecords = apiHistory.map { response ->
                        val factors = listOf(
                            "Sleep Duration: ${response.sleepDuration} hrs",
                            "Sleep Quality: ${response.sleepQuality}/10",
                            "Physical Activity: ${response.physicalActivity} min",
                            "Screen Time: ${response.screenTime} hrs",
                            "Workload: ${response.workload}",
                            "Mood: ${response.mood}",
                            "Anxiety: ${response.anxiety}",
                            "AI Recommendation: ${response.recommendation}"
                        )
                        var time = System.currentTimeMillis()
                        try {
                            // truncate fractional seconds and Z from timestamp for simple parsing
                            val timeStr = if (response.timestamp.contains(".")) response.timestamp.substringBefore(".") else response.timestamp
                            time = sdf.parse(timeStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {}

                        StressRecord(
                            id = response.id.toLong(),
                            userEmail = currentUserEmail,
                            score = response.score,
                            level = response.stressLevel,
                            reasons = factors,
                            timestamp = time,
                            cognitiveScore = response.score / 3,
                            emotionalScore = response.score / 3,
                            physicalScore = response.score / 3
                        )
                    }
                    _history.value = mappedRecords
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchAnalytics() {
        viewModelScope.launch {
            try {
                _weeklyAnalytics.value = RetrofitClient.instance.getWeeklyAnalytics()
                _monthlyAnalytics.value = RetrofitClient.instance.getMonthlyAnalytics()
                _trendsAnalytics.value = RetrofitClient.instance.getTrendsAnalytics()
                _factorsAnalytics.value = RetrofitClient.instance.getFactorsAnalytics()
            } catch (e: Exception) {
                // Silently handle or log error
                e.printStackTrace()
            }
        }
    }

    fun downloadUserData(onComplete: (String) -> Unit) {}
    fun buyPremium(onComplete: (String) -> Unit) {}
    fun backupChatHistory(onComplete: (String) -> Unit) {}
    fun restoreChatHistory(uri: Uri, onComplete: (String) -> Unit) {}
}
