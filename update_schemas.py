import re

with open('backend/schemas.py', 'r', encoding='utf-8') as f:
    content = f.read()

old_fields = """    sleep_duration: Optional[float] = 0.0
    sleep_quality: Optional[int] = 0
    physical_activity: Optional[int] = 0
    screen_time: Optional[float] = 0.0
    workload: Optional[str] = "Normal"
    mood: Optional[str] = "Neutral"
    anxiety: Optional[str] = "Low"
    mobile_number: Optional[str] = ""

    class Config:"""

new_fields = """    sleep_duration: Optional[float] = 0.0
    sleep_quality: Optional[int] = 0
    physical_activity: Optional[int] = 0
    screen_time: Optional[float] = 0.0
    workload: Optional[str] = "Normal"
    mood: Optional[str] = "Neutral"
    anxiety: Optional[str] = "Low"
    mobile_number: Optional[str] = ""
    age: Optional[int] = 0
    gender: Optional[str] = ""
    occupation: Optional[str] = ""
    marital_status: Optional[str] = ""
    wake_up_time: Optional[str] = ""
    bed_time: Optional[str] = ""
    caffeine_intake: Optional[int] = 0
    alcohol_intake: Optional[str] = ""
    smoking_habit: Optional[str] = ""
    work_hours: Optional[float] = 0.0
    travel_time: Optional[int] = 0
    social_interactions: Optional[int] = 0
    meditation_practice: Optional[str] = ""
    exercise_type: Optional[str] = ""
    blood_pressure: Optional[int] = 0
    blood_sugar_level: Optional[int] = 0
    caffeine_dependency: Optional[str] = ""
    body_feeling: Optional[str] = ""

    class Config:"""

content = content.replace(old_fields, new_fields)

with open('backend/schemas.py', 'w', encoding='utf-8') as f:
    f.write(content)
