import re

# 1. backend/main.py
with open('backend/main.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Add logging import if not present
if 'import logging' not in content:
    content = 'import logging\nlogging.basicConfig(level=logging.INFO)\nlogger = logging.getLogger(__name__)\n\n' + content

# Remove unused variables
content = re.sub(r'except Exception as e:\s+pass', r'except Exception:\n        pass', content)
content = re.sub(r'except Exception as col_e:\s+print\(f"Failed to add column \{column\.name\}: \{col_e\}"\)', r'except Exception as col_e:\n                        logger.error(f"Failed to add column {column.name}: {col_e}")', content)
content = re.sub(r'except Exception as col_e:\s+pass', r'except Exception:\n                        pass', content)
content = re.sub(r'response = sg\.send\(message\)', r'sg.send(message)', content)
# For `prev_date = date.today() - timedelta(days=7)`, it's assigned and never used. Let's just comment it out.
content = re.sub(r'prev_date = date.today\(\) - timedelta\(days=7\)', r'# prev_date = date.today() - timedelta(days=7)', content)

# Replace prints with logger
content = re.sub(r'print\("SMTP WARNING:(.*?)"\)', r'logger.warning("SMTP WARNING:\1")', content)
content = re.sub(r'print\(f"WARNING:(.*?)"\)', r'logger.warning(f"WARNING:\1")', content)
content = re.sub(r'print\("WARNING:(.*?)"\)', r'logger.warning("WARNING:\1")', content)
content = re.sub(r'print\("Error (.*?)"\)', r'logger.error("Error \1")', content)
content = re.sub(r'print\(f"Error (.*?)"\)', r'logger.error(f"Error \1")', content)
content = re.sub(r'print\("Database Save Error:",\s*e\)', r'logger.error(f"Database Save Error: {e}")', content)
content = re.sub(r'print\(f"SendGrid Exception: \{e\}"\)', r'logger.error(f"SendGrid Exception: {e}")', content)
content = re.sub(r'print\((.*?)\)', r'logger.info(\1)', content)

with open('backend/main.py', 'w', encoding='utf-8') as f:
    f.write(content)

# 2. backend/model.py
with open('backend/model.py', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'import torch\.optim\s*\n', '', content)
content = re.sub(r'import os\s*\n', '', content)

with open('backend/model.py', 'w', encoding='utf-8') as f:
    f.write(content)

# 3. backend/schemas.py
with open('backend/schemas.py', 'r', encoding='utf-8') as f:
    content = f.read()

content = re.sub(r'from pydantic import .*?EmailStr.*?\n', lambda m: m.group(0).replace('EmailStr, ', '').replace(', EmailStr', '').replace('EmailStr', ''), content)
content = re.sub(r'from typing import .*?Any.*?\n', lambda m: m.group(0).replace('Any, ', '').replace(', Any', '').replace('Any', ''), content)

with open('backend/schemas.py', 'w', encoding='utf-8') as f:
    f.write(content)

print("Python cleanup script ran successfully")
