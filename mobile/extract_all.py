import os
import re
import xml.etree.ElementTree as ET

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
strings_file = 'app/src/main/res/values/strings.xml'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

tree = ET.parse(strings_file)
root = tree.getroot()

existing_strings = set()
for child in root:
    if 'name' in child.attrib:
        existing_strings.add(child.attrib['name'])
        
def make_valid_id(text):
    text = re.sub(r'[^a-zA-Z0-9]+', '_', text).strip('_').lower()
    return "extracted_" + text[:20]

# Find Text("...") and Text(text = "...")
patterns = [
    r'Text\(\s*"([^"\\]+)"',
    r'Text\(\s*text\s*=\s*"([^"\\]+)"'
]

replacements = []
for p in patterns:
    matches = re.finditer(p, content)
    for m in matches:
        full_match = m.group(0)
        text_val = m.group(1)
        
        # skip if it's already a stringResource or empty
        if not text_val.strip() or 'stringResource' in text_val:
            continue
            
        string_id = make_valid_id(text_val)
        
        if string_id not in existing_strings:
            ET.SubElement(root, 'string', attrib={'name': string_id}).text = text_val
            existing_strings.add(string_id)
            
        if full_match.startswith('Text(text ='):
            new_code = f'Text(text = stringResource(R.string.{string_id})'
        else:
            new_code = f'Text(stringResource(R.string.{string_id})'
            
        replacements.append((full_match, new_code))

# sort by length descending to avoid partial matches
replacements.sort(key=lambda x: len(x[0]), reverse=True)

for old, new in replacements:
    content = content.replace(old, new)

# Also look for label = { Text("...") }
label_pattern = r'label\s*=\s*\{\s*Text\(\s*"([^"\\]+)"\s*\)\s*\}'
for m in re.finditer(label_pattern, content):
    full_match = m.group(0)
    text_val = m.group(1)
    if not text_val.strip(): continue
    string_id = make_valid_id(text_val)
    if string_id not in existing_strings:
        ET.SubElement(root, 'string', attrib={'name': string_id}).text = text_val
        existing_strings.add(string_id)
    new_code = f'label = {{ Text(stringResource(R.string.{string_id})) }}'
    content = content.replace(full_match, new_code)

# Save
xml_str = ET.tostring(root, encoding='utf-8', xml_declaration=True).decode('utf-8')
xml_str = xml_str.replace('><', '>\n    <').replace('</resources>', '\n</resources>')
with open(strings_file, 'w', encoding='utf-8') as f:
    f.write(xml_str)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print(f"Extracted {len(replacements)} strings.")
