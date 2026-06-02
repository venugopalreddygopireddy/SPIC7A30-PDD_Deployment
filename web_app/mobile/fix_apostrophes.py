import os
import re

dirs = ['values', 'values-fr', 'values-hi', 'values-ta', 'values-te']

for d in dirs:
    file_path = f'app/src/main/res/{d}/strings.xml'
    if not os.path.exists(file_path):
        continue
        
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # We want to replace ' with \' inside text nodes.
    # Regex to match content between > and <
    # Be careful not to replace \' if it's already escaped.
    
    def escape_apostrophes(match):
        text = match.group(1)
        # replace ' with \' only if not already preceded by \
        # this negative lookbehind (?<!\\) checks for no backslash
        text = re.sub(r"(?<!\\)'", r"\'", text)
        return f'>{text}<'

    new_content = re.sub(r'>([^<]+)<', escape_apostrophes, content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
        
print("Apostrophes escaped.")
