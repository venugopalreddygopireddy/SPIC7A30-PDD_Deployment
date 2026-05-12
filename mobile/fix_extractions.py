import os
import re

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

reverts = {
    'stringResource(R.string.extracted_reason)': '"• $reason"',
    'stringResource(R.string.extracted_viewmodel_stressscor)': 'viewModel.stressScore.toString()',
    'stringResource(R.string.extracted_record_score)': 'record.score.toString()',
    'stringResource(R.string.extracted_d_index_1)': '"D${index+1}"',
    'stringResource(R.string.extracted_avgthisweek)': 'avgThisWeek.toString()',
    'stringResource(R.string.extracted_avgstress)': 'avgStress.toString()',
    'stringResource(R.string.extracted_score)': 'score.toString()',
    'stringResource(R.string.extracted_avgmonthly)': 'avgMonthly.toString()',
    'stringResource(R.string.extracted_percentage)': '"$percentage%"',
    'stringResource(R.string.extracted_day)': 'day.toString()',
    'stringResource(R.string.extracted_secondsleft)': 'secondsLeft.toString()',
    'stringResource(R.string.extracted_rounds)': 'rounds.toString()',
    'stringResource(R.string.extracted_number)': 'number.toString()',
    'stringResource(R.string.extracted_selectedtab_screen_c)': 'stringResource(R.string.extracted_selectedtab_screen_c, selectedTab)',
    'stringResource(R.string.extracted_history_take_5_size_)': 'stringResource(R.string.extracted_history_take_5_size_, history.take(5).size)',
    'stringResource(R.string.extracted_calculated_from_chec)': 'stringResource(R.string.extracted_calculated_from_chec, checkIns)',
    'stringResource(R.string.extracted_lowpercent_of_your_d)': 'stringResource(R.string.extracted_lowpercent_of_your_d, lowPercent)',
    'stringResource(R.string.extracted_days_days)': 'stringResource(R.string.extracted_days_days, days)',
    'stringResource(R.string.extracted_managing_primaryfact)': 'stringResource(R.string.extracted_managing_primaryfact, primaryFactor)',
    'stringResource(R.string.extracted_duration_toint_min)': 'stringResource(R.string.extracted_duration_toint_min, duration.toInt())',
}

for old, new in reverts.items():
    content = content.replace(old, new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

# Update strings.xml format arguments
strings_file = 'app/src/main/res/values/strings.xml'
with open(strings_file, 'r', encoding='utf-8') as f:
    s_content = f.read()

s_reverts = {
    '$selectedTab Screen Coming Soon': '%1$s Screen Coming Soon',
    '${history.take(5).size} entries': '%1$d entries',
    'Calculated from $checkIns entries': 'Calculated from %1$d entries',
    '$lowPercent% of your days': '%1$d%% of your days',
    '$days days': '%1$d days',
    'Managing $primaryFactor seems': 'Managing %1$s seems',
    '${duration.toInt()} min': '%1$d min'
}

for old, new in s_reverts.items():
    s_content = s_content.replace(old, new)

with open(strings_file, 'w', encoding='utf-8') as f:
    f.write(s_content)

print("Fixes applied.")
