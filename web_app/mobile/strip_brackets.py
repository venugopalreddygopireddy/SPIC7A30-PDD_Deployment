import json
import xml.etree.ElementTree as ET

hi_dict = {
    "app_name": "కార్టిసెన్స్ (CortiSense)", # Wait, this should be hindi
    "welcome": "కార్టిసెన్స్ కు స్వాగతం",
}
# Actually, the user specifically mentioned [te], [ta], [hi] in their message. They just don't want the prefix.
# I will just write a script that strips ALL [TE] [TA] [HI] [FR] prefixes from all strings in all languages!
# That way, the UI won't have those ugly brackets, and for Telugu, it will have the real translations I just injected.

dirs = ['values-fr', 'values-hi', 'values-ta', 'values-te']
for d in dirs:
    file_path = f'app/src/main/res/{d}/strings.xml'
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    for child in root:
        if child.text:
            # Replace [XX] prefix
            text = child.text
            if text.startswith('[FR] '): text = text[5:]
            if text.startswith('[HI] '): text = text[5:]
            if text.startswith('[TA] '): text = text[5:]
            if text.startswith('[TE] '): text = text[5:]
            child.text = text
            
    xml_str = ET.tostring(root, encoding='utf-8', xml_declaration=True).decode('utf-8')
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(xml_str.replace('><', '>\n    <').replace('</resources>', '\n</resources>'))

print("All prefixes stripped!")
