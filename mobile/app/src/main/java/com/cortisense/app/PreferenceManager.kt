package com.cortisense.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {
    companion object {
        val THEME_KEY = booleanPreferencesKey("dark_theme")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        
        val USERNAME_KEY = stringPreferencesKey("username")
        val EMAIL_KEY = stringPreferencesKey("email")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val AGE_KEY = stringPreferencesKey("userAge")
        val GENDER_KEY = stringPreferencesKey("userGender")
        val GOAL_KEY = stringPreferencesKey("userGoal")
        val PROFILE_IMAGE_URI_KEY = stringPreferencesKey("profileImageUri")
        
        val IS_USER_REGISTERED_KEY = booleanPreferencesKey("isUserRegistered")
        val IS_PROFILE_CREATED_KEY = booleanPreferencesKey("isProfileCreated")
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("isLoggedIn")
        
        val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
        
        // Notification Settings
        val NOTIF_CHECKIN_KEY = booleanPreferencesKey("notif_checkin")
        val NOTIF_STRESS_ALERTS_KEY = booleanPreferencesKey("notif_stress_alerts")
        val NOTIF_RECOMMENDATIONS_KEY = booleanPreferencesKey("notif_recommendations")
        val NOTIF_ACHIEVEMENTS_KEY = booleanPreferencesKey("notif_achievements")
        
        // Streak & Stats
        val CURRENT_STREAK_KEY = stringPreferencesKey("current_streak")
        val LONGEST_STREAK_KEY = stringPreferencesKey("longest_streak")
        val TOTAL_CHECKINS_KEY = stringPreferencesKey("total_checkins")
        val LAST_CHECKIN_DATE_KEY = stringPreferencesKey("last_checkin_date")
        val USER_COINS_KEY = stringPreferencesKey("user_coins")
        val SLEEP_START_TIME_KEY = stringPreferencesKey("sleep_start_time")
        val IS_SLEEP_TRACKING_KEY = booleanPreferencesKey("is_sleep_tracking")
        
        val COMPLETED_TASKS_KEY = stringPreferencesKey("completed_tasks")
        val LAST_TASK_DATE_KEY = stringPreferencesKey("last_task_date")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system"
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: "en"
    }

    val userName: Flow<String> = context.dataStore.data.map { it[USERNAME_KEY] ?: "" }
    val userEmail: Flow<String> = context.dataStore.data.map { it[EMAIL_KEY] ?: "" }
    val userPassword: Flow<String> = context.dataStore.data.map { it[PASSWORD_KEY] ?: "" }
    val userAge: Flow<String> = context.dataStore.data.map { it[AGE_KEY] ?: "25" }
    val userGender: Flow<String> = context.dataStore.data.map { it[GENDER_KEY] ?: "" }
    val userGoal: Flow<String> = context.dataStore.data.map { it[GOAL_KEY] ?: "" }
    val profileImageUri: Flow<String> = context.dataStore.data.map { it[PROFILE_IMAGE_URI_KEY] ?: "" }
    val isPremium: Flow<Boolean> = context.dataStore.data.map { it[booleanPreferencesKey("is_premium")] ?: false }
    
    val isUserRegistered: Flow<Boolean> = context.dataStore.data.map { it[IS_USER_REGISTERED_KEY] ?: false }
    val isProfileCreated: Flow<Boolean> = context.dataStore.data.map { it[IS_PROFILE_CREATED_KEY] ?: false }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[IS_LOGGED_IN_KEY] ?: false }
    val jwtToken: Flow<String> = context.dataStore.data.map { it[JWT_TOKEN_KEY] ?: "" }

    val notifCheckin: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_CHECKIN_KEY] ?: true }
    val notifStressAlerts: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_STRESS_ALERTS_KEY] ?: true }
    val notifRecommendations: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_RECOMMENDATIONS_KEY] ?: true }
    val notifAchievements: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_ACHIEVEMENTS_KEY] ?: false }

    val currentStreak: Flow<String> = context.dataStore.data.map { it[CURRENT_STREAK_KEY] ?: "0" }
    val longestStreak: Flow<String> = context.dataStore.data.map { it[LONGEST_STREAK_KEY] ?: "0" }
    val totalCheckins: Flow<String> = context.dataStore.data.map { it[TOTAL_CHECKINS_KEY] ?: "0" }
    val lastCheckinDate: Flow<String> = context.dataStore.data.map { it[LAST_CHECKIN_DATE_KEY] ?: "" }
    val userCoins: Flow<String> = context.dataStore.data.map { it[USER_COINS_KEY] ?: "0" }
    val sleepStartTime: Flow<String> = context.dataStore.data.map { it[SLEEP_START_TIME_KEY] ?: "0" }
    val isSleepTracking: Flow<Boolean> = context.dataStore.data.map { it[IS_SLEEP_TRACKING_KEY] ?: false }

    val completedTasks: Flow<String> = context.dataStore.data.map { it[COMPLETED_TASKS_KEY] ?: "" }
    val lastTaskDate: Flow<String> = context.dataStore.data.map { it[LAST_TASK_DATE_KEY] ?: "" }

    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDark
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = lang
        }
    }

    suspend fun saveRegistration(name: String, email: String, pass: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = name
            preferences[EMAIL_KEY] = email
            preferences[PASSWORD_KEY] = pass
            preferences[IS_USER_REGISTERED_KEY] = true
        }
    }

    suspend fun saveProfileSetup(age: String, gender: String, goal: String) {
        context.dataStore.edit { preferences ->
            preferences[AGE_KEY] = age
            preferences[GENDER_KEY] = gender
            preferences[GOAL_KEY] = goal
            preferences[IS_PROFILE_CREATED_KEY] = true
        }
    }
    
    suspend fun updateProfile(name: String, age: String, gender: String, goal: String, imageUri: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = name
            preferences[AGE_KEY] = age
            preferences[GENDER_KEY] = gender
            preferences[GOAL_KEY] = goal
            preferences[PROFILE_IMAGE_URI_KEY] = imageUri
        }
    }
    
    suspend fun resetPassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD_KEY] = password
        }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    suspend fun saveJwtToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    suspend fun updateNotificationSetting(key: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when(key) {
                "checkin" -> prefs[NOTIF_CHECKIN_KEY] = enabled
                "stress" -> prefs[NOTIF_STRESS_ALERTS_KEY] = enabled
                "rec" -> prefs[NOTIF_RECOMMENDATIONS_KEY] = enabled
                "ach" -> prefs[NOTIF_ACHIEVEMENTS_KEY] = enabled
            }
        }
    }

    suspend fun updateStreakData(current: String, longest: String, total: String, lastDate: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_STREAK_KEY] = current
            prefs[LONGEST_STREAK_KEY] = longest
            prefs[TOTAL_CHECKINS_KEY] = total
            prefs[LAST_CHECKIN_DATE_KEY] = lastDate
        }
    }

    suspend fun updateUserCoins(coins: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_COINS_KEY] = coins
        }
    }
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun setPremium(value: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey("is_premium")] = value }
    }

    suspend fun setSleepTracking(isTracking: Boolean, startTime: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_SLEEP_TRACKING_KEY] = isTracking
            prefs[SLEEP_START_TIME_KEY] = startTime
        }
    }

    suspend fun updateCompletedTasks(tasks: String, date: String) {
        context.dataStore.edit { prefs ->
            prefs[COMPLETED_TASKS_KEY] = tasks
            prefs[LAST_TASK_DATE_KEY] = date
        }
    }
}
