import re
import json

with open('app/src/main/java/com/cortisense/app/MainActivity.kt', 'r', encoding='utf-8') as f:
    text = f.read()

strings = re.findall(r'Text\(\s*"(.*?)"', text)
strings += re.findall(r'Text\(text\s*=\s*"(.*?)"', text)
strings += re.findall(r'ProfileMenuItem\([^\,]+,\s*"(.*?)"', text)
strings += re.findall(r'SettingToggleItem\([^\,]+,\s*"(.*?)"', text)
strings += re.findall(r'Button\([^)]*Text\(\s*"(.*?)"', text)
strings += re.findall(r'TextButton\([^)]*Text\(\s*"(.*?)"', text)
strings += re.findall(r'AlertDialog\([^)]*title\s*=\s*\{\s*Text\("(.*?)"\)', text)

unique_strings = sorted(set([s for s in strings if s.strip() and '{' not in s and '$' not in s]))

with open('strings_to_translate.json', 'w', encoding='utf-8') as f:
    json.dump(unique_strings, f, ensure_ascii=False, indent=2)
