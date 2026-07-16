package com.cortisense.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    var showTermsModal by remember { mutableStateOf(false) }
    var showWhatsNewModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About & Help", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    AboutItem(
                        icon = Icons.Default.Description,
                        title = "Terms and Privacy",
                        onClick = { showTermsModal = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    AboutItem(
                        icon = Icons.Default.NewReleases,
                        title = "What's New",
                        onClick = { showWhatsNewModal = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("CONTACT SUPPORT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    AboutItem(
                        icon = Icons.Default.Email,
                        title = "Mail Support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@cortisense.com")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    AboutItem(
                        icon = Icons.Default.Chat,
                        title = "WhatsApp Chat",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://api.whatsapp.com/send?phone=7013995242")
                            }
                            context.startActivity(intent)
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    AboutItem(
                        icon = Icons.Default.Phone,
                        title = "Call Support",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:7013995242")
                            }
                            context.startActivity(intent)
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    AboutItem(
                        icon = Icons.Default.Message,
                        title = "Need Help",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:7013995242")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("MORE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    AboutItem(
                        icon = Icons.Default.Star,
                        title = "Rate Us",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                // Since this is the mobile app, open Play Store
                                data = Uri.parse("market://details?id=com.cortisense.app")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // If Play Store is not installed, open web browser
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.cortisense.app")))
                            }
                        }
                    )
                }
            }
        }
    }

    if (showTermsModal) {
        AlertDialog(
            onDismissRequest = { showTermsModal = false },
            title = { Text("Terms and Privacy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("1. Information Collection", fontWeight = FontWeight.Bold)
                    Text("We collect account details, mood logs, stress levels, and usage analytics to provide personalized insights.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2. How Data is Used", fontWeight = FontWeight.Bold)
                    Text("Your data is used exclusively to generate wellness insights, improve our AI accuracy, and provide relevant notifications.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3. Data Sharing & Security", fontWeight = FontWeight.Bold)
                    Text("We do not sell your personal data. All health and account data is encrypted and stored securely.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("4. User Rights", fontWeight = FontWeight.Bold)
                    Text("You have the right to access, export, or permanently delete your data at any time.", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showWhatsNewModal) {
        AlertDialog(
            onDismissRequest = { showWhatsNewModal = false },
            title = { Text("What's New", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Version 1.0.0", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Added complete data export in CSV and PDF formats.")
                    Text("• Added account deletion options for better privacy control.")
                    Text("• Replicated Web App features into Mobile seamlessly.")
                    Text("• Performance and UI improvements across the app.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showWhatsNewModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AboutItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
