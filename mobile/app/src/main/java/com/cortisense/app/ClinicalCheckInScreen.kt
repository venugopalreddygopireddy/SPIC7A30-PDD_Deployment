package com.cortisense.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cortisense.app.ui.theme.DeepIndigo
import com.cortisense.app.ui.theme.SageGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalCheckInScreen(
    viewModel: MainViewModel,
    onAnalyze: (StressResponse) -> Unit,
    onBack: () -> Unit
) {
    var currentPage by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()
    val totalPages = 6

    BackHandler {
        if (currentPage > 1) currentPage--
        else onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Wellness Check-In", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (currentPage > 1) currentPage-- else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Step $currentPage of $totalPages", style = MaterialTheme.typography.bodyMedium)
                    
                    if (currentPage < totalPages) {
                        Button(
                            onClick = { currentPage++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.submitCheckIn { onAnalyze(it) } },
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Submit Analysis")
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Check, null)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { currentPage.toFloat() / totalPages.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = SageGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(Modifier.height(24.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "PageTransition"
            ) { page ->
                when (page) {
                    1 -> PagePersonal(viewModel)
                    2 -> PageSleep(viewModel)
                    3 -> PageLifestyle(viewModel)
                    4 -> PageWork(viewModel)
                    5 -> PageHealth(viewModel)
                    6 -> PageMental(viewModel)
                }
            }
            
            if (viewModel.errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun PagePersonal(vm: MainViewModel) {
    val age by vm.userAge.collectAsState()
    val gender by vm.userGender.collectAsState()
    val mobile by vm.userMobile.collectAsState()

    LaunchedEffect(age, gender) {
        vm.checkInAge = age
        vm.checkInGender = gender
    }

    SectionContainer(title = "Personal Information", icon = "👤") {
        Text(
            text = "Note: If you want to change Age, Gender, or Mobile Number, please edit them in Edit Profile.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = gender,
            onValueChange = { },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false
        )
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = mobile,
            onValueChange = { },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = vm.checkInOccupation,
            onValueChange = { vm.checkInOccupation = it },
            label = { Text("Occupation") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Marital Status", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Single", "Married", "Divorced").forEach {
                FilterChip(
                    selected = vm.checkInMaritalStatus == it,
                    onClick = { vm.checkInMaritalStatus = it },
                    label = { Text(it) }
                )
            }
        }
    }
}

@Composable
fun PageSleep(vm: MainViewModel) {
    SectionContainer(title = "Sleep Information", icon = "🌙") {
        OutlinedTextField(
            value = vm.checkInWakeUpTime,
            onValueChange = { 
                vm.checkInWakeUpTime = it
                vm.calculateSleepMetrics()
            },
            label = { Text("Wake Up Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = vm.checkInBedTime,
            onValueChange = { 
                vm.checkInBedTime = it
                vm.calculateSleepMetrics()
            },
            label = { Text("Bed Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Calculated Sleep Duration: ${String.format(java.util.Locale.US, "%.1f", vm.checkInSleepDuration)} hours", 
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(16.dp))
        Text("Calculated Sleep Quality (1-5): ${vm.checkInSleepQuality}", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (1..5).forEach {
                Icon(
                    Icons.Default.Check, // Placeholder for Star icon
                    contentDescription = null,
                    tint = if (vm.checkInSleepQuality >= it) SageGreen else Color.Gray
                )
            }
        }
    }
}

@Composable
fun PageLifestyle(vm: MainViewModel) {
    SectionContainer(title = "Lifestyle & Habits", icon = "⚡") {
        Text("Physical Activity (Level 1-5)", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInPhysicalActivity.toFloat(), onValueChange = { vm.checkInPhysicalActivity = it.toInt() }, valueRange = 1f..5f, steps = 3)
        
        Spacer(Modifier.height(16.dp))
        Text("Screen Time: ${vm.checkInScreenTime.toInt()}h ${((vm.checkInScreenTime % 1) * 60).toInt()}m", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInScreenTime, onValueChange = { vm.checkInScreenTime = it }, valueRange = 0f..16f)

        Spacer(Modifier.height(16.dp))
        Text("Caffeine Intake (Cups)", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..4).forEach {
                InputChip(selected = vm.checkInCaffeineIntake == it, onClick = { vm.checkInCaffeineIntake = it }, label = { Text("$it") })
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Alcohol Intake (Freq 0-5)", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInAlcoholIntake.toFloat(), onValueChange = { vm.checkInAlcoholIntake = it.toInt() }, valueRange = 0f..5f, steps = 4)

        Spacer(Modifier.height(16.dp))
        Text("Smoking Habit", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("None", "Occasional", "Frequent").forEach {
                FilterChip(selected = vm.checkInSmokingHabit == it, onClick = { vm.checkInSmokingHabit = it }, label = { Text(it) })
            }
        }
    }
}

@Composable
fun PageWork(vm: MainViewModel) {
    SectionContainer(title = "Work & Daily Routine", icon = "💼") {
        Text("Work Hours (Per Day): ${vm.checkInWorkHours.toInt()}h ${((vm.checkInWorkHours % 1) * 60).toInt()}m", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInWorkHours, onValueChange = { vm.checkInWorkHours = it }, valueRange = 0f..16f)

        Spacer(Modifier.height(16.dp))
        Text("Travel Time: ${vm.checkInTravelTime.toInt()}h ${((vm.checkInTravelTime % 1) * 60).toInt()}m", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInTravelTime, onValueChange = { vm.checkInTravelTime = it }, valueRange = 0f..5f)

        Spacer(Modifier.height(16.dp))
        val dots = "●".repeat(vm.checkInSocialInteractions) + "○".repeat(5 - vm.checkInSocialInteractions)
        Text("Social Interactions: $dots", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInSocialInteractions.toFloat(), onValueChange = { vm.checkInSocialInteractions = it.toInt() }, valueRange = 1f..5f)

        Spacer(Modifier.height(16.dp))
        Text("Workload Level", style = MaterialTheme.typography.labelLarge)
        val workloadOptions = listOf("Light", "Normal", "Moderate", "Heavy", "Extreme")
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in workloadOptions.indices step 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val option1 = workloadOptions[i]
                    FilterChip(
                        selected = vm.checkInWorkload == option1,
                        onClick = { vm.checkInWorkload = option1 },
                        label = { Text(option1) },
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < workloadOptions.size) {
                        val option2 = workloadOptions[i + 1]
                        FilterChip(
                            selected = vm.checkInWorkload == option2,
                            onClick = { vm.checkInWorkload = option2 },
                            label = { Text(option2) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun PageHealth(vm: MainViewModel) {
    SectionContainer(title = "Health & Wellness", icon = "🏥") {
        Text("Meditation Practice: ${vm.checkInMeditationPractice} mins", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInMeditationPractice.toFloat(), onValueChange = { vm.checkInMeditationPractice = it.toInt() }, valueRange = 0f..30f)

        Spacer(Modifier.height(16.dp))
        Text("Exercise Type (Select multiple)", style = MaterialTheme.typography.labelLarge)
        val exercises = listOf("Gym", "Yoga", "Running", "Walking", "Cycling", "Swimming")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            exercises.forEach { ex ->
                FilterChip(
                    selected = vm.checkInExerciseTypes.contains(ex),
                    onClick = { 
                        if (vm.checkInExerciseTypes.contains(ex)) vm.checkInExerciseTypes.remove(ex)
                        else vm.checkInExerciseTypes.add(ex)
                    },
                    label = { Text(ex) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = vm.checkInBloodPressure, onValueChange = { vm.checkInBloodPressure = it }, label = { Text("Blood Pressure (mmHg)") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))
        Text("Blood Sugar (mg/dL): ${vm.checkInBloodSugarLevel}", style = MaterialTheme.typography.labelLarge)
        Slider(value = vm.checkInBloodSugarLevel.toFloat(), onValueChange = { vm.checkInBloodSugarLevel = it.toInt() }, valueRange = 70f..200f)
    }
}

@Composable
fun PageMental(vm: MainViewModel) {
    SectionContainer(title = "Mental & Emotional State", icon = "🧠") {
        Text("Mood", style = MaterialTheme.typography.labelLarge)
        val moods = listOf("Happy", "Calm", "Neutral", "Sad", "Depressed")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            moods.forEach {
                FilterChip(selected = vm.checkInMood == it, onClick = { vm.checkInMood = it }, label = { Text(it) })
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Anxiety Level", style = MaterialTheme.typography.labelLarge)
        val anxietyLevels = listOf("Low", "Mild", "Moderate", "High", "Extreme")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            anxietyLevels.forEach {
                FilterChip(selected = vm.checkInAnxiety == it, onClick = { vm.checkInAnxiety = it }, label = { Text(it) })
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Body Feeling", style = MaterialTheme.typography.labelLarge)
        val feelings = listOf("Relaxed", "Normal", "Tired", "Exhausted", "Burnout")
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            feelings.forEach {
                FilterChip(selected = vm.checkInBodyFeeling == it, onClick = { vm.checkInBodyFeeling = it }, label = { Text(it) })
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Caffeine Dependency", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("No", "Slight", "Yes").forEach {
                FilterChip(selected = vm.checkInCaffeineDependency == it, onClick = { vm.checkInCaffeineDependency = it }, label = { Text(it) })
            }
        }
    }
}

@Composable
fun SectionContainer(title: String, icon: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Divider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier = Modifier, horizontalArrangement: Arrangement.Horizontal = Arrangement.Start, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        content()
    }
}