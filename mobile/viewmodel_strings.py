import os

file_path = 'app/src/main/java/com/cortisense/app/MainViewModel.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    '"I\\'m having trouble connecting. Please try again."': 'getApplication<Application>().getString(R.string.chat_error)',
    '"Based on your latest analysis, your stress is $stressLevel. " +\n                    if (stressLevel != "Low Stress") "I recommend some deep breathing exercises." else "You\\'re doing great!"': 'getApplication<Application>().getString(R.string.chat_stress_reply, stressLevel, if (stressLevel != "Low Stress") getApplication<Application>().getString(R.string.chat_stress_high) else getApplication<Application>().getString(R.string.chat_stress_low))',
    '"Your average sleep is $sleepHours. Try to aim for 7-9 hours for better cortisol regulation."': 'getApplication<Application>().getString(R.string.chat_sleep_reply, sleepHours)',
    '"Hello! I\\'m your CortiSense assistant. How can I help you today?"': 'getApplication<Application>().getString(R.string.chat_hello)',
    '"I understand. Managing stress is important. Would you like to see your latest trends or try a breathing exercise?"': 'getApplication<Application>().getString(R.string.chat_default)'
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("ViewModel strings replaced")
