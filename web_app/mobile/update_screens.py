import os

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

profile_start = -1
profile_end = -1
forgot_start = -1
forgot_end = -1

for i, l in enumerate(lines):
    if 'fun ProfileSetupScreen(' in l:
        profile_start = i - 1
    elif 'fun ProfileSetupPreview(' in l:
        if profile_end == -1: profile_end = i - 2
    elif 'fun ForgotPasswordScreen(' in l:
        forgot_start = i - 1
    elif 'fun ForgotPasswordPreview(' in l:
        if forgot_end == -1: forgot_end = i - 2

profile_setup_code = """@Composable
fun ProfileSetupScreen(onContinue: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
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
                text = "Personal Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CustomTextField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it; errorMessage = null }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "Date of Birth (DD/MM/YYYY)",
                value = dob,
                onValueChange = { dob = it; errorMessage = null },
                placeholder = "01/01/1995"
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
                val genders = listOf("Male", "Female", "Other")
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

            val goals = listOf(stringResource(R.string.reduce_stress), stringResource(R.string.better_sleep), stringResource(R.string.improve_focus))
            goals.forEach { goal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            if (selectedGoal == goal) tealColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedGoal = goal; errorMessage = null }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGoal == goal,
                        onClick = { selectedGoal = goal; errorMessage = null },
                        colors = RadioButtonDefaults.colors(selectedColor = tealColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(goal, color = if (selectedGoal == goal) tealColor else textColor, fontWeight = if (selectedGoal == goal) FontWeight.Bold else FontWeight.Normal)
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
                        errorMessage = "Please complete all fields"
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
"""

forgot_password_code = """@Composable
fun ForgotPasswordScreen(
    registeredEmail: String,
    onBackToLogin: () -> Unit,
    onPasswordReset: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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

            val title = when (step) {
                1 -> "Reset Password"
                2 -> "Verify OTP"
                else -> "New Password"
            }
            
            val desc = when (step) {
                1 -> "Enter your email to receive an OTP"
                2 -> "Enter the 6-digit code sent to \$email"
                else -> "Enter your new password"
            }

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

            if (step == 1) {
                CustomTextField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    placeholder = "john@example.com"
                )
            } else if (step == 2) {
                CustomTextField(
                    label = "6-Digit OTP",
                    value = otp,
                    onValueChange = { otp = it; errorMessage = null }
                )
            } else if (step == 3) {
                CustomTextField(
                    label = "New Password",
                    value = newPassword,
                    onValueChange = { newPassword = it; errorMessage = null },
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomTextField(
                    label = "Confirm New Password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    isPassword = true
                )
            }

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
                    if (step == 1) {
                        if (email != registeredEmail) {
                            errorMessage = "Email not registered."
                        } else {
                            // Simulate sending email
                            generatedOtp = (100000..999999).random().toString()
                            android.widget.Toast.makeText(context, "OTP: \$generatedOtp", android.widget.Toast.LENGTH_LONG).show()
                            step = 2
                            errorMessage = null
                            successMessage = "OTP sent to your email!"
                        }
                    } else if (step == 2) {
                        if (otp == generatedOtp) {
                            step = 3
                            errorMessage = null
                            successMessage = null
                        } else {
                            errorMessage = "Invalid OTP."
                        }
                    } else if (step == 3) {
                        if (newPassword.isBlank() || confirmPassword.isBlank()) {
                            errorMessage = "Fields cannot be empty."
                        } else if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match."
                        } else {
                            onPasswordReset(newPassword)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                val btnText = when (step) {
                    1 -> "Send OTP"
                    2 -> "Verify Code"
                    else -> "Update Password"
                }
                Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (step == 2) {
                TextButton(onClick = {
                    generatedOtp = (100000..999999).random().toString()
                    android.widget.Toast.makeText(context, "New OTP: \$generatedOtp", android.widget.Toast.LENGTH_LONG).show()
                    successMessage = "New OTP sent!"
                }) {
                    Text("Resend Code", color = tealColor)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(onClick = onBackToLogin) {
                Text(stringResource(R.string.back_to_signin), color = subtitleColor)
            }
        }
    }
}
"""

new_lines = []
for i in range(len(lines)):
    if i == profile_start:
        new_lines.append(profile_setup_code + '\n')
    elif i > profile_start and i <= profile_end:
        continue
    elif i == forgot_start:
        new_lines.append(forgot_password_code + '\n')
    elif i > forgot_start and i <= forgot_end:
        continue
    else:
        new_lines.append(lines[i])

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
    
print("Updated ProfileSetupScreen and ForgotPasswordScreen")
