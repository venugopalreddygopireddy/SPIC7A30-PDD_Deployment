package com.cortisense.app

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    BackHandler { navController.popBackStack() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe DataStore fields from ViewModel
    val initialFirstName by viewModel.userFirstName.collectAsState()
    val initialLastName by viewModel.userLastName.collectAsState()
    val initialDob by viewModel.userDob.collectAsState()
    val initialMobile by viewModel.userMobile.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val initialAge by viewModel.userAge.collectAsState()
    val initialGender by viewModel.userGender.collectAsState()
    val initialGoal by viewModel.userGoal.collectAsState()
    val initialImageUri by viewModel.profileImageUri.collectAsState()

    var imageUri by remember(initialImageUri) { mutableStateOf(initialImageUri) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri.toString() }
    )

    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var dob by remember(initialDob) { mutableStateOf(initialDob) }
    var mobile by remember(initialMobile) { mutableStateOf(initialMobile) }
    var age by remember(initialAge) { mutableStateOf(initialAge) }
    var gender by remember(initialGender) { mutableStateOf(initialGender) }
    var goal by remember(initialGoal) { mutableStateOf(initialGoal) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dob = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            val today = java.util.Calendar.getInstance()
            var calculatedAge = today.get(java.util.Calendar.YEAR) - year
            if (today.get(java.util.Calendar.MONTH) < month || 
                (today.get(java.util.Calendar.MONTH) == month && today.get(java.util.Calendar.DAY_OF_MONTH) < dayOfMonth)) {
                calculatedAge--
            }
            age = calculatedAge.toString()
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    val updatedMessage = stringResource(id = R.string.profile_updated)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.edit_profile), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            

            // Profile Picture
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)),
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
                    Text(stringResource(R.string.extracted_change_photo), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Fields
            CustomTextField(
                label = "First Name",
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "Enter First Name"
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "Last Name",
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "Enter Last Name"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mobile Number
            CustomTextField(
                label = "Mobile Number",
                value = mobile,
                onValueChange = { mobile = it },
                placeholder = "Enter Mobile Number"
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth Field
            Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                CustomTextField(
                    label = "Date of Birth", 
                    value = dob, 
                    onValueChange = {}, 
                    placeholder = "DD/MM/YYYY",
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Age Field (Read-only)
            CustomTextField(
                label = stringResource(id = R.string.age),
                value = age,
                onValueChange = {},
                placeholder = stringResource(R.string.extracted_enter_your_age),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Field
            CustomTextField(
                label = stringResource(id = R.string.gender),
                value = gender,
                onValueChange = { gender = it },
                placeholder = stringResource(R.string.extracted_select_gender)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.primary_goal_question),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
            )

            // Goal Options
            GoalOption(
                icon = Icons.Default.Adjust,
                title = stringResource(id = R.string.reduce_stress),
                isSelected = goal == "Reduce Stress" || goal == stringResource(id = R.string.reduce_stress),
                onClick = { goal = "Reduce Stress" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoalOption(
                icon = Icons.Default.NightsStay,
                title = stringResource(id = R.string.better_sleep),
                isSelected = goal == "Better Sleep" || goal == stringResource(id = R.string.better_sleep),
                onClick = { goal = "Better Sleep" }
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoalOption(
                icon = Icons.Default.CenterFocusStrong,
                title = stringResource(id = R.string.improve_focus),
                isSelected = goal == "Improve Focus" || goal == stringResource(id = R.string.improve_focus),
                onClick = { goal = "Improve Focus" }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.updateProfile(firstName, lastName, userEmail, dob, age, gender, goal, mobile, imageUri)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(updatedMessage)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(id = R.string.save_profile),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun GoalOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isSelected: Boolean, onClick: () -> Unit) {
    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier.fillMaxWidth().background(if (isSelected) tealColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) tealColor else textColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = if (isSelected) tealColor else textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
