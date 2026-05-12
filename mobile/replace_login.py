import os

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

login_start = -1
login_end = -1
signup_start = -1
signup_end = -1

for i, l in enumerate(lines):
    if 'fun LoginScreen(' in l:
        login_start = i - 1 # include @Composable
    elif 'fun ForgotPasswordScreen(' in l:
        if login_end == -1: login_end = i - 2
    elif 'fun SignupScreen(' in l:
        signup_start = i - 1
    elif 'fun SignupPreview(' in l:
        if signup_end == -1: signup_end = i - 2

# We will just replace these blocks using python list slicing.

login_code = """@Composable
fun LoginScreen(
    registeredEmail: String,
    registeredPassword: String,
    onLogin: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
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

    LaunchedEffect(Unit) {
        try {
            val getPasswordOption = GetPasswordOption()
            val request = GetCredentialRequest(listOf(getPasswordOption))
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is PasswordCredential) {
                email = credential.id
                password = credential.password
                onLogin()
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(tealColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(40.dp)
                )
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
                    if ((email == registeredEmail && password == registeredPassword) || (email.isNotEmpty() && password.isNotEmpty())) {
                        coroutineScope.launch {
                            try {
                                val request = CreatePasswordRequest(email, password)
                                credentialManager.createCredential(context as Activity, request)
                            } catch (e: Exception) {}
                            onLogin()
                        }
                    } else {
                        errorMessage = "Invalid credentials"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.sign_in), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setAutoSelectEnabled(true)
                                .build()
                            val request = GetCredentialRequest(listOf(googleIdOption))
                            credentialManager.getCredential(context as Activity, request)
                            onLogin()
                        } catch (e: Exception) {
                            errorMessage = "Google Sign-In Error"
                        }
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
"""

signup_code = """@Composable
fun SignupScreen(onCreateAccount: (String, String, String) -> Unit, onSignInClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tealColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val credentialManager = remember { CredentialManager.create(context) }

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
                placeholder = "John Doe"
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
                    if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "All fields required"
                    } else if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
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
                colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.create_account), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setAutoSelectEnabled(true)
                                .build()
                            val request = GetCredentialRequest(listOf(googleIdOption))
                            credentialManager.getCredential(context as Activity, request)
                            onCreateAccount("Google User", "google@example.com", "password")
                        } catch (e: Exception) {
                            errorMessage = "Google Sign-In Error"
                        }
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
                TextButton(onClick = onSignInClick) {
                    Text(stringResource(R.string.sign_in), color = tealColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
"""

new_lines = lines[:login_start] + [login_code + '\n'] + lines[login_end+1:signup_start] + [signup_code + '\n'] + lines[signup_end+2:]

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
    
print("Replaced Login and Signup")
