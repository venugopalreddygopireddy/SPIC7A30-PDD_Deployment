package com.cortisense.app

import android.app.Application
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

    private val _language = MutableStateFlow("en")
    val language = _language.asStateFlow()

    private val _isUserRegistered = MutableStateFlow(false)
    val isUserRegistered = _isUserRegistered.asStateFlow()

    private val _isProfileCreated = MutableStateFlow(false)
    val isProfileCreated = _isProfileCreated.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

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
        viewModelScope.launch {
            launch { preferenceManager.isDarkTheme.collect { _isDarkTheme.value = it } }
            launch { preferenceManager.language.collect { _language.value = it } }
            launch { preferenceManager.isUserRegistered.collect { _isUserRegistered.value = it } }
            launch { preferenceManager.isProfileCreated.collect { _isProfileCreated.value = it } }
            launch { preferenceManager.isLoggedIn.collect { _isLoggedIn.value = it } }
            launch { preferenceManager.userEmail.collect { 
                _userEmail.value = it
                currentUserEmail = it
                if (it.isNotEmpty()) {
                    loadUserData(it)
                    loadNotifications(it)
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
        viewModelScope.launch {
            stressDao.getRecordsForUser(email).collect { 
                _history.value = it 
                updateDashboardMetrics(it)
            }
        }
        
        viewModelScope.launch {
            clinicalHistory.collect { checkIns ->
                if (checkIns.isNotEmpty()) {
                    val latest = checkIns.first()
                    // Dynamically set UI stats based on the latest real data
                    sleepHours = "${latest.sleepDuration} hrs"
                    
                    // Since heart rate isn't directly input, we dynamically simulate it based on stress score (higher stress = higher HR)
                    val calculatedHr = 65 + (latest.score / 2)
                    heartRate = "$calculatedHr bpm"
                    
                    // Dynamic wellness scores based on AI stress evaluation
                    cognitiveScore = (100 - (latest.score * 0.9)).toInt().coerceIn(0, 100)
                    emotionalScore = (100 - (latest.score * 1.2)).toInt().coerceIn(0, 100)
                    physicalScore = (100 - (latest.score * 0.8)).toInt().coerceIn(0, 100)
                    
                    // Update today's sleep for the main dashboard card
                    val hours = latest.sleepDuration.toInt()
                    val minutes = ((latest.sleepDuration - hours) * 60).toInt()
                    _todaySleepDuration.value = "${hours}h ${minutes}m"
                }
            }
        }
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
        viewModelScope.launch {
            preferenceManager.saveRegistration(name, email, pass)
            currentUserEmail = email
            currentUserName = name
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_userEmail.value == email && _userPassword.value == pass) {
                preferenceManager.setLoggedIn(true)
                onSuccess()
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (_userEmail.value == email) {
                onSuccess()
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
            preferenceManager.setLoggedIn(false)
        }
    }

    fun resetInputs() {
        quizAnswers = List(9) { -1 }
        ventText = ""
        reasons = emptyList()
    }

    fun initializeCheckInForm() {
        viewModelScope.launch {
            val latest = kotlinx.coroutines.flow.firstOrNull(checkInRepository.allCheckIns)?.firstOrNull()
            if (latest != null) {
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
                
                // Update Local UI State
                stressScore = response.score
                stressLevel = response.stressLevel
                aiRecommendation = response.recommendation
                isEscalated = response.isEscalated
                
                // Save to Database
                val record = StressRecord(
                    userEmail = currentUserEmail,
                    score = response.score,
                    level = response.stressLevel,
                    reasons = listOf(response.message),
                    stressIndex = response.score,
                    ventText = ventText
                )
                stressDao.insertRecord(record)
                
                // --- Save to Clinical Database (The New History) ---
                val clinicalEntity = StressCheckInEntity(
                    stressLevel = response.stressLevel,
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
        if (_lastCheckinDate.value != today) {
            val newTotal = (_totalCheckins.value.toIntOrNull() ?: 0) + 1
            var newStreak = (_currentStreak.value.toIntOrNull() ?: 0) + 1
            
            // Check if last checkin was yesterday
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            
            if (_lastCheckinDate.value != yesterday && _lastCheckinDate.value.isNotEmpty()) {
                newStreak = 1
            }
            
            val newLongest = maxOf(newStreak, _longestStreak.value.toIntOrNull() ?: 0)
            
            preferenceManager.updateStreakData(
                newStreak.toString(),
                newLongest.toString(),
                newTotal.toString(),
                today
            )
        }
    }

    fun setTheme(isDark: Boolean) {
        viewModelScope.launch { preferenceManager.setTheme(isDark) }
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

    fun exportReport(period: String, format: String, onComplete: (String) -> Unit) {}

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

                                                                                                                                                                                                                                                                                                                                                                fun downloadUserData(onComplete: (String) -> Unit) {}
    fun buyPremium(onComplete: (String) -> Unit) {}
    fun backupChatHistory(onComplete: (String) -> Unit) {}
    fun restoreChatHistory(uri: Uri, onComplete: (String) -> Unit) {}
}
