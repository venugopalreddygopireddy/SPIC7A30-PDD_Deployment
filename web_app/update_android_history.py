import re

with open('mobile/app/src/main/java/com/cortisense/app/CheckInDetailScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add new imports
if 'import kotlinx.coroutines.launch' not in content:
    content = content.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp\nimport kotlinx.coroutines.launch\nimport androidx.compose.foundation.clickable\nimport androidx.compose.material.icons.filled.CheckCircle\nimport androidx.compose.material.icons.filled.CheckCircleOutline')

# Add state for actions and coroutine scope
old_state = """    var checkIn by remember { mutableStateOf<StressCheckInEntity?>(null) }
    
    LaunchedEffect(checkInId) {"""

new_state = """    var checkIn by remember { mutableStateOf<StressCheckInEntity?>(null) }
    var actions by remember { mutableStateOf<List<ActionItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(checkInId) {
        try {
            val apiData = RetrofitClient.instance.getHistoryById(checkInId.toInt())
            if (apiData.actions != null) {
                actions = apiData.actions.filter { !it.isDone }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
"""
if 'var actions by remember' not in content:
    content = content.replace(old_state, new_state)

# Render the actions
old_ai = """                // AI Recommendation
                Text("AI Recommendation", fontWeight = FontWeight.Bold, fontSize = 18.sp)"""

new_ai = """                if (actions.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Pending Actions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    actions.forEach { action ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2030)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = "Complete Action",
                                    tint = Color(0xFF5E9B72),
                                    modifier = Modifier.padding(top = 2.dp, end = 12.dp)
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(action.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(action.description, color = Color.LightGray, fontSize = 14.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    RetrofitClient.instance.completeAction(checkInId.toInt(), action.id)
                                                    actions = actions.filter { it.id != action.id }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E9B72).copy(alpha = 0.2f), contentColor = Color(0xFF5E9B72)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Mark as Done")
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // AI Recommendation
                Text("AI Recommendation", fontWeight = FontWeight.Bold, fontSize = 18.sp)"""

if 'Pending Actions' not in content:
    content = content.replace(old_ai, new_ai)

with open('mobile/app/src/main/java/com/cortisense/app/CheckInDetailScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
