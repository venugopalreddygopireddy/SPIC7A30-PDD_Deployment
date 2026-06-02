import os

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

custom_text_field_code = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}
"""

for i, l in enumerate(lines):
    if 'fun LoginScreen(' in l:
        lines.insert(i-1, custom_text_field_code + '\n')
        break

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
