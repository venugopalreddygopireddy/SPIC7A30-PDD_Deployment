import requests

payload = {
    "age": 25,
    "gender": "Male",
    "occupation": "Software Engineer",
    "marital_status": "Single",
    "sleep_duration": 7.0,
    "sleep_quality": 3,
    "wake_up_time": "07:00",
    "bed_time": "23:00",
    "physical_activity": 3,
    "screen_time": 8.0,
    "caffeine_intake": 2,
    "alcohol_intake": "0",
    "smoking_habit": "None",
    "work_hours": 8.0,
    "travel_time": 1,
    "social_interactions": 3,
    "meditation_practice": "0",
    "exercise_type": "None",
    "blood_pressure": 120,
    "blood_sugar_level": 90,
    "mood": "Neutral",
    "anxiety": "Low",
    "caffeine_dependency": "No",
    "workload": "Normal",
    "body_feeling": "Normal"
}

# Login first to get token
login_data = {
    "email": "test@test.com", # Needs a valid user
    "password": "password123"
}
# We don't know the user's password. We can just create a test user.

url_register = "https://cortisense-backend.onrender.com/register"
res_reg = requests.post(url_register, json={
    "first_name": "Test",
    "last_name": "User",
    "age": 25,
    "gender": "Male",
    "email": "testagent@test.com",
    "password": "password123"
})
print("Reg:", res_reg.json())

if "access_token" in res_reg.json() or res_reg.status_code == 400:
    # login
    res_log = requests.post("https://cortisense-backend.onrender.com/login", json={
        "email": "testagent@test.com",
        "password": "password123"
    })
    token = res_log.json().get("access_token")
    print("Token:", token[:10] if token else None)
    
    if token:
        res_chk = requests.post(
            "https://cortisense-backend.onrender.com/checkin",
            json=payload,
            headers={"Authorization": f"Bearer {token}"}
        )
        print("Checkin Status:", res_chk.status_code)
        print("Checkin Body:", res_chk.json())
        
        # Now check history
        res_hist = requests.get(
            "https://cortisense-backend.onrender.com/history",
            headers={"Authorization": f"Bearer {token}"}
        )
        print("History Count:", len(res_hist.json()) if isinstance(res_hist.json(), list) else res_hist.json())
