import re

with open('web/src/app/checkin/page.tsx', 'r', encoding='utf-8') as f:
    content = f.read()

icons = ["ArrowLeft", "ArrowRight", "Loader2", "CheckCircle2", "User", "Moon", "Zap", "Briefcase", "RunIcon", "Check", "BrainCircuit", "CloudLightning", "Heart", "AlertTriangle", "Activity"]
used_icons = []
for icon in icons:
    if re.search(r'<\s*' + icon + r'\b', content) or re.search(r'\b' + icon + r'\b', content[content.find('export default'):]):
        used_icons.append(icon)

if 'RunIcon' in used_icons:
    used_icons.remove('RunIcon')
    used_icons.append('Activity as RunIcon')

import_statement = "import { " + ", ".join(used_icons) + " } from 'lucide-react';"
content = re.sub(r'import\s+\{[^}]+\}\s+from\s+[\'"]lucide-react[\'"];', import_statement, content)

with open('web/src/app/checkin/page.tsx', 'w', encoding='utf-8') as f:
    f.write(content)

print("Cleaned up web imports")
