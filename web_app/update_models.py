import re

with open('backend/models.py', 'r', encoding='utf-8') as f:
    content = f.read()

old_fields = """    gender = Column(String)
    created_at = Column(DateTime, default=datetime.utcnow)"""

new_fields = """    gender = Column(String)
    mobile_number = Column(String, nullable=True)
    dob = Column(String, nullable=True)
    goal = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)"""

if 'mobile_number' not in content[:content.find('class StressCheckIn')]:
    content = content.replace(old_fields, new_fields)

with open('backend/models.py', 'w', encoding='utf-8') as f:
    f.write(content)
