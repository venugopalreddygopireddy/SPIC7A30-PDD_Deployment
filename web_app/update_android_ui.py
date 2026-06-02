import re

with open('mobile/app/src/main/java/com/cortisense/app/ProfileScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove image handling
content = re.sub(r'val initialImageUri.*?mutableStateOf.*?\(initialImageUri\)', '', content, flags=re.DOTALL)
content = re.sub(r'val photoPickerLauncher.*?\}', '', content, flags=re.DOTALL)

# Update profile picture UI to static logo
old_profile_ui = """            // Profile Picture
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
            }"""

new_profile_ui = """            // Profile Picture (Static)
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(50.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(userEmail, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
            }"""

if 'Profile Picture (Static)' not in content:
    content = content.replace(old_profile_ui, new_profile_ui)

# Also update the updateProfile call
content = content.replace('viewModel.updateProfile(firstName, lastName, userEmail, dob, age, gender, goal, mobile, imageUri)', 'viewModel.updateProfile(firstName, lastName, userEmail, dob, age, gender, goal, mobile, "")')

with open('mobile/app/src/main/java/com/cortisense/app/ProfileScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
