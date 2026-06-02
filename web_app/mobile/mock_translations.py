import xml.etree.ElementTree as ET
import os

main_strings = 'app/src/main/res/values/strings.xml'
tree = ET.parse(main_strings)
root = tree.getroot()

locales = {
    'fr': '[FR] ',
    'hi': '[HI] ',
    'te': '[TE] ',
    'ta': '[TA] '
}

for lang, prefix in locales.items():
    lang_file = f'app/src/main/res/values-{lang}/strings.xml'
    if not os.path.exists(os.path.dirname(lang_file)):
        os.makedirs(os.path.dirname(lang_file))
    
    if os.path.exists(lang_file):
        lang_tree = ET.parse(lang_file)
        lang_root = lang_tree.getroot()
        existing = {child.attrib['name'] for child in lang_root if 'name' in child.attrib}
    else:
        lang_root = ET.Element('resources')
        existing = set()
    
    for child in root:
        if 'name' in child.attrib:
            name = child.attrib['name']
            if name not in existing:
                new_element = ET.SubElement(lang_root, 'string', attrib={'name': name})
                if child.text:
                    new_element.text = prefix + child.text
                else:
                    new_element.text = prefix
    
    # Save formatted XML
    xml_str = ET.tostring(lang_root, encoding='utf-8', xml_declaration=True).decode('utf-8')
    with open(lang_file, 'w', encoding='utf-8') as f:
        f.write(xml_str.replace('><', '>\n    <').replace('</resources>', '\n</resources>'))

print("Translations mocked.")
