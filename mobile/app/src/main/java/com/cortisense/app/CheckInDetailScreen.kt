package com.cortisense.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDetailScreen(
    checkInId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var checkIn by remember { mutableStateOf<StressCheckInEntity?>(null) }
    
    LaunchedEffect(checkInId) {
        checkIn = viewModel.getCheckInById(checkInId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        checkIn?.let { data ->
            val color = when (data.stressLevel) {
                "Low" -> Color(0xFF5E9B72)
                "Moderate" -> Color(0xFFF5A623)
                "High" -> Color(0xFFE8714A)
                "Critical" -> Color(0xFFD63E3E)
                else -> Color.Gray
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(data.stressLevel, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
                        Text("Score: ${data.score}/100", fontSize = 16.sp, color = color.copy(alpha = 0.8f))
                        Spacer(Modifier.height(8.dp))
                        Text("${data.date} at ${data.time}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // AI Recommendation
                Text("AI Recommendation", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = data.recommendation,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }

                if (data.isEscalated) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4B4B).copy(alpha = 0.1f))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFF4B4B))
                            Spacer(Modifier.width(12.dp))
                            Text("Escalation protocol was active for this session.", color = Color(0xFFFF4B4B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // All 25 Parameters Breakdown
                Text("Detailed Metrics", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                
                DetailSection("Personal") {
                    DetailRow("Age", data.age.toString())
                    DetailRow("Gender", data.gender)
                    DetailRow("Occupation", data.occupation)
                    DetailRow("Marital Status", data.maritalStatus)
                }
                
                DetailSection("Sleep") {
                    DetailRow("Duration", "${data.sleepDuration} hrs")
                    DetailRow("Quality", "${data.sleepQuality}/5")
                    DetailRow("Wake/Bed", "${data.wakeUpTime} / ${data.bedTime}")
                }
                
                DetailSection("Lifestyle") {
                    DetailRow("Activity Level", "${data.physicalActivity}/5")
                    DetailRow("Screen Time", "${data.screenTime} hrs")
                    DetailRow("Caffeine/Alcohol", "${data.caffeineIntake} / ${data.alcoholIntake}")
                    DetailRow("Smoking", data.smokingHabit)
                }
                
                DetailSection("Work & Routine") {
                    DetailRow("Work Hours", "${data.workHours} hrs")
                    DetailRow("Travel Time", "${data.travelTime} hrs")
                    DetailRow("Social Score", "${data.socialInteractions}/5")
                    DetailRow("Workload", data.workload)
                }
                
                DetailSection("Health & Wellness") {
                    DetailRow("Meditation", "${data.meditationPractice} mins")
                    DetailRow("Exercise Type", data.exerciseType)
                    DetailRow("Blood Pressure", data.bloodPressure)
                    DetailRow("Blood Sugar", "${data.bloodSugarLevel} mg/dL")
                }
                
                DetailSection("Mental State") {
                    DetailRow("Mood", data.mood)
                    DetailRow("Anxiety", data.anxiety)
                    DetailRow("Body Feeling", data.bodyFeeling)
                    DetailRow("Caffeine Dependency", data.caffeineDependency)
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
