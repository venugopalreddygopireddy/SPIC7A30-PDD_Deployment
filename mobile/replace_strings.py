import os

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    'text = "Home"': 'text = stringResource(R.string.home)',
    'text = "Analytics"': 'text = stringResource(R.string.analytics)',
    'text = "Check-in"': 'text = stringResource(R.string.checkin)',
    'text = "AI Chat"': 'text = stringResource(R.string.ai_chat)',
    'text = "Profile"': 'text = stringResource(R.string.profile)',
    'text = "Settings"': 'text = stringResource(R.string.settings)',
    'text = "Language"': 'text = stringResource(R.string.language)',
    'text = "Dark Mode"': 'text = stringResource(R.string.dark_mode)',
    'text = "Log Out"': 'text = stringResource(R.string.logout)',
    'text = "Edit Profile"': 'text = stringResource(R.string.edit_profile)',
    'text = "Stress Score"': 'text = stringResource(R.string.stress_score)'
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Replacement complete.")
