import requests

payload = {
    "age": 21,
    "gender": "Male",
    "mobile_number": "7013995242",
    "occupation": "Student",
    "marital_status": "Single",
    "sleep_duration": 8.3,
    "sleep_quality": 5,
    "wake_up_time": "06:20",
    "bed_time": "22:00",
    "physical_activity": 1,
    "screen_time": 4.0,
    "caffeine_intake": 0,
    "alcohol_intake": "0",
    "smoking_habit": "None",
    "work_hours": 7.0,
    "travel_time": 0.75,
    "social_interactions": 3,
    "meditation_practice": "0",
    "exercise_type": "Walking",
    "blood_pressure": 120,
    "blood_sugar_level": 140,
    "mood": "Neutral",
    "anxiety": "Low",
    "caffeine_dependency": "No",
    "workload": "Light",
    "body_feeling": "Normal"
}

try:
    print("Testing payload...")
    response = requests.post("https://cortisense-backend.onrender.com/api/checkins/stress-checkin", json=payload)
    print("Status:", response.status_code)
    print("Response:", response.json())
except Exception as e:
    print("Error:", e)
