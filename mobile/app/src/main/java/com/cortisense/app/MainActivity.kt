package com.cortisense.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import com.cortisense.app.ui.theme.CortiSenseTheme
import com.cortisense.app.ui.theme.SageGreen
import com.cortisense.app.ui.theme.DustyBlue
import com.cortisense.app.ui.theme.WarmCream
import com.cortisense.app.ui.theme.DeepIndigo
import com.cortisense.app.ui.theme.TwilightOrange
import com.cortisense.app.ui.theme.SoftSage
import com.cortisense.app.ui.theme.TextDark
import com.cortisense.app.ui.theme.TextLight
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.CenterFocusStrong

import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.clip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.CreateCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.app.Activity

@Composable
fun getTranslatedStressLevel(levelKey: String): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(levelKey, "string", context.packageName)
    return if (resId != 0) stringResource(resId) else levelKey
}

@Composable
fun getTranslatedReason(reasonKey: String): String {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(reasonKey, "string", context.packageName)
    return if (resId != 0) stringResource(resId) else reasonKey
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply smooth transition if activity is being recreated (e.g. language/theme change)
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        
        NotificationHelper.createNotificationChannels(this)
        NotificationHelper.scheduleNotifications(this)
        
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val navController = rememberNavController()
            
            val themeMode by viewModel.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }
            val language by viewModel.language.collectAsState()

            // Handle locale change gracefully
            LaunchedEffect(language) {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language)
                val currentLocales = AppCompatDelegate.getApplicationLocales()
                
                // Only set if different to prevent infinite recreation loops or unnecessary flashes
                if (currentLocales.toLanguageTags() != language) {
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
            }

            // Request Notification Permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                
                LaunchedEffect(Unit) {
                    if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            CortiSenseTheme(darkTheme = isDarkTheme) {
                // Persistent state management
                val registeredName by viewModel.userName.collectAsState()
                val registeredEmail by viewModel.userEmail.collectAsState()
                val registeredPassword by viewModel.userPassword.collectAsState()
                val isUserRegistered by viewModel.isUserRegistered.collectAsState()
                val isProfileCreated by viewModel.isProfileCreated.collectAsState()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                NavHost(
                    navController = navController, 
                    startDestination = "splash"
                ) {
                        composable("splash") {
                            SplashScreen(onTimeout = { 
                                val destination = when {
                                    isUserRegistered && isLoggedIn && isProfileCreated -> "dashboard"
                                    isUserRegistered && isLoggedIn && !isProfileCreated -> "profile_setup"
                                    isUserRegistered -> "login"
                                    else -> "welcome"
                                }
                                navController.navigate(destination) {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("welcome") {
                            WelcomeScreen(
                                onGetStarted = { navController.navigate("signup") },
                                onSignIn = { navController.navigate("login") }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onLogin = { email, password -> 
                                    viewModel.login(email, password) {
                                        val destination = if (isProfileCreated) "dashboard" else "profile_setup"
                                        navController.navigate(destination) {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                },
                                onSignUpClick = { navController.navigate("signup") },
                                onForgotPasswordClick = { navController.navigate("forgot_password") },
                                viewModel = viewModel
                            )
                        }
                        composable("signup") {
                            val context = LocalContext.current
                            SignupScreen(
                                backendError = viewModel.errorMessage,
                                isLoading = viewModel.isLoading,
                                onCreateAccount = { name, email, password ->
                                    val parts = name.trim().split(" ", limit = 2)
                                    val firstName = parts.getOrNull(0) ?: name
                                    val lastName = parts.getOrNull(1) ?: ""
                                    
                                    viewModel.registerWithApi(
                                        firstName = firstName.ifEmpty { "User" },
                                        lastName = lastName,
                                        age = 25,
                                        gender = "Not specified",
                                        email = email,
                                        pass = password,
                                        onSuccess = {
                                            android.widget.Toast.makeText(
                                                context, 
                                                "Account created successfully. Please sign in.", 
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                            navController.navigate("login") {
                                                popUpTo("signup") { inclusive = true }
                                            }
                                        }
                                    )
                                },
                                onSignInClick = { navController.navigate("login") }
                            )
                        }
                        composable("forgot_password") {
                            val context = LocalContext.current
                            ForgotPasswordScreen(
                                onBackToLogin = { navController.popBackStack() },
                                onPasswordReset = { email -> 
                                    viewModel.forgotPasswordApi(email) { _ ->
                                        android.widget.Toast.makeText(
                                            context, 
                                            "OTP sent to your email!", 
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate("verify_otp/$email")
                                    }
                                }
                            )
                        }
                        composable(
                            "verify_otp/{email}",
                            arguments = listOf(androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val context = LocalContext.current
                            VerifyOTPScreen(
                                email = email,
                                onBack = { navController.popBackStack() },
                                onVerify = { otp ->
                                    viewModel.verifyOtpApi(email, otp) {
                                        navController.navigate("reset_password/$email/$otp") {
                                            popUpTo("forgot_password") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable(
                            "reset_password/{email}/{otp}",
                            arguments = listOf(
                                androidx.navigation.navArgument("email") { type = androidx.navigation.NavType.StringType },
                                androidx.navigation.navArgument("otp") { type = androidx.navigation.NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email") ?: ""
                            val otp = backStackEntry.arguments?.getString("otp") ?: ""
                            val context = LocalContext.current
                            ResetPasswordScreen(
                                email = email,
                                onBack = { navController.popBackStack() },
                                onReset = { newPass ->
                                    viewModel.resetPasswordApi(email, otp, newPass) {
                                        android.widget.Toast.makeText(
                                            context, 
                                            "Password reset successfully. Please sign in.", 
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigate("login") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("dashboard") {
                            LaunchedEffect(Unit) {
                                viewModel.checkAndResetStreak()
                            }
                            CortiSenseScreen(
                                viewModel = viewModel,
                                navController = navController,
                                onLogout = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("dailyCheckin") {
                            ClinicalCheckInScreen(
                                viewModel = viewModel,
                                onAnalyze = { _ ->
                                    navController.navigate("analysis")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("analysis") {
                            AnalyzingDataScreen(
                                viewModel = viewModel,
                                onFinished = {
                                    navController.navigate("result") {
                                        popUpTo("analysis") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("result") {
                            ClinicalResultScreen(
                                viewModel = viewModel,
                                onRecheck = {
                                    viewModel.resetInputs()
                                    navController.navigate("dailyCheckin") {
                                        popUpTo("result") { inclusive = true }
                                    }
                                },
                                onViewInsights = {
                                    navController.navigate("checkinHistory") {
                                        popUpTo("result") { inclusive = true }
                                    }
                                },
                                onStartBreathing = {
                                    navController.navigate("breathing_from_result")
                                },
                                onGrounding = {
                                    navController.navigate("grounding")
                                }
                            )
                        }
                        composable("checkinHistory") {
                            CheckInHistoryScreen(
                                viewModel = viewModel,
                                onDetail = { id -> navController.navigate("checkinDetail/$id") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "checkinDetail/{checkInId}",
                            arguments = listOf(navArgument("checkInId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong("checkInId") ?: 0L
                            CheckInDetailScreen(
                                checkInId = id,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("history_detail") {
                            HistoryDetailScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onStartBreathing = { navController.navigate("breathing_from_result") }
                            )
                        }

                        composable("grounding") {
                            GroundingScreen(
                                onComplete = {
                                    navController.navigate("dashboard") {
                                        popUpTo("grounding") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("breathing_from_result") {
                            BreathingExerciseScreen(
                                onClose = { navController.popBackStack() }
                            )
                        }
                        composable("profile_setup") {
                            ProfileSetupScreen(
                                initialName = registeredName,
                                onContinue = { name, age, gender, goal ->
                                    viewModel.saveProfileSetup(name, age, gender, goal)
                                    navController.navigate("onboarding")
                                }
                            )
                        }
                        composable("onboarding") {
                            OnboardingScreen(onContinue = { navController.navigate("permissions") })
                        }
                        composable("permissions") {
                            PermissionsScreen(
                                onAllowAccess = { navController.navigate("dashboard") },
                                onSkip = { navController.navigate("dashboard") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                navController = navController,
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("edit_profile") {
                            ProfileScreen(
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                        composable("chat") {
                            CortiChatScreen(viewModel = viewModel)
                        }
                    }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(
    initialName: String,
    onContinue: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember(initialName) { mutableStateOf(initialName) }
    var dob by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("25") }
    var gender by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(stringResource(R.string.extracted_complete_your_profil),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(stringResource(R.string.extracted_help_us_personalize_),
                fontSize = 16.sp,
                color = subtitleColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.personal_details),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CustomTextField(
                label = stringResource(R.string.full_name_label),
                value = name,
                onValueChange = { name = it; errorMessage = null }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = stringResource(R.string.dob_label),
                value = dob,
                onValueChange = { dob = it; errorMessage = null },
                placeholder = stringResource(R.string.dob_placeholder)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.age),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = age,
                onValueChange = { age = it; errorMessage = null },
                placeholder = { Text(stringResource(R.string.enter_age)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.gender),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val genders = listOf(
                    stringResource(R.string.gender_male),
                    stringResource(R.string.gender_female),
                    stringResource(R.string.gender_other)
                )
                genders.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g; errorMessage = null },
                        label = { Text(g) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tealColor.copy(alpha = 0.2f),
                            selectedLabelColor = tealColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.primary_goal_question),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val goals = listOf(
                "Reduce Stress" to stringResource(R.string.reduce_stress),
                "Better Sleep" to stringResource(R.string.better_sleep),
                "Improve Focus" to stringResource(R.string.improve_focus)
            )
            goals.forEach { (goalKey, goalLabel) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (selectedGoal == goalKey) tealColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedGoal = goalKey; errorMessage = null }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGoal == goalKey,
                        onClick = { selectedGoal = goalKey; errorMessage = null },
                        colors = RadioButtonDefaults.colors(selectedColor = tealColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(goalLabel, color = if (selectedGoal == goalKey) tealColor else textColor, fontWeight = if (selectedGoal == goalKey) FontWeight.Bold else FontWeight.Normal)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (name.isBlank() || dob.isBlank() || age.isBlank() || gender.isBlank() || selectedGoal.isBlank()) {
                        errorMessage = context.getString(R.string.error_all_fields)
                    } else {
                        onContinue(name, age, gender, selectedGoal)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.continue_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileSetupPreview() {
    CortiSenseTheme {
        ProfileSetupScreen(initialName = stringResource(R.string.fullname_placeholder), onContinue = { _, _, _, _ -> })
    }
}

@Composable
fun PermissionsScreen(onAllowAccess: () -> Unit, onSkip: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lightTealBg = MaterialTheme.colorScheme.primaryContainer

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(stringResource(R.string.extracted_grant_permissions),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(stringResource(R.string.extracted_we_need_a_few_permis),
                fontSize = 16.sp,
                color = subtitleColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Activity Tracking Card
            PermissionCard(
                icon = Icons.Default.Timeline,
                iconBg = lightTealBg,
                iconTint = tealColor,
                title = stringResource(R.string.activity_tracking_title),
                description = stringResource(R.string.activity_tracking_desc_full),
                toggleLabel = stringResource(R.string.enable_tracking_label)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Card
            PermissionCard(
                icon = Icons.Default.NotificationsNone,
                iconBg = lightTealBg,
                iconTint = tealColor,
                title = stringResource(R.string.notifications_title),
                description = stringResource(R.string.notifications_desc_full),
                toggleLabel = stringResource(R.string.enable_notifications_label)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Allow Access Button
            Button(
                onClick = onAllowAccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Text(stringResource(R.string.extracted_allow_access),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip Text
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.skip_for_now),
                    color = subtitleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    toggleLabel: String
) {
    var isChecked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = toggleLabel,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PermissionsPreview() {
    CortiSenseTheme {
        PermissionsScreen(onAllowAccess = {}, onSkip = {})
    }
}

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lightTealBg = MaterialTheme.colorScheme.primaryContainer

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(stringResource(R.string.extracted_how_it_works),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(stringResource(R.string.extracted_three_simple_steps_t),
                fontSize = 16.sp,
                color = subtitleColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Step 1
            OnboardingStep(
                icon = Icons.Default.Timeline,
                iconBg = lightTealBg,
                iconTint = tealColor,
                title = stringResource(R.string.track_step_title),
                description = stringResource(R.string.track_step_desc)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Step 2
            OnboardingStep(
                icon = Icons.Default.Psychology,
                iconBg = lightTealBg,
                iconTint = tealColor,
                title = stringResource(R.string.analyze_step_title),
                description = stringResource(R.string.analyze_step_desc)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Step 3
            OnboardingStep(
                icon = Icons.Default.AutoGraph,
                iconBg = lightTealBg,
                iconTint = tealColor,
                title = stringResource(R.string.improve_step_title),
                description = stringResource(R.string.improve_step_desc)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pager Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == 1) 24.dp else 8.dp, 8.dp)
                            .background(
                                color = if (index == 1) tealColor else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.extracted_continue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStep(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingPreview() {
    CortiSenseTheme {
        OnboardingScreen(onContinue = {})
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
// ... (rest of SplashScreen remains same)
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds delay
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo (User Picture or fallback)
                val viewModel: MainViewModel = viewModel()
                val imageUri by viewModel.profileImageUri.collectAsState(initial = "")
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(tealColor, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri.isNotEmpty()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "App Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "C",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(stringResource(R.string.app_name),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline
                Text(stringResource(R.string.extracted_predict_stress_preve),
                    fontSize = 16.sp,
                    color = subtitleColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Loading dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(tealColor, RoundedCornerShape(50))
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    CortiSenseTheme {
        SplashScreen(onTimeout = {})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val credentialManager = remember { CredentialManager.create(context) }

    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                // Successfully selected a Google account
                email = accountName
                onLogin(email, "google_oauth_placeholder")
            } else {
                errorMessage = context.getString(R.string.auth_error_no_email)
            }
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            errorMessage = viewModel.errorMessage
            viewModel.errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        try {
            val getPasswordOption = GetPasswordOption()
            val request = GetCredentialRequest(listOf(getPasswordOption))
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is PasswordCredential) {
                email = credential.id
                password = credential.password
                onLogin(email, password)
            }
        } catch (e: Exception) {
            // Ignored - user might not have saved passwords
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            val imageUri by viewModel.profileImageUri.collectAsState(initial = "")
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(tealColor, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "App Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "C",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.welcome_back),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(stringResource(R.string.extracted_sign_in_to_continue_),
                fontSize = 14.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                label = stringResource(R.string.email_address),
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = null
                },
                placeholder = "john@example.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = stringResource(R.string.password),
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = null
                },
                isPassword = true
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    stringResource(R.string.forgot_password),
                    color = tealColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                val request = CreatePasswordRequest(email, password)
                                credentialManager.createCredential(context as Activity, request)
                            } catch (e: Exception) {}
                            onLogin(email, password)
                        }
                    } else {
                        errorMessage = context.getString(R.string.auth_error_credentials)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.sign_in), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    try {
                        val intent = android.accounts.AccountManager.newChooseAccountIntent(
                            null, null, arrayOf("com.google"), null, null, null, null
                        )
                        googleSignInLauncher.launch(intent)
                    } catch (e: Exception) {
                        errorMessage = context.getString(R.string.auth_error_google)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.sign_in_with_google), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.extracted_already_have_an_acco), color = subtitleColor)
                TextButton(onClick = onSignUpClick) {
                    Text(stringResource(R.string.signup), color = tealColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onPasswordReset: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    
    val context = LocalContext.current

    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lightTealBg = MaterialTheme.colorScheme.primaryContainer

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(lightTealBg, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = null,
                    tint = tealColor,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val title = stringResource(R.string.reset_password_title)
            val desc = stringResource(R.string.reset_password_desc_full)

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Text(
                text = desc,
                fontSize = 14.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                label = stringResource(R.string.email_address_label),
                value = email,
                onValueChange = { email = it; errorMessage = null },
                placeholder = "john@example.com"
            )


            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = tealColor,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = context.getString(R.string.invalid_email)
                    } else {
                        onPasswordReset(email)
                        successMessage = context.getString(R.string.reset_link_sent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.send_reset_link_btn), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }


            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBackToLogin) {
                Text(stringResource(R.string.back_to_signin), color = subtitleColor)
            }
        }
    }
}

@Composable
fun ForgotPasswordPreview() {
    CortiSenseTheme {
        ForgotPasswordScreen(onBackToLogin = {}, onPasswordReset = {})
    }
}

@Composable
fun SignupScreen(
    backendError: String? = null,
    isLoading: Boolean = false,
    onCreateAccount: (String, String, String) -> Unit, 
    onSignInClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(backendError) {
        if (backendError != null) {
            errorMessage = backendError
        }
    }

    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val credentialManager = remember { CredentialManager.create(context) }

    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                // Split email to get a name approximation
                val parsedName = accountName.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                onCreateAccount(parsedName, accountName, "google_oauth_placeholder")
            } else {
                errorMessage = context.getString(R.string.auth_error_no_email)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.create_account),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(stringResource(R.string.signup_subtitle),
                fontSize = 14.sp,
                color = subtitleColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                label = stringResource(R.string.full_name),
                value = name,
                onValueChange = { name = it; errorMessage = null },
                placeholder = stringResource(R.string.fullname_placeholder)
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = stringResource(R.string.email_address),
                value = email,
                onValueChange = { email = it; errorMessage = null },
                placeholder = "john@example.com"
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = stringResource(R.string.password),
                value = password,
                onValueChange = { password = it; errorMessage = null },
                isPassword = true
            )
            Text(stringResource(R.string.password_length_tip),
                fontSize = 12.sp,
                color = subtitleColor,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 4.dp, start = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = stringResource(R.string.confirm_password),
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                isPassword = true
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    try {
                        val intent = android.accounts.AccountManager.newChooseAccountIntent(
                            null, null, arrayOf("com.google"), null, null, null, null
                        )
                        googleSignInLauncher.launch(intent)
                    } catch (e: Exception) {
                        errorMessage = context.getString(R.string.auth_error_google)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor), // Make it prominent for new users
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.sign_in_with_google), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "OR",
                color = subtitleColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = context.getString(R.string.auth_error_fields)
                    } else if (password != confirmPassword) {
                        errorMessage = context.getString(R.string.passwords_do_not_match)
                    } else {
                        coroutineScope.launch {
                            try {
                                val request = CreatePasswordRequest(email, password)
                                credentialManager.createCredential(context as Activity, request)
                            } catch (e: Exception) {}
                            onCreateAccount(name, email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.create_account), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.extracted_already_have_an_acco), color = subtitleColor)
                TextButton(onClick = onSignInClick) {
                    Text(stringResource(R.string.sign_in), color = tealColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupPreview() {
    CortiSenseTheme {
        SignupScreen(onCreateAccount = { _, _, _ -> }, onSignInClick = {})
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onSignIn: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(tealColor, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = stringResource(R.string.welcome),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle
            Text(
                text = stringResource(R.string.welcome_subtitle),
                fontSize = 16.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.welcome_description),
                fontSize = 14.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Get Started Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.get_started),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign In Button
            OutlinedButton(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    CortiSenseTheme {
        WelcomeScreen(onGetStarted = {}, onSignIn = {})
    }
}

@Composable
fun CortiSenseScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf("Home") }
    var showAlertByScore by rememberSaveable { mutableStateOf<Int?>(null) }
    var currentSubScreen by rememberSaveable { mutableStateOf<String?>(null) }
    val tabBackStack = remember { mutableStateListOf<String>() }
    val isSafeMode by viewModel.isSafeMode.collectAsState()

    // Force Home tab in Safe Mode
    LaunchedEffect(isSafeMode) {
        if (isSafeMode) {
            selectedTab = "Home"
            currentSubScreen = null
        }
    }
    
    // Handle system back button for internal sub-screens and tab history
    BackHandler(enabled = currentSubScreen != null || showAlertByScore != null || selectedTab != "Home" || tabBackStack.isNotEmpty()) {
        if (showAlertByScore != null) {
            showAlertByScore = null
        } else if (currentSubScreen != null) {
            when (currentSubScreen) {
                "alert_detail" -> currentSubScreen = "notifications"
                "breathing", "sleep_guide", "activity_plan", "nutrition_tips", "mindfulness" -> currentSubScreen = "recommendations"
                else -> currentSubScreen = null
            }
        } else if (selectedTab == "Achievements") {
            selectedTab = "Profile"
        } else if (tabBackStack.isNotEmpty()) {
            selectedTab = tabBackStack.removeAt(tabBackStack.size - 1)
        } else if (selectedTab != "Home") {
            selectedTab = "Home"
        }
    }
    
    val language by viewModel.language.collectAsState()
    val tealColor = MaterialTheme.colorScheme.primary

    when {
        isSafeMode -> {
            SafeModeScreen(onStartBreathing = { currentSubScreen = "breathing" }, onTalkToAI = { currentSubScreen = "recommendations" })
        }
        showAlertByScore != null -> {
            StressAlertScreen(
                score = showAlertByScore!!, 
                onBack = { showAlertByScore = null },
                onStartBreathing = { 
                    showAlertByScore = null
                    currentSubScreen = "breathing" 
                },
                viewModel = viewModel
            )
        }
        currentSubScreen == "notifications" -> {
            NotificationsScreen(viewModel = viewModel, onBack = { currentSubScreen = null }, onOpenDetail = { currentSubScreen = "alert_detail" })
        }
        currentSubScreen == "alert_detail" -> {
            AlertDetailScreen(viewModel = viewModel, onBack = { currentSubScreen = "notifications" })
        }
        currentSubScreen == "streak_tracker" -> {
            StreakTrackerScreen(viewModel = viewModel, onBack = { currentSubScreen = null })
        }
        currentSubScreen == "export_report" -> {
            ExportReportScreen(viewModel = viewModel, onBack = { currentSubScreen = null })
        }
        currentSubScreen == "share_results" -> {
            ShareResultsScreen(viewModel = viewModel, onBack = { currentSubScreen = null })
        }
        currentSubScreen == "error" -> {
            ErrorScreen(onRetry = { currentSubScreen = null }, onBackHome = { selectedTab = "Home"; currentSubScreen = null })
        }
        currentSubScreen == "recommendations" -> {
            RecommendationsScreen(
                viewModel = viewModel,
                onBack = { currentSubScreen = null },
                onStartBreathing = { currentSubScreen = "breathing" },
                onViewSleepGuide = { currentSubScreen = "sleep_guide" },
                onViewActivityPlan = { currentSubScreen = "activity_plan" },
                onViewNutritionTips = { currentSubScreen = "nutrition_tips" },
                onStartMindfulness = { currentSubScreen = "mindfulness" }
            )
        }
        currentSubScreen == "breathing" -> {
            BreathingExerciseScreen(onClose = { currentSubScreen = "recommendations" })
        }
        currentSubScreen == "sleep_guide" -> {
            SleepOptimizationScreen(onBack = { currentSubScreen = "recommendations" })
        }
        currentSubScreen == "activity_plan" -> {
            ActivityPlanScreen(onBack = { currentSubScreen = "recommendations" })
        }
        currentSubScreen == "nutrition_tips" -> {
            NutritionTipsScreen(onBack = { currentSubScreen = "recommendations" })
        }
        currentSubScreen == "mindfulness" -> {
            MindfulnessScreen(onBack = { currentSubScreen = "recommendations" }, onComplete = { currentSubScreen = "completion" })
        }
        currentSubScreen == "completion" -> {
            ExerciseCompletionScreen(
                onViewProgress = { 
                    selectedTab = "Analytics"
                    currentSubScreen = null 
                },
                onBackHome = { 
                    selectedTab = "Home"
                    currentSubScreen = null 
                }
            )
        }
        else -> {
            Scaffold(
                bottomBar = {
                    if (!isSafeMode) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                Triple("Home", Icons.Outlined.Home, Icons.Filled.Home),
                                Triple("Analytics", Icons.Outlined.AutoGraph, Icons.Filled.AutoGraph),
                                Triple("Check-in", Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircle),
                                Triple("Profile", Icons.Outlined.PersonOutline, Icons.Filled.Person)
                            )

                            items.forEach { (label, icon, selectedIcon) ->
                                val translatedLabel = when (label) {
                                    "Home" -> stringResource(R.string.tab_home)
                                    "Analytics" -> stringResource(R.string.tab_analytics)
                                    "Check-in" -> stringResource(R.string.tab_checkin)
                                    "Profile" -> stringResource(R.string.tab_profile)
                                    else -> label
                                }
                                NavigationBarItem(
                                    selected = selectedTab == label,
                                    onClick = {
                                        if (label == "Check-in") {
                                            viewModel.initializeCheckInForm()
                                            navController.navigate("dailyCheckin")
                                        } else if (selectedTab != label) {
                                            tabBackStack.add(selectedTab)
                                            selectedTab = label
                                            currentSubScreen = null
                                        }
                                    },
                                    label = { Text(translatedLabel, fontSize = 10.sp) },
                                    icon = {
                                        if (label == "Profile") {
                                            val imageUri by viewModel.profileImageUri.collectAsState()
                                            if (imageUri.isNotEmpty()) {
                                                AsyncImage(
                                                    model = imageUri,
                                                    contentDescription = "Profile",
                                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (selectedTab == label) selectedIcon else icon,
                                                    contentDescription = label
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (selectedTab == label) selectedIcon else icon,
                                                contentDescription = label
                                            )
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = tealColor,
                                        selectedTextColor = tealColor,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedTab) {
                        "Home" -> HomeScreen(
                            viewModel = viewModel,
                            navController = navController,
                            onShowAlert = { showAlertByScore = it },
                            onShowNotifications = { currentSubScreen = "notifications" },
                            onViewRecommendations = { currentSubScreen = "recommendations" }
                        )
                        "Analytics" -> AnalyticsScreen(
                            viewModel = viewModel,
                            navController = navController,
                            onExport = { currentSubScreen = "export_report" },
                            onShare = { currentSubScreen = "share_results" }
                        )
                        "Check-in" -> { /* Handled in NavigationBarItem onClick */ }
                        "Profile" -> ProfileFlow(
                            viewModel = viewModel,
                            onAchievements = { selectedTab = "Achievements" },
                            onStreak = { currentSubScreen = "streak_tracker" },
                            onClinicalHistory = { navController.navigate("checkinHistory") },
                            onLogout = onLogout,
                            currentLanguage = language,
                            onNavigateToChat = { navController.navigate("chat") }
                        )
                        "Achievements" -> AchievementsScreen(viewModel = viewModel, onBack = { selectedTab = "Profile" })
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = stringResource(R.string.extracted_selectedtab_screen_c, selectedTab))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StressOrb(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val orbColor = when(viewModel.stressLevel) {
        "stress_level_low" -> SageGreen.copy(alpha = alpha)
        "stress_level_moderate" -> TwilightOrange.copy(alpha = alpha)
        "stress_level_high" -> Color(0xFFFF9800).copy(alpha = alpha)
        "stress_level_critical" -> Color(0xFFFF4B4B).copy(alpha = alpha)
        else -> SageGreen.copy(alpha = alpha)
    }

    Box(
        modifier = modifier
            .size(280.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orbColor, Color.Transparent),
                    center = center,
                    radius = size.width / 2
                )
            )
        }
        
        // Inner core
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.4f), orbColor.copy(alpha = 0.8f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(100.dp)
                )
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = viewModel.stressScore.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = getTranslatedStressLevel(viewModel.stressLevel),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ProfileFlow(
    viewModel: MainViewModel, 
    onAchievements: () -> Unit, 
    onStreak: () -> Unit, 
    onClinicalHistory: () -> Unit,
    onLogout: () -> Unit, 
    currentLanguage: String,
    onNavigateToChat: () -> Unit
) {
    var subScreen by rememberSaveable { mutableStateOf("main") }
    
    BackHandler(enabled = subScreen != "main") {
        subScreen = "main"
    }
    
    when (subScreen) {
        "main" -> ProfileMainScreenContent(
            onEditProfile = { subScreen = "edit" },
            onNotifications = { subScreen = "notifications" },
            onSettings = { subScreen = "settings" },
            onAppearance = { subScreen = "appearance" },
            onLanguage = { subScreen = "language" },
            onPrivacy = { subScreen = "privacy" },
            onAbout = { subScreen = "about" },
            onAchievements = onAchievements,
            onStreak = onStreak,
            onClinicalHistory = onClinicalHistory,
            onLogout = onLogout,
            viewModel = viewModel
        )
        "edit" -> EditProfileScreen(viewModel = viewModel, onBack = { subScreen = "main" })
        "notifications" -> NotificationPrefsScreen(viewModel = viewModel, onBack = { subScreen = "main" })
        "settings" -> SettingsScreen(
            onBack = { subScreen = "main" },
            onAppearance = { subScreen = "appearance" },
            onLanguage = { subScreen = "language" }
        )
        "appearance" -> AppearanceScreen(viewModel = viewModel, onBack = { subScreen = "main" })
        "language" -> LanguageScreen(currentLanguage = currentLanguage, onLanguageChange = { viewModel.setLanguage(it) }, onBack = { subScreen = "main" })
        "privacy" -> PrivacyScreen(viewModel = viewModel, onLogout = onLogout, onBack = { subScreen = "main" })
        "streak" -> StreakTrackerScreen(viewModel = viewModel, onBack = { subScreen = "main" })
        "about" -> AboutScreen(viewModel = viewModel, onBack = { subScreen = "main" }, onNavigateToChat = onNavigateToChat)
    }
}
@Composable
fun StressScoreCard(viewModel: MainViewModel) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val themeColor = when(viewModel.stressLevel) {
        "stress_level_low" -> Color(0xFFFFD1DC) // Lite Pink
        "stress_level_moderate" -> Color(0xFFFFEB3B) // Yellow
        "stress_level_high" -> Color(0xFFFF9800) // Orange
        "stress_level_critical" -> Color(0xFFFF4B4B) // Red
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progress
            val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawArc(
                        color = outlineVariantColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = themeColor,
                        startAngle = -90f,
                        sweepAngle = (viewModel.stressScore.toFloat() / 170f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.stressScore.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = stringResource(R.string.stress_score),
                        fontSize = 14.sp,
                        color = subtitleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = getTranslatedStressLevel(viewModel.stressLevel),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = themeColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if(viewModel.stressScore < 40) stringResource(R.string.stress_managed) 
                       else stringResource(R.string.stress_recommendation),
                fontSize = 14.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun OverviewCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    navController: NavHostController,
    onShowAlert: (Int) -> Unit,
    onShowNotifications: () -> Unit,
    onViewRecommendations: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val todayCheckins by viewModel.todayCheckinsCount.collectAsState()
    val todaySleep by viewModel.todaySleepDuration.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Notification Icon at Top Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .clickable { onShowNotifications() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Alerts",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color.Red, CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Profile Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val imageUri by viewModel.profileImageUri.collectAsState()
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        if (imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = primaryColor, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if(Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) stringResource(R.string.morning_greeting) else stringResource(R.string.evening_greeting),
                            style = MaterialTheme.typography.labelSmall,
                            color = subtitleColor
                        )
                        Text(
                            text = viewModel.currentUserName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stress Orb
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShowAlert(viewModel.stressScore) },
                contentAlignment = Alignment.Center
            ) {
                StressOrb(viewModel = viewModel)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Action Button
            Button(
                onClick = { 
                    viewModel.initializeCheckInForm()
                    navController.navigate("dailyCheckin") 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.daily_checkin_btn),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Today's Stats (Mini)
            val currentStreak by viewModel.currentStreak.collectAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CheckCircle,
                    value = todayCheckins.toString(),
                    title = stringResource(R.string.checkins_label)
                )
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Whatshot,
                    value = currentStreak,
                    title = stringResource(R.string.streak_label)
                )
                CompactStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.NightsStay,
                    value = todaySleep,
                    title = stringResource(R.string.sleep_label)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CompactStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AnalyticsScreen(viewModel: MainViewModel, navController: NavHostController, onExport: () -> Unit, onShare: () -> Unit) {
    val tabs = listOf("Trends", "Weekly", "Monthly", "Factors", "History")
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Elegant Sub-tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.insights),
                style = MaterialTheme.typography.headlineMedium,
                color = textColor
            )
            IconButton(
                onClick = onExport,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Export", tint = textColor, modifier = Modifier.size(20.dp))
            }
        }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = primaryColor,
            edgePadding = 24.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = primaryColor,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val tabLabel = when(tab) {
                    "Trends" -> stringResource(R.string.trends_tab)
                    "Weekly" -> stringResource(R.string.weekly_tab)
                    "Monthly" -> stringResource(R.string.monthly_tab)
                    "Factors" -> stringResource(R.string.factors_tab)
                    "History" -> stringResource(R.string.history_tab)
                    else -> tab
                }
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { 
                        Text(
                            text = tabLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (pagerState.currentPage == index) textColor else textColor.copy(alpha = 0.7f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> StressTrendsScreen(viewModel)
                1 -> WeeklyReportScreen(viewModel)
                2 -> MonthlyReportScreen(viewModel)
                3 -> FactorBreakdownScreen(viewModel)
                4 -> HistoryLogScreen(viewModel, navController)
            }
        }
    }
}

@Composable
fun StressTrendsScreen(viewModel: MainViewModel) {
    val history by viewModel.history.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Wave Graph Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.stress_trends), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.daily_avg_stress), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))
                StressWaveGraph(history = history)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val sdfLabel = SimpleDateFormat("EEE", Locale.getDefault())
                    val cal = Calendar.getInstance()
                    var currentDay = cal.get(Calendar.DAY_OF_WEEK)
                    if (currentDay == Calendar.SUNDAY) {
                        currentDay = 8
                    }
                    cal.add(Calendar.DAY_OF_YEAR, -(currentDay - Calendar.MONDAY))
                    val labels = (0..6).map { i ->
                        val c = cal.clone() as Calendar
                        c.add(Calendar.DAY_OF_YEAR, i)
                        sdfLabel.format(c.time)
                    }
                    labels.forEach { day ->
                        Text(day, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        StreakCalendar(history = history)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun StressWaveGraph(history: List<StressRecord>) {
    // Get last 7 days including today
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    val cal = Calendar.getInstance()
    var currentDay = cal.get(Calendar.DAY_OF_WEEK)
    if (currentDay == Calendar.SUNDAY) {
        currentDay = 8
    }
    cal.add(Calendar.DAY_OF_YEAR, -(currentDay - Calendar.MONDAY))
    
    val weekDays = (0..6).map { i ->
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, i)
        sdf.format(c.time)
    }
    
    val dailyScores = history.groupBy { sdf.format(Date(it.timestamp)) }
        .mapValues { entry -> entry.value.map { it.score }.average().toFloat() }
    
    val scores = weekDays.map { dailyScores[it] ?: 0f }
    val maxScore = 100f
    
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 20.dp, bottom = 20.dp)) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val stepPx = if (scores.size > 1) widthPx / (scores.size - 1) else widthPx
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val step = if (scores.size > 1) width / (scores.size - 1) else width
            
            // Draw horizontal grid lines (Y-axis 0, 50, 100)
            listOf(0f, 0.5f, 1f).forEach { fraction ->
                val y = height * fraction
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
            
            val path = androidx.compose.ui.graphics.Path()
            scores.forEachIndexed { index, score ->
                val x = index * step
                val y = height - (score / maxScore * height)
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    val prevX = (index - 1) * step
                    val prevY = height - (scores[index - 1] / maxScore * height)
                    path.cubicTo(
                        prevX + step / 2, prevY,
                        x - step / 2, y,
                        x, y
                    )
                }
            }
            
            drawPath(
                path = path,
                color = SageGreen,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            val fillPath = androidx.compose.ui.graphics.Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(SageGreen.copy(alpha = 0.3f), Color.Transparent)
                )
            )
            
            // Draw points
            scores.forEachIndexed { index, score ->
                val x = index * step
                val y = height - (score / maxScore * height)
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = SageGreen,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        val density = androidx.compose.ui.platform.LocalDensity.current
        scores.forEachIndexed { index, score ->
            val xPx = index * stepPx
            val yPx = heightPx - (score / maxScore * heightPx)
            
            val xDp = with(density) { xPx.toDp() }
            val yDp = with(density) { yPx.toDp() }
            
            Text(
                text = score.toInt().toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.offset(x = xDp - 8.dp, y = yDp - 22.dp)
            )
        }
    }
}

@Composable
fun StressHeatmap(history: List<StressRecord>) {
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Get last 28 days (4 weeks)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = Calendar.getInstance()
    
    val checkinDays = history.map { sdf.format(Date(it.timestamp)) }.toSet()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (0 until 7).forEach { weekIndex -> // Show 7 columns (weeks)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (0 until 5).forEach { dayIndex -> // 5 rows
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -( (6 - weekIndex) * 5 + (4 - dayIndex) ))
                    val dateStr = sdf.format(cal.time)
                    val isChecked = checkinDays.contains(dateStr)
                    
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = if (isChecked) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun AnalyticsOverviewScreen(viewModel: MainViewModel) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val currentStreak by viewModel.currentStreak.collectAsState()
    val trends by viewModel.trends.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.analytics),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(stringResource(R.string.extracted_track_your_stress_pa),
            fontSize = 14.sp,
            color = subtitleColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoSquareCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.TrendingDown,
                iconTint = tealColor,
                title = stringResource(R.string.avg_this_week),
                value = if (trends.isNotEmpty()) "${trends.map { it.score }.average().toInt()}" else "--"
            )
            InfoSquareCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarToday,
                iconTint = tealColor,
                title = stringResource(R.string.streak),
                value = currentStreak
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = stringResource(R.string.weekly_trend), fontWeight = FontWeight.Bold, color = textColor)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = subtitleColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.last_7_days), fontSize = 12.sp, color = subtitleColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (trends.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.extracted_no_data_available), color = subtitleColor)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val displayData = trends.take(7).reversed()
                        displayData.forEachIndexed { index, record ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = record.score.toString(), fontSize = 10.sp, color = textColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height((record.score * 1.0).dp)
                                        .background(
                                            color = when(record.level) {
                                                "stress_level_low" -> tealColor
                                                "stress_level_moderate" -> Color(0xFFFFD700)
                                                else -> Color(0xFFFF4B4B)
                                            },
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "D${index+1}", fontSize = 10.sp, color = subtitleColor)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LegendItem(MaterialTheme.colorScheme.primary, stringResource(R.string.low))
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(Color(0xFFFFD700), stringResource(R.string.moderate))
                    Spacer(modifier = Modifier.width(16.dp))
                    LegendItem(Color(0xFFFF4B4B), stringResource(R.string.high))
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



@Composable
fun WeeklyReportScreen(viewModel: MainViewModel, onShare: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val avgStress by viewModel.avgStressThisWeek.collectAsState()
    val bestDay by viewModel.bestDayThisWeek.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.weekly_report), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = stringResource(R.string.extracted_current_week_summary), fontSize = 14.sp, color = subtitleColor)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.avg_this_week), fontSize = 12.sp, color = subtitleColor)
                Text(text = avgStress.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingDown, null, tint = tealColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.extracted_updated_from_your_la), fontSize = 14.sp, color = tealColor, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = stringResource(R.string.extracted_key_metrics), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        
        Spacer(modifier = Modifier.height(16.dp))

        val history by viewModel.weeklyHistory.collectAsState()
        val checkIns = history.size
        val highestScore = history.maxOfOrNull { it.score } ?: 0
        val lowestScoreWeekly = history.minOfOrNull { it.score } ?: 0

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle, iconTint = tealColor, title = "Check-ins", value = "$checkIns")
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingDown, iconTint = tealColor, title = "Lowest Score", value = "$lowestScoreWeekly")
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingUp, iconTint = Color(0xFFFF4B4B), title = "Highest Score", value = "$highestScore")
        }
    }
}

@Composable
fun ReportMetricCard(modifier: Modifier, icon: ImageVector, title: String, value: String, status: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = status, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun InfoSquareCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SafeModeScreen(onStartBreathing: () -> Unit, onTalkToAI: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showSupportDialog by remember { mutableStateOf(false) }

    // Contact Support Dialog
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = {
                Text(
                    text = "Contact Support",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Need help? Reach us through:", color = Color(0xFF444444))
                    // Email Option
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:venugopalgopireddy500@gmail.com")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "CortiSense Support Request")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "No email app found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Email Support", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                Text("venugopalgopireddy500@gmail.com", fontSize = 11.sp, color = Color(0xFF444444))
                            }
                        }
                    }
                    // Call Option
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:7013995242")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open dialer", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, null, tint = Color(0xFF1565C0), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Call Support", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0))
                                Text("+91 7013995242", fontSize = 11.sp, color = Color(0xFF444444))
                            }
                        }
                    }
                    // WhatsApp Option
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://wa.me/917013995242?text=Hi%2C%20I%20need%20support%20with%20CortiSense")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "WhatsApp not installed", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("WhatsApp Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                Text("+91 7013995242", fontSize = 11.sp, color = Color(0xFF444444))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepIndigo),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.breathe_with_me_title),
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif),
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Pulsating breathing circle
            val infiniteTransition = rememberInfiniteTransition(label = "breathing")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
            
            Button(
                onClick = onStartBreathing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Text(stringResource(R.string.extended_breathing_guide), color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { showSupportDialog = true }) {
                Text(stringResource(R.string.i_need_help), color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun HomeHeader(userName: String, isSafeMode: Boolean = false) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = if (isSafeMode) stringResource(R.string.take_it_slow, userName) else stringResource(R.string.extracted_hello_username, userName),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = if (isSafeMode) stringResource(R.string.we_are_here_to_help) else stringResource(R.string.extracted_how_are_you_feeling_t),
            fontSize = 16.sp,
            color = subtitleColor
        )
    }
}

@Composable
fun StressAlertScreen(score: Int, viewModel: MainViewModel, onBack: () -> Unit, onStartBreathing: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    val stressLevel = when {
        score >= 85 -> "Critical"
        score >= 70 -> "High"
        score >= 40 -> "Moderate"
        else -> "Low"
    }

    val themeColor = when (stressLevel) {
        "Critical" -> Color(0xFFFF4B4B) // Red
        "High" -> Color(0xFFFF9800) // Orange
        "Moderate" -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFFFFD1DC) // Lite Pink
    }

    val alertTitle = when (stressLevel) {
        "Critical" -> stringResource(R.string.critical_stress_alert)
        "High" -> stringResource(R.string.high_stress_alert)
        "Moderate" -> stringResource(R.string.moderate_stress_alert)
        else -> stringResource(R.string.doing_great)
    }

    val alertSubtitle = when (stressLevel) {
        "Critical" -> stringResource(R.string.immediate_action_needed)
        "High" -> stringResource(R.string.action_recommended)
        "Moderate" -> stringResource(R.string.stay_mindful)
        else -> stringResource(R.string.keep_it_up)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }

            // Alert Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (stressLevel == "Low") Icons.Default.AutoAwesome else Icons.Default.Warning,
                            contentDescription = null,
                            tint = themeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = alertTitle, fontWeight = FontWeight.Bold, color = textColor, fontSize = 18.sp)
                        Text(text = alertSubtitle, color = subtitleColor, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            drawArc(
                                color = outlineVariantColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = themeColor,
                                startAngle = -90f,
                                sweepAngle = (score.toFloat() / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = score.toString(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text(text = stringResource(R.string.stress_score), fontSize = 12.sp, color = subtitleColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (stressLevel == "Low") stringResource(R.string.low) else if (stressLevel == "Moderate") stringResource(R.string.high) else stringResource(R.string.stress_level_critical),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Text(
                        text = if (stressLevel == "Low") stringResource(R.string.excellent) else if (stressLevel == "Moderate") stringResource(R.string.alert_level_elevated) else stringResource(R.string.alert_level_alert),
                        fontSize = 14.sp,
                        color = subtitleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recommendation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (stressLevel) {
                                "Critical" -> stringResource(R.string.alert_severe_detected)
                                "High" -> stringResource(R.string.alert_elevated_detected)
                                "Moderate" -> stringResource(R.string.alert_slightly_elevated)
                                else -> stringResource(R.string.excellent)
                            },
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (stressLevel) {
                            "Critical" -> stringResource(R.string.alert_critical_desc)
                            "High" -> stringResource(R.string.alert_high_desc)
                            "Moderate" -> stringResource(R.string.alert_moderate_desc)
                            else -> stringResource(R.string.alert_low_desc)
                        },
                        color = subtitleColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (stressLevel == "Critical") {
                Button(
                    onClick = onStartBreathing,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text(stringResource(R.string.extracted_start_emergency_brea), color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else if (stressLevel == "Low") {
                val currentStreak by viewModel.currentStreak.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.EmojiEvents,
                        iconTint = Color(0xFFFFD700),
                        title = stringResource(R.string.streak),
                        value = "$currentStreak " + stringResource(R.string.streak_days_label)
                    )
                    CompactStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingDown,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.vs_last_week),
                        value = "-12%"
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            RecommendedActionsCard(viewModel = viewModel)
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NotificationsScreen(viewModel: MainViewModel, onBack: () -> Unit, onOpenDetail: () -> Unit) {
    val notifications by viewModel.notifications.collectAsState()
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsAsRead()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { /* Could add clear all here */ }) {
                        Text("History", fontSize = 12.sp, color = subtitleColor)
                    }
                }
            }

            Text(
                text = stringResource(R.string.notifications),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = if (notifications.isEmpty()) stringResource(R.string.notif_all_caught_up) else stringResource(R.string.extracted_stay_updated_on_your),
                fontSize = 14.sp,
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = subtitleColor.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.notif_no_new),
                            style = MaterialTheme.typography.bodyLarge,
                            color = subtitleColor
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notification ->
                        val icon = when (notification.type) {
                            "stress", NotificationHelper.CHANNEL_STRESS -> Icons.Default.Warning
                            "checkin", NotificationHelper.CHANNEL_CHECKIN -> Icons.Default.CheckCircle
                            "achievement" -> Icons.Default.EmojiEvents
                            else -> Icons.Default.Notifications
                        }
                        val iconColor = when (notification.type) {
                            "stress", NotificationHelper.CHANNEL_STRESS -> Color(0xFFFF4B4B)
                            "checkin", NotificationHelper.CHANNEL_CHECKIN -> tealColor
                            "achievement" -> Color(0xFFFFD700)
                            else -> tealColor
                        }

                        NotificationItem(
                            title = notification.title,
                            description = notification.message,
                            time = formatTimestamp(LocalContext.current, notification.timestamp),
                            icon = icon,
                            iconTint = iconColor,
                            isNew = !notification.isRead,
                            onClick = {
                                if (notification.type == "stress") onOpenDetail()
                            },
                            onDelete = {
                                viewModel.deleteNotification(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

fun formatTimestamp(context: Context, timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> context.getString(R.string.time_just_now)
        diff < 3600000 -> context.getString(R.string.time_m_ago, diff / 60000)
        diff < 86400000 -> context.getString(R.string.time_h_ago, diff / 3600000)
        else -> SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}

@Composable
fun NotificationItem(
    title: String,
    description: String,
    time: String,
    icon: ImageVector,
    iconTint: Color,
    isNew: Boolean = false,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isNew) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp
                    )
                    if (isNew) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Delete", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



@Composable
fun AnalyzingDataScreen(viewModel: MainViewModel, onFinished: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    
    LaunchedEffect(Unit) {
        delay(3000)
        onFinished()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = tealColor, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = stringResource(R.string.analyzing), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
            Text(text = stringResource(R.string.analyzing_data_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = tealColor, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = stringResource(R.string.processing_markers), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalysisStep(stringResource(R.string.analyzing_heart_rate))
                AnalysisStep(stringResource(R.string.evaluating_sleep))
                AnalysisStep(stringResource(R.string.computing_stress))
                AnalysisStep(stringResource(R.string.generating_insights))
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(8.dp), color = tealColor, trackColor = MaterialTheme.colorScheme.surface)
        }
    }
}

@Composable
fun AnalysisStep(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
fun WeeklyReportScreen(viewModel: MainViewModel) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val history by viewModel.weeklyHistory.collectAsState()
    
    val avgWeekly = if (history.isEmpty()) 0 else history.map { it.score }.average().toInt()
    val checkIns = history.size
    val lowDays = history.count { it.level == "Low Stress" || it.level == "stress_level_low" }
    val moderateDays = history.count { it.level == "Moderate Stress" || it.level == "stress_level_moderate" }
    val highDays = history.count { it.level == "High Stress" || it.level == "stress_level_high" || it.level == "Critical Stress" || it.level == "stress_level_critical" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.weekly_report), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = stringResource(R.string.last_7_days), fontSize = 14.sp, color = subtitleColor)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.weekly_average), fontSize = 12.sp, color = subtitleColor, fontWeight = FontWeight.Bold)
                Text(text = avgWeekly.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(text = stringResource(R.string.based_on_checkins, checkIns), fontSize = 12.sp, color = tealColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val highestScore = history.maxOfOrNull { it.score } ?: 0
        val lowestScoreWeekly = history.minOfOrNull { it.score } ?: 0
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle, iconTint = tealColor, title = "Check-ins", value = "$checkIns")
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingUp, iconTint = Color(0xFFFF4B4B), title = "Highest Score", value = "$highestScore")
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingDown, iconTint = tealColor, title = "Lowest Score", value = "$lowestScoreWeekly")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = stringResource(R.string.distribution), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                StressDistItem(stringResource(R.string.stress_level_low), lowDays, checkIns, tealColor)
                Spacer(modifier = Modifier.height(16.dp))
                StressDistItem(stringResource(R.string.stress_level_moderate), moderateDays, checkIns, Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(16.dp))
                StressDistItem(stringResource(R.string.report_high_critical), highDays, checkIns, Color(0xFFFF4B4B))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun MonthlyReportScreen(viewModel: MainViewModel) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val history by viewModel.monthlyHistory.collectAsState()
    
    val avgMonthly = if (history.isEmpty()) 0 else history.map { it.score }.average().toInt()
    val checkIns = history.size
    val lowDays = history.count { it.level == "Low Stress" || it.level == "stress_level_low" }
    val moderateDays = history.count { it.level == "Moderate Stress" || it.level == "stress_level_moderate" }
    val highDays = history.count { it.level == "High Stress" || it.level == "stress_level_high" || it.level == "Critical Stress" || it.level == "stress_level_critical" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.extracted_monthly_report), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = stringResource(R.string.extracted_overview_of_all_your), fontSize = 14.sp, color = subtitleColor)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.extracted_average_stress_score), fontSize = 12.sp, color = subtitleColor)
                Text(text = avgMonthly.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textColor)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingDown, null, tint = tealColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.extracted_calculated_from_chec, checkIns), fontSize = 14.sp, color = tealColor, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val highestScore = history.maxOfOrNull { it.score } ?: 0
        val lowestScoreMonthly = history.minOfOrNull { it.score } ?: 0
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle, iconTint = tealColor, title = "Check-ins", value = "$checkIns")
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingDown, iconTint = tealColor, title = "Lowest Score", value = "$lowestScoreMonthly")
            InfoSquareCard(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.TrendingUp, iconTint = Color(0xFFFF4B4B), title = "Highest Score", value = "$highestScore")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = stringResource(R.string.extracted_stress_distribution), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                StressDistItem(stringResource(R.string.low), lowDays, checkIns, tealColor)
                Spacer(modifier = Modifier.height(16.dp))
                StressDistItem(stringResource(R.string.moderate), moderateDays, checkIns, Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(16.dp))
                StressDistItem(stringResource(R.string.high), highDays, checkIns, Color(0xFFFF4B4B))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = stringResource(R.string.extracted_monthly_achievement), fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(12.dp))
                val lowPercent = if (checkIns > 0) (lowDays * 100 / checkIns) else 0
                Text(stringResource(R.string.extracted_lowpercent_of_your_d, lowPercent),
                    fontSize = 13.sp,
                    color = subtitleColor,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun StressDistItem(label: String, days: Int, total: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(50)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            Text(text = "$days times", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (total > 0) days.toFloat() / total.toFloat() else 0f)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun FactorBreakdownScreen(viewModel: MainViewModel) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val history by viewModel.history.collectAsState()

    val allReasons = history.flatMap { it.reasons }
    val counts = allReasons.groupingBy { it }.eachCount()
    val sorted = counts.entries.sortedByDescending { it.value }
    
    val lastCheckin = history.maxByOrNull { it.timestamp }
    val lastCheckinReasons = lastCheckin?.reasons ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.extracted_factor_breakdown), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = stringResource(R.string.extracted_what_influences_your), fontSize = 14.sp, color = subtitleColor)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QueryStats, null, tint = tealColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.extracted_contribution_analysi), fontWeight = FontWeight.Bold, color = textColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.extracted_each_factor_s_impact), fontSize = 12.sp, color = subtitleColor)
                
                Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (sorted.isEmpty()) {
                Text(
                    text = "No check-ins yet to analyze.",
                    fontSize = 14.sp,
                    color = subtitleColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                sorted.forEach { (reason, count) ->
                val percentage = (count * 100 / allReasons.size)
                val icon = when {
                    reason.lowercase().contains("sleep") -> Icons.Default.NightsStay
                    reason.lowercase().contains("workload") -> Icons.Default.Work
                    reason.lowercase().contains("anxiety") -> Icons.Default.Psychology
                    reason.lowercase().contains("caffeine") -> Icons.Default.Coffee
                    reason.lowercase().contains("screen") -> Icons.Default.PhoneAndroid
                    else -> Icons.Default.Adjust
                }
                FactorItem(
                    icon = icon, 
                    title = getTranslatedReason(reason), 
                    impact = if (percentage > 30) stringResource(R.string.factor_high_impact) else if (percentage > 15) stringResource(R.string.factor_medium_impact) else stringResource(R.string.factor_low_impact), 
                    percentage = percentage, 
                    color = if (percentage > 30) Color(0xFFFF4B4B) else if (percentage > 15) Color(0xFFFFD700) else tealColor
                )
            }
        }
        }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.extracted_ai_recommendation),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (lastCheckinReasons.isEmpty()) {
                    Text(
                        text = "Add more check-ins to see personalized recommendations.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                } else {
                    lastCheckinReasons.take(2).forEach { reason ->
                        Text(
                            text = "• ${getTranslatedReason(reason)}",
                            fontWeight = FontWeight.Bold,
                            color = tealColor
                        )
                        Text(
                            text = getFactorRecommendation(reason),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FactorItem(icon: ImageVector, title: String, impact: String, percentage: Int, color: Color) {
    Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = impact, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = "$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(50))
                )
            }
        }
    }
}

fun getFactorRecommendation(factor: String): String {
    return when(factor.lowercase()) {
        "workload", "work", "studies" -> "Break large tasks into smaller steps. Prioritize urgent work and take regular 5-minute breaks."
        "sleep deprivation", "poor sleep", "sleep" -> "Try to maintain a consistent sleep schedule. Avoid screens 1 hour before bedtime."
        "anxiety", "overthinking" -> "Practice deep breathing (4-7-8 method). Write down your thoughts to clear your mind."
        "caffeine", "diet" -> "Limit caffeine intake after 2 PM. Stay hydrated with water throughout the day."
        "screen time", "device usage" -> "Follow the 20-20-20 rule: Every 20 minutes, look at something 20 feet away for 20 seconds."
        "health", "illness" -> "Rest is crucial. Consult a doctor if symptoms persist, and listen to your body's limits."
        "family", "relationship" -> "Communicate openly with loved ones. Setting healthy boundaries can significantly reduce stress."
        "finance", "money" -> "Create a clear budget and track expenses. Focus on small, manageable financial goals."
        else -> "Take a moment to step back and breathe. Regular mindfulness and short breaks can help manage this stressor."
    }
}

@Composable
fun HistoryLogScreen(viewModel: MainViewModel, navController: NavHostController) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val history by viewModel.history.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.history_log), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = stringResource(R.string.extracted_your_past_daily_entr), fontSize = 14.sp, color = subtitleColor)

        Spacer(modifier = Modifier.height(24.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.extracted_no_entries_found), color = subtitleColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                history.forEach { record ->
                    HistoryItem(
                        date = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(record.timestamp)),
                        score = record.score,
                        level = getTranslatedStressLevel(record.level),
                        color = when(record.level) {
                            "stress_level_low" -> MaterialTheme.colorScheme.primary
                            "stress_level_moderate" -> Color(0xFFFFD700)
                            else -> Color(0xFFFF4B4B)
                        },
                        onClick = {
                            viewModel.selectRecord(record)
                            navController.navigate("history_detail")
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun HistoryItem(date: String, score: Int, level: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = score.toString(), fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = level, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text(text = date, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun HistoryDetailScreen(viewModel: MainViewModel, onBack: () -> Unit, onStartBreathing: () -> Unit) {
    val record by viewModel.selectedRecord.collectAsState()
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    if (record == null) {
        onBack()
        return
    }

    val r = record!!

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Text(text = stringResource(R.string.clinical_checkin_report), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(
                text = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(r.timestamp)),
                fontSize = 14.sp,
                color = subtitleColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scoreColor = when(r.level) {
                        "stress_level_low" -> tealColor
                        "stress_level_moderate" -> Color(0xFFFFD700)
                        else -> Color(0xFFFF4B4B)
                    }

                    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            drawArc(outlineVariant, 0f, 360f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                            drawArc(scoreColor, -90f, (r.score.toFloat() / 100f) * 360f, false, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = r.score.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text(text = stringResource(R.string.clinical_stress_index), fontSize = 12.sp, color = subtitleColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = getTranslatedStressLevel(r.level), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = scoreColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reasons
            Text(text = stringResource(R.string.clinical_identified_factors), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            r.reasons.forEach { reason ->
                Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(tealColor, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = getTranslatedReason(reason), fontSize = 14.sp, color = textColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Analysis Details
            Text(text = stringResource(R.string.clinical_analysis_details), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalysisRow(stringResource(R.string.clinical_cognitive_load), r.cognitiveScore, 12)
                    AnalysisRow(stringResource(R.string.clinical_emotional_state), r.emotionalScore, 12)
                    AnalysisRow(stringResource(R.string.clinical_physical_tension), r.physicalScore, 12)
                }
            }

            if (r.ventText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = stringResource(R.string.clinical_personal_reflection), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = r.ventText,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic Recommendations
            Text(text = stringResource(R.string.clinical_dynamic_reco), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            
            when (r.level) {
                "stress_level_low", "Low Stress" -> {
                    RecoItem(Icons.Default.EmojiEvents, stringResource(R.string.reco_l1_t1), stringResource(R.string.reco_l1_b1))
                    RecoItem(Icons.Default.CheckCircle, stringResource(R.string.reco_low_t2), stringResource(R.string.reco_low_b2))
                }
                "stress_level_moderate", "Moderate Stress" -> {
                    RecoItem(Icons.Default.Psychology, stringResource(R.string.reco_l2_t1), stringResource(R.string.reco_l2_b1))
                    RecoItem(Icons.Default.NotificationsOff, stringResource(R.string.reco_l2_t3), stringResource(R.string.reco_l2_b3))
                }
                "stress_level_high", "High Stress" -> {
                    RecoItem(Icons.Default.Air, stringResource(R.string.reco_l3_t1), stringResource(R.string.reco_l3_b1))
                    RecoItem(Icons.Default.FilterList, stringResource(R.string.reco_l3_t3), stringResource(R.string.reco_l3_b3))
                }
                else -> {
                    RecoItem(Icons.Default.Warning, stringResource(R.string.crisis_step1), stringResource(R.string.safety_valve_desc))
                    Button(
                        onClick = onStartBreathing,
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B))
                    ) {
                        Text(stringResource(R.string.clinical_start_sos), fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun AnalysisRow(label: String, score: Int, max: Int) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$score / $max", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = score.toFloat() / max.toFloat(),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun RecoItem(icon: ImageVector, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun SleepAnalyticsScreen(viewModel: MainViewModel) {
    val isTracking by viewModel.isSleepTracking.collectAsState()
    val todaySleep by viewModel.todaySleepDuration.collectAsState()
    val sleepHistory by viewModel.sleepHistory.collectAsState()
    val currentMinutes = viewModel.currentSessionMinutes
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    val infiniteTransition = rememberInfiniteTransition(label = "sleep_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.sleep_sanctuary),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
        Text(
            text = stringResource(R.string.sleep_sanctuary_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Control Card with Glassmorphism-like style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isTracking) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTracking) Icons.Default.NightsStay else Icons.Default.Brightness3,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (isTracking) stringResource(R.string.resting_mode) else stringResource(R.string.sleep_tracking_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                if (isTracking) {
                    Text(
                        text = stringResource(R.string.recorded_time, currentMinutes / 60, currentMinutes % 60),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.last_night_stat, todaySleep),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Button(
                    onClick = { if (isTracking) viewModel.pauseSleepTracking() else viewModel.startSleepTracking() },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking) Color(0xFFFF4B4B) else primaryColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isTracking) stringResource(R.string.stop_session) else stringResource(R.string.start_sleeping),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Quick Stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val avgSleep = if (sleepHistory.isEmpty()) "0h" else {
                val total = sleepHistory.sumOf { it.durationMinutes }
                val avg = total / sleepHistory.size
                "${avg / 60}h ${avg % 60}m"
            }
            
            SleepStatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.avg_sleep),
                value = avgSleep,
                icon = Icons.Default.TrendingUp,
                color = primaryColor
            )
            SleepStatCard(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.excellent), // Using "excellent" as proxy for quality
                value = stringResource(R.string.high),
                icon = Icons.Default.Star,
                color = Color(0xFFFFD700)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = stringResource(R.string.stress_trends), // Using stress_trends as proxy for trends
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SleepCalendar(sleepHistory = sleepHistory)
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SleepStatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SleepCalendar(sleepHistory: List<SleepRecord>) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    // Group sleep by date
    val sleepByDate = sleepHistory.groupBy { it.date }.mapValues { entry ->
        entry.value.sumOf { it.durationMinutes }
    }
    
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed, Sun=0
    
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Days of Week Header
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Grid
            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7
            
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1
                        
                        if (day in 1..daysInMonth) {
                            val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day)
                            val minutes = sleepByDate[dateStr] ?: 0
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .background(
                                        if (minutes > 0) MaterialTheme.colorScheme.primary.copy(alpha = (minutes / 600f).coerceIn(0.1f, 0.8f))
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (minutes > 400) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (minutes > 0) {
                                        Text(
                                            text = "${minutes / 60}h",
                                            fontSize = 8.sp,
                                            color = if (minutes > 400) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StressCalendarScreen(viewModel: MainViewModel) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val history by viewModel.history.collectAsState()
    
    val calendar = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    val monthName = remember(currentMonth, currentYear) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, currentMonth)
        cal.set(Calendar.YEAR, currentYear)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(text = stringResource(R.string.stress_calendar), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        Text(text = "Track your daily stress levels with our interactive heat map.", fontSize = 14.sp, color = subtitleColor)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = subtitleColor)
                    }
                    Text(text = monthName, fontWeight = FontWeight.Bold, color = textColor)
                    IconButton(onClick = {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = subtitleColor)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(text = day, fontSize = 10.sp, color = subtitleColor, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dailyStressMap = history.groupBy { sdf.format(it.timestamp) }
                    .mapValues { (_, records) ->
                        records.maxByOrNull { it.score }?.level ?: "stress_level_low"
                    }
                
                val cal = Calendar.getInstance()
                cal.set(currentYear, currentMonth, 1)
                val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                Column {
                    var dayCounter = 1
                    for (row in 0..5) {
                        if (dayCounter > daysInMonth) break
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            for (col in 0..6) {
                                if ((row == 0 && col < firstDayOfWeek) || dayCounter > daysInMonth) {
                                    Spacer(modifier = Modifier.size(32.dp))
                                } else {
                                    val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, dayCounter)
                                    val stressLevel = dailyStressMap[dateStr]
                                    
                                    val color = when (stressLevel) {
                                        "Low Stress", "stress_level_low" -> Color(0xFFFFD1DC)
                                        "Moderate Stress", "stress_level_moderate" -> Color(0xFFFFEB3B)
                                        "High Stress", "stress_level_high" -> Color(0xFFFF9800)
                                        "Critical Stress", "stress_level_critical" -> Color(0xFFFF4B4B)
                                        else -> Color.White
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(color, RoundedCornerShape(8.dp))
                                            .border(1.dp, if (color == Color.White) MaterialTheme.colorScheme.outlineVariant else Color.Transparent, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayCounter.toString(), 
                                            fontSize = 12.sp, 
                                            color = if (color == Color.White) textColor else Color.DarkGray, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    dayCounter++
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = stringResource(R.string.stress_level_legend), fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(16.dp))
                CalendarLegendItem(Color(0xFFFFD1DC), stringResource(R.string.low), stringResource(R.string.stress_well_managed))
                CalendarLegendItem(Color(0xFFFFEB3B), stringResource(R.string.moderate), stringResource(R.string.stress_elevated_detected))
                CalendarLegendItem(Color(0xFFFF9800), stringResource(R.string.high), stringResource(R.string.stress_high_break))
                CalendarLegendItem(Color(0xFFFF4B4B), stringResource(R.string.stress_level_critical), stringResource(R.string.stress_urgent_relax))
                CalendarLegendItem(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), stringResource(R.string.not_tracked), stringResource(R.string.not_tracked_desc))
            }
        }
    }
}

@Composable
fun CalendarLegendItem(color: Color, label: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun NutritionTipsScreen(onBack: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Restaurant, null, tint = tealColor, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = stringResource(R.string.extracted_nutrition_tips), fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = stringResource(R.string.extracted_eat_to_reduce_stress), fontSize = 12.sp, color = subtitleColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = stringResource(R.string.extracted_food_stress_connecti), fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.extracted_what_you_eat_directl), fontSize = 14.sp, color = subtitleColor, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            val tips = listOf(
                stringResource(R.string.nutri_tip_1),
                stringResource(R.string.nutri_tip_2),
                stringResource(R.string.nutri_tip_3),
                stringResource(R.string.nutri_tip_4)
            )
            
            Text(text = stringResource(R.string.extracted_eat_stress_reducing_), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    tips.forEach { tip ->
                        var isChecked by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { isChecked = !isChecked },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isChecked) tealColor else subtitleColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = tip, color = if (isChecked) textColor else subtitleColor, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.extracted_limit_caffeine_intak), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BulletItem(stringResource(R.string.nutri_caffeine_1))
                    BulletItem(stringResource(R.string.nutri_caffeine_2))
                    BulletItem(stringResource(R.string.nutri_caffeine_3))
                    BulletItem(stringResource(R.string.nutri_caffeine_4))
                }
            }
        }
    }
}

@Composable
fun BulletItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MindfulnessScreen(onBack: () -> Unit, onComplete: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(180) } // 3 minutes

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            onComplete()
        }
    }
    
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    var duration by remember { mutableStateOf(10f) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = stringResource(R.string.extracted_mindfulness), fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = stringResource(R.string.extracted_guided_meditation), fontSize = 12.sp, color = subtitleColor)
                    }
                }
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, null)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = tealColor, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = stringResource(R.string.extracted_start_your_session), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = stringResource(R.string.extracted_find_a_quiet_place_a), fontSize = 14.sp, color = subtitleColor, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stringResource(R.string.extracted_duration), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = stringResource(R.string.extracted_duration_toint_min, duration.toInt()), fontWeight = FontWeight.Bold, color = tealColor)
                    }
                    Slider(
                        value = duration,
                        onValueChange = { duration = it },
                        valueRange = 5f..30f,
                        colors = SliderDefaults.colors(thumbColor = tealColor, activeTrackColor = tealColor)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.surface)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.extracted_begin_meditation), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_guided_sessions), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VolumeUp, null, tint = tealColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.extracted_morning_calm), fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = stringResource(R.string.extracted_5_min_guided), fontSize = 12.sp, color = subtitleColor)
                    }
                    Icon(Icons.Default.PlayArrow, null, tint = subtitleColor)
                }
            }
        }
    }
}

@Composable
fun ExerciseCompletionScreen(onViewProgress: () -> Unit, onBackHome: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = tealColor, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = stringResource(R.string.extracted_great_job_today), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center)
            Text(stringResource(R.string.extracted_you_ve_completed_you),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CompletionMetricCard(modifier = Modifier.weight(1f), label = "5", subLabel = "Rounds")
                CompletionMetricCard(modifier = Modifier.weight(1f), label = "3:45", subLabel = "Duration")
                CompletionMetricCard(modifier = Modifier.weight(1f), label = "-12%", subLabel = "Stress", color = tealColor)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onViewProgress,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Text(text = stringResource(R.string.extracted_view_my_progress), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Text(text = stringResource(R.string.extracted_back_to_home), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.extracted_10_points_earned), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CompletionMetricCard(modifier: Modifier, label: String, subLabel: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@Composable
fun ProfileMainScreen(
    onEditProfile: () -> Unit,
    onNotifications: () -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onAchievements: () -> Unit,
    onStreak: () -> Unit,
    onClinicalHistory: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MainViewModel
) {
    // Add missing parameters or handle them inside CortiSenseScreen/ProfileFlow
    ProfileMainScreenContent(
        onEditProfile = onEditProfile,
        onNotifications = onNotifications,
        onSettings = onSettings,
        onAppearance = {}, // Will be updated in subScreen logic
        onLanguage = {},   // Will be updated in subScreen logic
        onPrivacy = onPrivacy,
        onAbout = onAbout,
        onAchievements = onAchievements,
        onStreak = onStreak,
        onClinicalHistory = onClinicalHistory,
        onLogout = onLogout,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileMainScreenContent(
    onEditProfile: () -> Unit,
    onNotifications: () -> Unit,
    onSettings: () -> Unit,
    onAppearance: () -> Unit,
    onLanguage: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onAchievements: () -> Unit,
    onStreak: () -> Unit,
    onClinicalHistory: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MainViewModel
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val history by viewModel.history.collectAsState()
    val imageUri by viewModel.profileImageUri.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val currentUserName by viewModel.userName.collectAsState()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }
    
    var showImagePreview by remember { mutableStateOf(false) }

    if (showImagePreview) {
        Dialog(onDismissRequest = { showImagePreview = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showImagePreview = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black,
                    tonalElevation = 8.dp
                ) {
                    if (imageUri.isNotEmpty()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Full Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.profile), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(tealColor, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showImagePreview = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = viewModel.currentUserName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(text = viewModel.currentUserEmail, fontSize = 12.sp, color = subtitleColor)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Stats Row
        val todayCheckins by viewModel.todayCheckinsCount.collectAsState()
        val displayStreak = currentStreak
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onStreak() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Whatshot, null, tint = if (todayCheckins > 0) Color(0xFFFFA500) else subtitleColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(
                        targetState = displayStreak,
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn())
                                .with(slideOutVertically { -it } + fadeOut())
                        }
                    ) { targetStreak ->
                        Text(text = targetStreak, fontWeight = FontWeight.ExtraBold, color = textColor, fontSize = 18.sp)
                    }
                    Text(text = " Days Streak", fontWeight = FontWeight.ExtraBold, color = textColor, fontSize = 18.sp)
                }
                Text(text = if (todayCheckins > 0) "Streak Active!" else "Check in today to activate streak", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (todayCheckins > 0) tealColor else subtitleColor)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = subtitleColor)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Check-in History / Clinical History ---
        val clinicalHistory by viewModel.clinicalHistory.collectAsState(initial = emptyList())
        
        ProfileMenuItem(
            icon = Icons.Default.History,
            title = "Check-in & Clinical History (${clinicalHistory.size})",
            onClick = onClinicalHistory
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.extracted_account), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuItem(Icons.Default.Edit, stringResource(R.string.menu_edit_profile), onClick = onEditProfile)
        ProfileMenuItem(Icons.Default.Palette, stringResource(R.string.menu_appearance), onClick = onAppearance)
        ProfileMenuItem(Icons.Default.Language, stringResource(R.string.menu_language), onClick = onLanguage)
        ProfileMenuItem(Icons.Default.Notifications, stringResource(R.string.menu_notifications), onClick = onNotifications)
        ProfileMenuItem(Icons.Default.Lock, stringResource(R.string.menu_privacy), onClick = onPrivacy)
        ProfileMenuItem(Icons.Default.Info, stringResource(R.string.menu_about), onClick = onAbout)

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.logout), color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecommendedActionsCard(viewModel: MainViewModel) {
    val completedTasks by viewModel.completedTasks.collectAsState()
    val stressScore = viewModel.stressScore
    
    val tasks = if (stressScore > 70) {
        listOf(
            Triple("task_breath", "5-Minute Deep Breathing", 10),
            Triple("task_meditate", "10-Minute Guided Meditation", 15),
            Triple("task_journal", "Write down your thoughts", 5),
            Triple("task_music", "Listen to calm music", 10),
            Triple("task_stretch", "Do some light stretching", 10)
        )
    } else if (stressScore > 40) {
        listOf(
            Triple("task_walk", "Take a 15-minute short walk", 15),
            Triple("task_water", "Drink a glass of water", 5),
            Triple("task_breath", "5-Minute Deep Breathing", 10),
            Triple("task_read", "Read a book for 10 minutes", 10),
            Triple("task_music", "Listen to calm music", 10)
        )
    } else {
        listOf(
            Triple("task_water", "Drink a glass of water", 5),
            Triple("task_stretch", "Do some light stretching", 10),
            Triple("task_hobby", "Spend time on a hobby", 15),
            Triple("task_friends", "Chat with a friend", 10),
            Triple("task_walk", "Take a 10-minute short walk", 15)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Recommended Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Tasks based on your current stress score ($stressScore)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            val remainingTasks = tasks.filter { !completedTasks.contains(it.first) }

            if (remainingTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All tasks completed!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("Come back later for more.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                tasks.forEach { (id, title, coins) ->
                    val isCompleted = completedTasks.contains(id)
                    AnimatedVisibility(
                        visible = !isCompleted,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Spa, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                            }
                            
                            Button(
                                onClick = { viewModel.completeTask(id, coins) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Done", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatCard(modifier: Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun CheckInDataItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFFBDC3C7))
        }
    }
}


@Composable
fun EditProfileScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val initialName by viewModel.userName.collectAsState()
    val initialEmail by viewModel.userEmail.collectAsState()
    val initialAge by viewModel.userAge.collectAsState()
    val initialGender by viewModel.userGender.collectAsState()
    val initialGoal by viewModel.userGoal.collectAsState()
    val initialImageUri by viewModel.profileImageUri.collectAsState()

    var name by remember(initialName) { mutableStateOf(initialName) }
    val email = initialEmail
    var age by remember(initialAge) { mutableStateOf(initialAge) }
    var gender by remember(initialGender) { mutableStateOf(initialGender) }
    var goal by remember(initialGoal) { mutableStateOf(initialGoal) }
    var imageUri by remember(initialImageUri) { mutableStateOf(initialImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri.toString() }
    )

    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Text(text = stringResource(R.string.edit_profile), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(100.dp).background(tealColor, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(50.dp))
                        }
                    }
                    Box(
                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
                TextButton(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(stringResource(R.string.extracted_change_photo), color = tealColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CustomTextField(label = stringResource(R.string.full_name), value = name, onValueChange = { name = it }, placeholder = stringResource(R.string.fullname_placeholder))
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = stringResource(R.string.email_address), value = email, onValueChange = {}, placeholder = stringResource(R.string.email_placeholder))
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = stringResource(R.string.age), value = age, onValueChange = { age = it }, placeholder = stringResource(R.string.enter_age))
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = stringResource(R.string.gender), value = gender, onValueChange = { gender = it }, placeholder = stringResource(R.string.select_gender))
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = stringResource(R.string.profile_goal_label), value = goal, onValueChange = { goal = it }, placeholder = stringResource(R.string.profile_goal_placeholder))

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(name, age, gender, goal, imageUri)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Text(stringResource(R.string.extracted_save_changes), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CoinDisplay(coins: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = coins,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "🪙",
            fontSize = 18.sp,
            modifier = Modifier.graphicsLayer(rotationZ = rotation)
        )
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onAppearance: () -> Unit, onLanguage: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Column {
                    Text(text = stringResource(R.string.settings), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = stringResource(R.string.extracted_customize_your_exper), fontSize = 12.sp, color = subtitleColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSection(title = stringResource(R.string.section_appearance)) {
                ProfileMenuItem(Icons.Default.Palette, stringResource(R.string.theme_title), onClick = onAppearance)
                SettingToggleItem(Icons.Default.DarkMode, stringResource(R.string.setting_dark_mode), stringResource(R.string.setting_dark_mode_desc))
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = stringResource(R.string.section_notifications)) {
                SettingToggleItem(Icons.Default.NotificationsActive, stringResource(R.string.setting_push_notifications), stringResource(R.string.setting_push_notifications_desc))
                SettingToggleItem(Icons.Default.CalendarToday, stringResource(R.string.setting_daily_reminders), stringResource(R.string.setting_daily_reminders_desc))
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = stringResource(R.string.section_general)) {
                ProfileMenuItem(Icons.Default.Language, stringResource(R.string.language_title), onClick = onLanguage)
                ProfileMenuItem(Icons.Default.Help, stringResource(R.string.setting_help_support), onClick = {})
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(12.dp))
    Column(content = content)
}

@Composable
fun SettingToggleItem(icon: ImageVector, title: String, subtitle: String) {
    var checked by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun NotificationPrefsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Text(text = stringResource(R.string.notifications), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Text(stringResource(R.string.extracted_notifications_help_y),
                    modifier = Modifier.padding(16.dp),
                    fontSize = 13.sp,
                    color = subtitleColor,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val notifCheckin by viewModel.notifCheckin.collectAsState()
            val notifStressAlerts by viewModel.notifStressAlerts.collectAsState()
            val notifRecommendations by viewModel.notifRecommendations.collectAsState()
            val notifAchievements by viewModel.notifAchievements.collectAsState()

            NotificationPrefItem(Icons.Default.Schedule, stringResource(R.string.notif_pref_checkin), stringResource(R.string.notif_pref_checkin_desc), notifCheckin) {
                viewModel.updateNotificationSetting("checkin", it)
            }
            NotificationPrefItem(Icons.Default.AutoGraph, stringResource(R.string.notif_pref_stress), stringResource(R.string.notif_pref_stress_desc), notifStressAlerts) {
                viewModel.updateNotificationSetting("stress", it)
            }
            NotificationPrefItem(Icons.Default.Lightbulb, stringResource(R.string.notif_pref_rec), stringResource(R.string.notif_pref_rec_desc), notifRecommendations) {
                viewModel.updateNotificationSetting("rec", it)
            }
            NotificationPrefItem(Icons.Default.EmojiEvents, stringResource(R.string.notif_pref_ach), stringResource(R.string.notif_pref_ach_desc), notifAchievements) {
                viewModel.updateNotificationSetting("ach", it)
            }
        }
    }
}

@Composable
fun NotificationPrefItem(icon: ImageVector, title: String, subtitle: String, initialValue: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var checked by remember { mutableStateOf(initialValue) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = { 
                    checked = it
                    onCheckedChange(it)
                },
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StreakTrackerScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val currentStreak by viewModel.currentStreak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    val todayCheckins by viewModel.todayCheckinsCount.collectAsState()
    val history by viewModel.history.collectAsState()
    
    // Strict logic: Show 0 until the user performs today's check-in
    val displayStreak = currentStreak
    
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                }
                Text(stringResource(R.string.streak_tracker_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Current Streak Display
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Whatshot,
                    null,
                    tint = if (todayCheckins > 0) Color(0xFFFFA500) else subtitleColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedContent(
                    targetState = displayStreak,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn())
                            .with(slideOutVertically { -it } + fadeOut())
                    }
                ) { targetStreak ->
                    Text(
                        text = targetStreak,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }
                Text(
                    text = " " + stringResource(R.string.streak_days_label),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }
            Text(
                text = if (todayCheckins > 0) stringResource(R.string.streak_active) else stringResource(R.string.streak_inactive),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (todayCheckins > 0) primaryColor else subtitleColor
            )

            Spacer(modifier = Modifier.height(48.dp))
            
            // Metrics Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StreakMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.streak_longest),
                    value = "$longestStreak"
                )
                StreakMetricCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.streak_today_checkins),
                    value = "$todayCheckins"
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Streak Calendar
            Text(
                text = stringResource(R.string.streak_calendar_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            StreakCalendar(history = history)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = stringResource(R.string.streak_motivation_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = stringResource(R.string.streak_motivation_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun StreakCalendar(history: List<StressRecord>) {
    val today = Calendar.getInstance()
    val currentDay = today.get(Calendar.DAY_OF_MONTH)
    val currentMonth = today.get(Calendar.MONTH)
    val currentYear = today.get(Calendar.YEAR)

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val checkinDates = history.map { sdf.format(Date(it.timestamp)) }.toSet()

    val calForMonth = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
    val daysInMonth = calForMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calForMonth.get(Calendar.DAY_OF_WEEK) - 1
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calForMonth.time)

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Month header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor
                )
                // Legend
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(primaryColor, CircleShape))
                    Text(stringResource(R.string.streak_visited), style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                    Box(modifier = Modifier.size(10.dp).background(outlineVariant, CircleShape))
                    Text(stringResource(R.string.streak_missed), style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val totalCells = daysInMonth + firstDayOfWeek
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1

                        if (day in 1..daysInMonth) {
                            val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day)
                            val isCheckedIn = checkinDates.contains(dateStr)
                            val isToday = day == currentDay
                            val isFuture = day > currentDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .then(
                                        if (isToday)
                                            Modifier.border(2.dp, primaryColor, RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .background(
                                        when {
                                            isCheckedIn -> primaryColor
                                            isFuture -> Color.Transparent
                                            else -> outlineVariant.copy(alpha = 0.25f)
                                        },
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCheckedIn) {
                                    // Show checkmark icon for visited days
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = day.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = when {
                                            isToday -> primaryColor
                                            isFuture -> onSurfaceVariant.copy(alpha = 0.3f)
                                            else -> onSurface.copy(alpha = 0.5f)
                                        }
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary bar at the bottom
            val checkedThisMonth = (1..daysInMonth).count { day ->
                val dateStr = String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day)
                checkinDates.contains(dateStr)
            }
            val passedDays = currentDay
            val pct = if (passedDays > 0) checkedThisMonth.toFloat() / passedDays.toFloat() else 0f

            HorizontalDivider(color = outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.streak_summary, checkedThisMonth, passedDays),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurface
                    )
                    Text(
                        text = stringResource(R.string.streak_summary_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(primaryColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun StreakMetricCard(modifier: Modifier, title: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RecommendationsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onStartBreathing: () -> Unit,
    onViewSleepGuide: () -> Unit,
    onViewActivityPlan: () -> Unit,
    onViewNutritionTips: () -> Unit,
    onStartMindfulness: () -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val stressLevel = viewModel.stressLevel

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(text = stringResource(R.string.extracted_recommendations), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = stringResource(R.string.extracted_personalized_activit), fontSize = 14.sp, color = subtitleColor)

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = stringResource(R.string.extracted_today_s_progress), fontWeight = FontWeight.Bold, color = textColor)
                        val progress = when(stressLevel) {
                            "stress_level_low" -> "4 of 5 Completed"
                            "stress_level_moderate" -> "2 of 5 Completed"
                            else -> "0 of 5 Completed"
                        }
                        Text(text = progress, fontSize = 12.sp, color = subtitleColor)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { when(stressLevel) {
                            "stress_level_low" -> 0.8f
                            "stress_level_moderate" -> 0.4f
                            else -> 0.1f
                        } },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                        color = tealColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (stressLevel == "stress_level_high" || stressLevel == "stress_level_critical") {
                RecommendationItem(
                    icon = Icons.Default.Air,
                    title = stringResource(R.string.reco_urgent_breathing),
                    description = stringResource(R.string.reco_urgent_breathing_desc),
                    duration = stringResource(R.string.extracted_5_min),
                    onStart = onStartBreathing
                )
                Spacer(modifier = Modifier.height(16.dp))
                RecommendationItem(
                    icon = Icons.Default.Cancel,
                    title = stringResource(R.string.reco_cancel_tasks),
                    description = stringResource(R.string.reco_cancel_tasks_desc),
                    duration = stringResource(R.string.reco_action),
                    onStart = {}
                )
            } else {
                RecommendationItem(
                    icon = Icons.Default.Air,
                    title = stringResource(R.string.reco_daily_breathing),
                    description = stringResource(R.string.reco_daily_breathing_desc),
                    duration = stringResource(R.string.extracted_5_min),
                    onStart = onStartBreathing
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            RecommendationItem(
                icon = Icons.Default.NightsStay,
                title = stringResource(R.string.extracted_sleep_optimization),
                description = if(stressLevel.contains("high") || stressLevel.contains("critical")) stringResource(R.string.reco_sleep_urgent) else stringResource(R.string.reco_sleep_normal),
                duration = stringResource(R.string.reco_read_guide),
                onStart = onViewSleepGuide
            )
            Spacer(modifier = Modifier.height(16.dp))
            RecommendationItem(
                icon = Icons.Default.Psychology,
                title = stringResource(R.string.reco_mindfulness_title),
                description = stringResource(R.string.reco_mindfulness_desc),
                duration = stringResource(R.string.extracted_duration_toint_min, 10),
                onStart = onStartMindfulness
            )
            Spacer(modifier = Modifier.height(16.dp))
            RecommendationItem(
                icon = Icons.Default.FitnessCenter,
                title = stringResource(R.string.reco_activity_title),
                description = if(stressLevel.contains("high") || stressLevel.contains("critical")) stringResource(R.string.reco_activity_high_stress) else stringResource(R.string.reco_activity_normal),
                duration = stringResource(R.string.extracted_duration_toint_min, 15),
                onStart = onViewActivityPlan
            )
        }
    }
}

@Composable
fun RecommendationItem(icon: ImageVector, title: String, description: String, duration: String, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                Text(text = duration, fontSize = 11.sp, color = Color(0xFFBDC3C7), modifier = Modifier.padding(top = 4.dp))
            }
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(stringResource(R.string.extracted_start), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BreathingExerciseScreen(onClose: () -> Unit) {
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(4) }
    var phase by remember { mutableStateOf("breath_inhale") }
    var rounds by remember { mutableStateOf(0) }

    val tealColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(isRunning, phase, secondsLeft) {
        if (isRunning) {
            if (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            } else {
                when (phase) {
                    "breath_inhale" -> {
                        phase = "breath_hold"
                        secondsLeft = 7
                    }
                    "breath_hold" -> {
                        phase = "breath_exhale"
                        secondsLeft = 8
                    }
                    "breath_exhale" -> {
                        phase = "breath_inhale"
                        secondsLeft = 4
                        rounds += 1
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = stringResource(R.string.extracted_breathing_exercise), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = stringResource(R.string.extracted_4_7_8_technique), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null)
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            Box(contentAlignment = Alignment.Center) {
                // Outer glow/ring
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(tealColor.copy(alpha = 0.1f), RoundedCornerShape(120.dp))
                )
                // Middle circle
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(tealColor.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                )
                // Core circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(tealColor, RoundedCornerShape(80.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = secondsLeft.toString(), fontSize = 64.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                        Text(text = stringResource(LocalContext.current.resources.getIdentifier(phase, "string", LocalContext.current.packageName)), fontSize = 18.sp, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text(text = stringResource(R.string.extracted_completed_rounds), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = rounds.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Text(if (isRunning) stringResource(R.string.breath_pause) else stringResource(R.string.breath_start), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                InstructionStep(MaterialTheme.colorScheme.primary, stringResource(R.string.breath_instruction_inhale))
                InstructionStep(Color(0xFFFFD700), stringResource(R.string.breath_instruction_hold))
                InstructionStep(MaterialTheme.colorScheme.primary, stringResource(R.string.breath_instruction_exhale))
            }
        }
    }
}

@Composable
fun InstructionStep(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(50)))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SleepOptimizationScreen(onBack: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.NightsStay, null, tint = tealColor)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = stringResource(R.string.extracted_sleep_optimization), fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = stringResource(R.string.extracted_improve_your_sleep_q), fontSize = 12.sp, color = subtitleColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = stringResource(R.string.extracted_why_sleep_matters), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
                Text(stringResource(R.string.extracted_quality_sleep_is_cru),
                    modifier = Modifier.padding(20.dp),
                    fontSize = 14.sp,
                    color = subtitleColor,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_sleep_hygiene_tips), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            
            val tip1Checked = remember { mutableStateOf(false) }
            val tip2Checked = remember { mutableStateOf(false) }
            val tip3Checked = remember { mutableStateOf(false) }
            
            SleepTipItem(Icons.Default.WbSunny, stringResource(R.string.sleep_tip_schedule_title), stringResource(R.string.sleep_tip_schedule_desc), tip1Checked)
            Spacer(modifier = Modifier.height(16.dp))
            SleepTipItem(Icons.Default.Coffee, stringResource(R.string.sleep_tip_caffeine_title), stringResource(R.string.sleep_tip_caffeine_desc), tip2Checked)
            Spacer(modifier = Modifier.height(16.dp))
            SleepTipItem(Icons.Default.PhoneAndroid, stringResource(R.string.sleep_tip_screen_title), stringResource(R.string.sleep_tip_screen_desc), tip3Checked)
        }
    }
}

@Composable
fun SleepTipItem(icon: ImageVector, title: String, description: String, checkedState: MutableState<Boolean>) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { checkedState.value = !checkedState.value }, 
        shape = RoundedCornerShape(20.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        border = BorderStroke(1.dp, if (checkedState.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(40.dp).background(if (checkedState.value) MaterialTheme.colorScheme.primaryContainer else Color(0xFFFFF9E6), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = if (checkedState.value) MaterialTheme.colorScheme.primary else Color(0xFFFFD700), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
            if (checkedState.value) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ActivityPlanScreen(onBack: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FitnessCenter, null, tint = tealColor, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = stringResource(R.string.extracted_activity_plan), fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = stringResource(R.string.extracted_move_to_reduce_stres), fontSize = 12.sp, color = subtitleColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = stringResource(R.string.extracted_exercise_benefits), fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.extracted_regular_physical_act), fontSize = 14.sp, color = subtitleColor, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_recommended_activiti), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            ActivityCard(Icons.Default.DirectionsWalk, stringResource(R.string.activity_walk_title), stringResource(R.string.activity_walk_desc), stringResource(R.string.extracted_5_min), stringResource(R.string.activity_intensity_light)) {
                android.widget.Toast.makeText(context, context.getString(R.string.activity_started, context.getString(R.string.activity_walk_title)), android.widget.Toast.LENGTH_SHORT).show()
            }
            Spacer(modifier = Modifier.height(16.dp))
            ActivityCard(Icons.Default.FitnessCenter, stringResource(R.string.activity_strength_title), stringResource(R.string.activity_strength_desc), stringResource(R.string.extracted_duration_toint_min, 30), stringResource(R.string.activity_intensity_moderate)) {
                android.widget.Toast.makeText(context, context.getString(R.string.activity_started, context.getString(R.string.activity_strength_title)), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun ActivityCard(icon: ImageVector, title: String, description: String, duration: String, intensity: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color(0xFFBDC3C7), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = duration, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFD700), RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = intensity, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(stringResource(R.string.extracted_start_activity), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PrivacyScreen(viewModel: MainViewModel, onLogout: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    var showExportDialog by remember { mutableStateOf(false) }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.extracted_download_my_data)) },
            text = { Text(stringResource(R.string.choose_format)) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.exportData("PDF")
                    showExportDialog = false 
                }) {
                    Text(stringResource(R.string.pdf_format))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.exportData("CSV")
                    showExportDialog = false 
                }) {
                    Text(stringResource(R.string.csv_format))
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(text = stringResource(R.string.extracted_privacy_data), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = stringResource(R.string.extracted_control_your_data_an), fontSize = 14.sp, color = subtitleColor)

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, null, tint = tealColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = stringResource(R.string.extracted_your_data_is_protect), fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = stringResource(R.string.extracted_all_your_health_data), fontSize = 12.sp, color = subtitleColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Privacy Policy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Information Collection", fontWeight = FontWeight.Bold, color = textColor)
                    Text("We collect account details, mood logs, stress levels, and usage analytics to provide personalized insights.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("2. How Data is Used", fontWeight = FontWeight.Bold, color = textColor)
                    Text("Your data is used exclusively to generate wellness insights, improve our AI accuracy, and provide relevant notifications.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("3. Data Sharing & Security", fontWeight = FontWeight.Bold, color = textColor)
                    Text("We do not sell your personal data. All health and account data is encrypted and stored securely.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("4. User Rights", fontWeight = FontWeight.Bold, color = textColor)
                    Text("You have the right to access, export, or permanently delete your data at any time using the options below.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("5. Contact", fontWeight = FontWeight.Bold, color = textColor)
                    Text("For privacy inquiries, contact privacy@cortisense.com.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.extracted_your_data), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { 
                        showExportDialog = true
                    }) {
                        Icon(Icons.Default.Download, null, tint = subtitleColor)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = stringResource(R.string.extracted_download_my_data), fontWeight = FontWeight.Bold, color = textColor)
                            Text(text = stringResource(R.string.extracted_export_all_your_data), fontSize = 11.sp, color = subtitleColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { 
                        viewModel.deleteUserAccount { 
                            onLogout()
                        }
                    }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = stringResource(R.string.extracted_delete_my_account), fontWeight = FontWeight.Bold, color = Color.Red)
                            Text(text = stringResource(R.string.extracted_permanently_remove_a), fontSize = 11.sp, color = subtitleColor)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun AboutScreen(viewModel: MainViewModel, onBack: () -> Unit, onNavigateToChat: () -> Unit = {}) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val isPremium by viewModel.isPremium.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()

    var showPremiumDialog by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }

    // Premium Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = { Text(stringResource(R.string.premium_upgrade_title), fontWeight = FontWeight.Bold, color = tealColor) },
            text = { 
                if (isPremium) {
                    Column {
                        Text(stringResource(R.string.thank_you_premium), fontWeight = FontWeight.Bold, color = tealColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("You are enjoying all the exclusive features of CortiSense Premium. Thank you for your support!", fontSize = 14.sp)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(stringResource(R.string.premium_upgrade_desc))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("How to earn coins:", fontWeight = FontWeight.Bold, color = tealColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("1. Daily Check-ins", fontWeight = FontWeight.Medium)
                        Text("Earn coins every day by completing your mood check-ins.", fontSize = 12.sp, color = subtitleColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text("2. Keep Your Streak", fontWeight = FontWeight.Medium)
                        Text("Maintain a long streak for bonus multiplier coins.", fontSize = 12.sp, color = subtitleColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text("3. Meet Daily Goals", fontWeight = FontWeight.Medium)
                        Text("Complete achievements and wellness goals.", fontSize = 12.sp, color = subtitleColor)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.cost_2000_coins), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.premium_balance_info, userCoins), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                if (!isPremium) {
                    Button(
                        onClick = { 
                            viewModel.buyPremium { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                if (message.startsWith("Success") || message.contains("ధన్యవాదాలు") || message.contains("நன்றி") || message.contains("सफल")) showPremiumDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tealColor)
                    ) {
                        Text(stringResource(R.string.buy_now_2000))
                    }
                } else {
                    TextButton(onClick = { showPremiumDialog = false }) { Text(stringResource(R.string.close_btn)) }
                }
            },
            dismissButton = {
                if (!isPremium) {
                    TextButton(onClick = { showPremiumDialog = false }) { Text(stringResource(R.string.maybe_later)) }
                }
            }
        )
    }

    // What's New Dialog
    if (showWhatsNewDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsNewDialog = false },
            title = { Text(stringResource(R.string.whats_new_dialog)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val updates = listOf(
                        Icons.Default.AutoGraph to "Personalized Stress Insights" to "CortiSense AI now delivers deeper analysis of your daily check-ins to identify hidden stress patterns.",
                        Icons.Default.Spa to "New Stress Relief Tasks" to "Earn coins by completing daily tasks like Deep Breathing, Walking, and Hydration to reach your wellness goals.",
                        Icons.Default.NotificationsActive to "Smart Reminders" to "Never miss a check-in or task with our new adaptive notification system.",
                        Icons.Default.Headset to "Expanded Support" to "Need help? You can now instantly reach our support team via WhatsApp, Email, or Phone."
                    )
                    
                    updates.forEach { (iconTitle, desc) ->
                        val (icon, title) = iconTitle
                        Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.Top) {
                            Box(modifier = Modifier.size(40.dp).background(tealColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = tealColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWhatsNewDialog = false }) { Text(stringResource(R.string.great_btn)) }
            }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text(stringResource(R.string.terms_privacy_dialog)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Effective Date: May 26, 2026", fontSize = 12.sp, color = subtitleColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("1. Introduction", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("Welcome to CortiSense. By using our app, you agree to these Terms and Conditions. Please read them carefully. CortiSense is designed to help you track stress and well-being.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("2. Health Disclaimer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("CortiSense is not a medical device. The insights, suggestions, and AI chat provided are for informational purposes only and do not constitute professional medical advice, diagnosis, or treatment.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("3. Data Privacy & Security", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("We take your privacy seriously. Your clinical history, mood records, and chat logs are stored securely. We do not sell your personal data to third parties. For AI features, anonymized text may be processed securely.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("4. Premium Services", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("Premium features can be unlocked using in-app coins. Coins are earned through daily activity. Virtual currency holds no real-world monetary value and cannot be exchanged for cash.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("5. User Conduct", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("You agree to use CortiSense for lawful purposes only. Misuse of the AI support system or exploiting coin mechanisms is strictly prohibited.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("6. Changes to Terms", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                    Text("We reserve the right to modify these terms at any time. Continued use of the app signifies your acceptance of any updated terms.", fontSize = 13.sp, color = subtitleColor, lineHeight = 18.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) { Text(stringResource(R.string.i_understand_btn)) }
            }
        )
    }

    // Support Dialog
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text(stringResource(R.string.contact_support)) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showSupportDialog = false
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@cortisense.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "CortiSense Support")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Mail Support") }
                    
                    TextButton(
                        onClick = {
                            showSupportDialog = false
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://wa.me/917013995242")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("WhatsApp Chat") }
                    
                    TextButton(
                        onClick = {
                            showSupportDialog = false
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:+917013995242")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No dialer app found", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Call Support") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) { Text(stringResource(R.string.close_btn)) }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            
            Box(modifier = Modifier.size(80.dp).background(tealColor, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.app_name), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = stringResource(R.string.version, "1.1.0"), fontSize = 14.sp, color = subtitleColor)

            Spacer(modifier = Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = stringResource(R.string.extracted_about_cortisense), fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.extracted_cortisense_is_an_ai_), fontSize = 14.sp, color = subtitleColor, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            ProfileMenuItem(Icons.Default.Info, stringResource(R.string.whats_new), onClick = { showWhatsNewDialog = true })
            ProfileMenuItem(Icons.Default.Description, stringResource(R.string.terms_and_privacy), onClick = { showTermsDialog = true })
            ProfileMenuItem(Icons.Default.Email, stringResource(R.string.contact_support), onClick = { 
                showSupportDialog = true
            })
            ProfileMenuItem(Icons.Default.Star, stringResource(R.string.rate_us), onClick = { 
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            })
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.extracted_need_help), 
                fontWeight = FontWeight.Bold, 
                color = textColor,
                modifier = Modifier.clickable { 
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:+917981821290")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
                    }
                }.padding(8.dp)
            )
        }
    }
}

@Composable
fun AlertDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val history by viewModel.history.collectAsState()
    val latestHighStress = history.firstOrNull { it.score > 70 }
    
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val orangeColor = Color(0xFFFFA500)
    
    val timeText = if (latestHighStress != null) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(latestHighStress.timestamp))
    } else "No recent alerts"

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = stringResource(R.string.extracted_alert_details), fontWeight = FontWeight.Bold, color = textColor)
                    Text(text = "Today at $timeText", fontSize = 12.sp, color = subtitleColor)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                border = BorderStroke(1.dp, orangeColor.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = orangeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = if (latestHighStress != null) getTranslatedStressLevel(latestHighStress!!.level) else stringResource(R.string.alert_no_high_stress), fontWeight = FontWeight.Bold, color = textColor)
                        Text(text = stringResource(R.string.alert_score_info, latestHighStress?.score ?: 0), fontSize = 12.sp, color = subtitleColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_contributing_factors), fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))

            latestHighStress?.reasons?.forEach { reason ->
                FactorDetailItem(Icons.Default.Adjust, getTranslatedReason(reason), stringResource(R.string.factor_based_on_inputs), stringResource(R.string.high), orangeColor)
            }
            if (latestHighStress == null) {
                Text(stringResource(R.string.no_factors_detected), modifier = Modifier.padding(16.dp), color = subtitleColor)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoGraph, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(R.string.extracted_pattern_analysis), fontWeight = FontWeight.Bold, color = textColor)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(R.string.extracted_this_is_your_3rd_hig), fontSize = 14.sp, color = subtitleColor, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
fun FactorDetailItem(icon: ImageVector, title: String, value: String, level: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = level, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AchievementsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val history by viewModel.history.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()
    
    val totalCheckins = history.size
    val lowStressCount = history.count { it.score < 40 }
    val is7DayStreak = longestStreak.toInt() >= 7
    val isFirstCheckin = totalCheckins >= 1
    
    val unlockedCount = (if (is7DayStreak) 1 else 0) + (if (isFirstCheckin) 1 else 0)
    
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(text = stringResource(R.string.extracted_achievements), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AchievementStatCard(modifier = Modifier.weight(1f), value = "$unlockedCount", label = stringResource(R.string.achievements_unlocked), color = tealColor)
                AchievementStatCard(modifier = Modifier.weight(1f), value = "${4 - unlockedCount}", label = stringResource(R.string.achievements_locked), color = Color(0xFFFFD700))
                AchievementStatCard(modifier = Modifier.weight(1f), value = "4", label = stringResource(R.string.achievements_total), color = subtitleColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AchievementItem(Icons.Default.Whatshot, stringResource(R.string.achievements_7day_streak), stringResource(R.string.achievements_7day_streak_desc), is7DayStreak)
            AchievementItem(Icons.Default.CheckCircle, stringResource(R.string.achievements_first_checkin), stringResource(R.string.achievements_first_checkin_desc), isFirstCheckin)
            AchievementProgressItem(Icons.Default.Favorite, stringResource(R.string.achievements_stress_warrior), stringResource(R.string.achievements_stress_warrior_desc), (lowStressCount / 30f).coerceAtMost(1f), "${(lowStressCount / 30f * 100).toInt()}%")
            AchievementProgressItem(Icons.Default.History, stringResource(R.string.achievements_century_club), stringResource(R.string.achievements_century_club_desc), (totalCheckins / 100f).coerceAtMost(1f), "${(totalCheckins / 100f * 100).toInt()}%")
        }
    }
}

@Composable
fun AchievementStatCard(modifier: Modifier, value: String, label: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AchievementItem(
    icon: ImageVector,
    title: String,
    description: String,
    isUnlocked: Boolean,
    unlockedTag: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked) MaterialTheme.colorScheme.primary else Color(0xFFBDC3C7),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (unlockedTag != null) {
                Text(
                    text = unlockedTag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else if (isUnlocked) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AchievementProgressItem(icon: ImageVector, title: String, description: String, progress: Float, progressText: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color(0xFFBDC3C7))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = progressText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}



@Composable
fun ExportReportScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedPeriod by remember { mutableStateOf("Last 30 Days") }
    var selectedFormat by remember { mutableStateOf("PDF") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(text = stringResource(R.string.extracted_export_report), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = stringResource(R.string.extracted_download_your_wellne), fontSize = 14.sp, color = subtitleColor)

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_select_time_period), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))

            ExportPeriodItem(icon = Icons.Outlined.CalendarToday, label = "Last 7 Days", size = "84 KB", isSelected = selectedPeriod == "Last 7 Days") {
                selectedPeriod = "Last 7 Days"
            }
            ExportPeriodItem(icon = Icons.Outlined.CalendarMonth, label = "Last 30 Days", size = "320 KB", isSelected = selectedPeriod == "Last 30 Days") {
                selectedPeriod = "Last 30 Days"
            }
            ExportPeriodItem(icon = Icons.Outlined.DateRange, label = "Last 3 Months", size = "950 KB", isSelected = selectedPeriod == "Last 3 Months") {
                selectedPeriod = "Last 3 Months"
            }
            ExportPeriodItem(icon = Icons.Outlined.History, label = "All Time", size = "2.1 MB", isSelected = selectedPeriod == "All Time") {
                selectedPeriod = "All Time"
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_export_format), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExportFormatCard(modifier = Modifier.weight(1f), icon = Icons.Default.PictureAsPdf, label = stringResource(R.string.pdf_format), isSelected = selectedFormat == "PDF") {
                    selectedFormat = "PDF"
                }
                ExportFormatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Description, label = stringResource(R.string.csv_format), isSelected = selectedFormat == "CSV") {
                    selectedFormat = "CSV"
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = stringResource(R.string.extracted_report_includes), fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    val items = listOf(
                        "Daily stress scores and trends",
                        "Physiological data (heart rate, sleep)",
                        "Psychological assessments",
                        "Lifestyle factors and patterns",
                        "AI insights and recommendations"
                    )
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = tealColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = item, fontSize = 13.sp, color = Color(0xFF4B5563))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.exportReport(selectedPeriod, selectedFormat) { message ->
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        if (message.contains("saved", ignoreCase = true)) {
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Text(stringResource(R.string.extracted_generate_report), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
            }
        }
    }
}

@Composable
fun ExportPeriodItem(icon: ImageVector, label: String, size: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            }
            Text(text = size, fontSize = 12.sp, color = Color(0xFFBDC3C7))
        }
    }
}

@Composable
fun ExportFormatCard(modifier: Modifier, icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() }, 
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ShareResultsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val history by viewModel.history.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    
    val avgScore = if (history.isEmpty()) 0 else history.map { it.score }.average().toInt()
    
    val shareText = stringResource(R.string.share_text, avgScore)

    fun shareData() {
        val sendIntent: android.content.Intent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary
    val yellowColor = Color(0xFFFFD700)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(text = stringResource(R.string.extracted_share_results), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = stringResource(R.string.extracted_share_your_progress_), fontSize = 14.sp, color = subtitleColor)

            Spacer(modifier = Modifier.height(32.dp))

            // Score Share Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val totalCheckins by viewModel.totalCheckins.collectAsState()
                    val lowScore by viewModel.todayLowestScore.collectAsState()
                    
                    // Score Circle
                    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            drawArc(outlineVariantColor, 0f, 360f, false, style = Stroke(10.dp.toPx()))
                            drawArc(tealColor, -90f, (avgScore.toFloat() / 100f) * 360f, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = avgScore.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text(text = stringResource(R.string.stress_score), fontSize = 10.sp, color = subtitleColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = stringResource(R.string.extracted_weekly_average), fontSize = 14.sp, color = subtitleColor)
                    Text(text = avgScore.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, null, tint = tealColor, modifier = Modifier.size(14.dp))
                        Text(text = stringResource(R.string.share_better_than_last), color = tealColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ShareStatItem(totalCheckins, stringResource(R.string.share_days_tracked))
                        ShareStatItem(currentStreak, stringResource(R.string.share_day_streak))
                        ShareStatItem(lowScore.toString(), stringResource(R.string.share_lowest_score))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_share_via), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))

            ShareViaItem(icon = Icons.Outlined.Email, title = stringResource(R.string.share_email), subtitle = stringResource(R.string.share_email_desc)) { shareData() }
            ShareViaItem(icon = Icons.Outlined.ChatBubbleOutline, title = stringResource(R.string.share_message), subtitle = stringResource(R.string.share_message_desc)) { shareData() }
            ShareViaItem(icon = Icons.Outlined.ContentCopy, title = stringResource(R.string.share_copy), subtitle = stringResource(R.string.share_copy_desc)) { shareData() }
        }
    }
}

@Composable
fun ShareViaItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.Share, null, tint = Color(0xFFBDC3C7), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ShareStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorScreen(onRetry: () -> Unit, onBackHome: () -> Unit) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val tealColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(R.string.error_something_wrong),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.extracted_we_encountered_an_er),
                color = subtitleColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.extracted_try_again), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(stringResource(R.string.extracted_go_back_home), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            Text(text = stringResource(R.string.extracted_error_code_err_500), fontSize = 12.sp, color = Color(0xFF9CA3AF))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.extracted_if_this_problem_pers), fontSize = 12.sp, color = Color(0xFF9CA3AF))
                Text(text = stringResource(R.string.extracted_support), fontSize = 12.sp, color = tealColor, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
            }
        }
    }
}

@Composable
fun EmptyHomeScreen(onStartCheckIn: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Timeline, null, tint = tealColor, modifier = Modifier.size(60.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_no_data_yet), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(stringResource(R.string.extracted_you_haven_t_logged_a),
                color = subtitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onStartCheckIn,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor)
            ) {
                Text(stringResource(R.string.extracted_start_your_first_che), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.surface)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NumberedStep(1, stringResource(R.string.empty_home_step1_title), stringResource(R.string.empty_home_step1_desc))
                NumberedStep(2, stringResource(R.string.empty_home_step2_title), stringResource(R.string.empty_home_step2_desc))
                NumberedStep(3, stringResource(R.string.empty_home_step3_title), stringResource(R.string.empty_home_step3_desc))
            }
        }
    }
}

@Composable
fun NumberedStep(number: Int, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

@Composable
fun AppearanceScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemDark
    }
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            }
            Text(text = stringResource(R.string.extracted_appearance), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = stringResource(R.string.extracted_choose_your_preferre), fontSize = 14.sp, color = subtitleColor)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_current_theme_previe), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock Preview Card matching image
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF111827) else MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(tealColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.WbSunny, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = stringResource(R.string.app_name), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                                Text(text = stringResource(R.string.extracted_stress_score_35), fontSize = 10.sp, color = subtitleColor)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        PreviewMiniStat(stringResource(R.string.preview_heart_rate), "72", isDarkTheme)
                        PreviewMiniStat(stringResource(R.string.preview_sleep), "7.5h", isDarkTheme)
                        PreviewMiniStat(stringResource(R.string.preview_mood), stringResource(R.string.option_good), isDarkTheme)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(R.string.extracted_select_theme), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = subtitleColor)
            Spacer(modifier = Modifier.height(16.dp))
            
            ThemeOption(Icons.Default.LightMode, "Light mode", "Classic light interface", themeMode == "light") { viewModel.setThemeMode("light") }
            ThemeOption(Icons.Default.DarkMode, stringResource(R.string.dark_mode_title), "Easy on the eyes in low light", themeMode == "dark") { viewModel.setThemeMode("dark") }
            ThemeOption(Icons.Default.Settings, "System Default", "Adapts to your device settings", themeMode == "system") { viewModel.setThemeMode("system") }
        }
    }
}

@Composable
fun PreviewMiniStat(label: String, value: String, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun ThemeOption(icon: ImageVector, title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LanguageScreen(currentLanguage: String, onLanguageChange: (String) -> Unit, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Language, null, tint = tealColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = stringResource(R.string.language), fontWeight = FontWeight.Bold, color = textColor, fontSize = 20.sp)
                Text(text = stringResource(R.string.extracted_select_language), fontSize = 12.sp, color = subtitleColor)
            }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.language), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = subtitleColor) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val allLanguages = listOf(
                    Triple("English", "English", "🇺🇸") to "en",
                    Triple("हिंदी", "Hindi", "🇮🇳") to "hi",
                    Triple("தமிழ்", "Tamil", "🇮🇳") to "ta",
                    Triple("తెలుగు", "Telugu", "🇮🇳") to "te"
                )
                
                val filteredLanguages = allLanguages.filter { (langInfo, _) ->
                    val (label, subtitle, _) = langInfo
                    label.contains(searchQuery, ignoreCase = true) || subtitle.contains(searchQuery, ignoreCase = true)
                }
                
                filteredLanguages.forEach { (langInfo, code) ->
                    val (label, subtitle, flag) = langInfo
                    LanguageItem(label, subtitle, flag, currentLanguage == code) { onLanguageChange(code) }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
                Text(
                    text = stringResource(R.string.tagline), 
                    modifier = Modifier.padding(12.dp).fillMaxWidth(), 
                    fontSize = 12.sp, 
                    color = subtitleColor, 
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LanguageItem(label: String, subtitle: String, flag: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = flag, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun VerifyOTPScreen(
    email: String,
    onBack: () -> Unit,
    onVerify: (String) -> Unit
) {
    var otp by remember { mutableStateOf("") }
    
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Verify OTP", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter the 6-digit OTP sent to $email", fontSize = 14.sp, color = subtitleColor, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            CustomTextField(
                label = "6-Digit OTP",
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                placeholder = "123456"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onVerify(otp) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = otp.length == 6
            ) {
                Text("Verify", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Back", color = tealColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    email: String,
    onBack: () -> Unit,
    onReset: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("New Password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Set a new password for $email", fontSize = 14.sp, color = subtitleColor, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            CustomTextField(
                label = "New Password",
                value = password,
                onValueChange = { password = it },
                isPassword = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CustomTextField(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                isPassword = true
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onReset(password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = password.isNotEmpty() && password == confirmPassword
            ) {
                Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = onBack) {
                Text("Cancel", color = tealColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
