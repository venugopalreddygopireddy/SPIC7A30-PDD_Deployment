import os

file_path = 'app/src/main/java/com/cortisense/app/ProfileScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

imports = '''import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
'''

if 'import coil.compose.AsyncImage' not in content:
    content = content.replace('package com.cortisense.app', 'package com.cortisense.app\n\n' + imports)

# Find where to add initialImageUri and photoPickerLauncher
target_1 = '    val initialGoal by viewModel.userGoal.collectAsState()'
replace_1 = target_1 + '''
    val initialImageUri by viewModel.profileImageUri.collectAsState()
    var imageUri by remember(initialImageUri) { mutableStateOf(initialImageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> if (uri != null) imageUri = uri.toString() }
    )
'''
content = content.replace(target_1, replace_1)

# Find the save button updateProfile
target_2 = 'viewModel.updateProfile(name, age, gender, goal, "")'
replace_2 = 'viewModel.updateProfile(name, age, gender, goal, imageUri)'
content = content.replace(target_2, replace_2)

# Now find the Name Field, and insert the profile picture UI above it
target_3 = '            // Name Field'
replace_3 = '''
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
                    Text("Change Photo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Field'''
content = content.replace(target_3, replace_3)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print('Updated ProfileScreen.kt')
