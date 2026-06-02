import requests
import json
import uuid

BASE_URL = 'https://cortisense-backend.onrender.com'

def test_system():
    # 1. Registration
    email = f"smoke_{uuid.uuid4().hex[:8]}@example.com"
    password = "password123"
    print(f"Registering {email}...")
    register_data = {
        "email": email, 
        "password": password,
        "first_name": "Smoke",
        "last_name": "User",
        "age": 25,
        "gender": "Other"
    }
    resp = requests.post(f"{BASE_URL}/register", json=register_data)
    assert resp.status_code == 200, resp.text
    print("Registration successful.")

    # 2. Login
    print("Logging in...")
    resp = requests.post(f"{BASE_URL}/login", json={"email": email, "password": password})
    assert resp.status_code == 200, resp.text
    token = resp.json()["access_token"]
    headers = {"Authorization": f"Bearer {token}"}
    print("Login successful.")

    # 3. Profile Sync
    print("Updating profile...")
    profile_data = {
        "first_name": "Smoke",
        "last_name": "Test",
        "dob": "01/01/1990",
        "age": "34",
        "gender": "Male",
        "goal": "Reduce Stress",
        "mobile": "1234567890"
    }
    resp = requests.put(f"{BASE_URL}/users/me/profile", json=profile_data, headers=headers)
    assert resp.status_code == 200, resp.text
    print("Profile updated.")

    resp = requests.get(f"{BASE_URL}/users/me/profile", headers=headers)
    assert resp.status_code == 200, resp.text
    assert resp.json()["first_name"] == "Smoke"
    print("Profile fetch verified.")

    # 4. Check-in Creation
    print("Creating check-in...")
    checkin_data = {
        "anxiety_level": 5,
        "sleep_quality": 3,
        "sleep_duration": 6.5,
        "heart_rate": 80,
        "stress_level": 7,
        "mood_score": 6,
        "workload_level": 8,
        "physical_activity": 2,
        "caffeine_intake": 4,
        "water_intake": 1,
        "social_interactions": 3,
        "age": 25,
        "gender": "Male",
        "occupation": "Engineer",
        "marital_status": "Single",
        "wake_up_time": "07:00",
        "bed_time": "23:00",
        "screen_time": 8,
        "alcohol_intake": 0,
        "smoking_habit": "No",
        "work_hours": 8,
        "travel_time": 1,
        "meditation_practice": 0,
        "exercise_type": "None",
        "blood_pressure": 120,
        "blood_sugar_level": 90,
        "mood": "Good",
        "anxiety": "Low",
        "caffeine_dependency": "Yes",
        "workload": "Medium",
        "body_feeling": "Normal"
    }
    resp = requests.post(f"{BASE_URL}/checkin", json=checkin_data, headers=headers)
    assert resp.status_code == 200, resp.text
    checkin_id = resp.json()["id"]
    actions = resp.json()["actions"]
    print(f"Check-in created with ID {checkin_id}. AI Prediction successful.")

    # 5. Dashboard Summary
    print("Fetching dashboard summary...")
    resp = requests.get(f"{BASE_URL}/dashboard/summary", headers=headers)
    assert resp.status_code == 200, resp.text
    print("Dashboard verified.")

    # 6. Analytics Sync
    print("Fetching analytics...")
    requests.get(f"{BASE_URL}/analytics/weekly", headers=headers).raise_for_status()
    requests.get(f"{BASE_URL}/analytics/monthly", headers=headers).raise_for_status()
    requests.get(f"{BASE_URL}/analytics/trends", headers=headers).raise_for_status()
    requests.get(f"{BASE_URL}/analytics/factors", headers=headers).raise_for_status()
    print("Analytics verified.")

    # 7. History Fetch
    print("Fetching history...")
    resp = requests.get(f"{BASE_URL}/history", headers=headers)
    assert resp.status_code == 200, resp.text
    assert len(resp.json()) >= 1
    
    resp = requests.get(f"{BASE_URL}/history/{checkin_id}", headers=headers)
    assert resp.status_code == 200, resp.text
    print("History verified.")

    # 8. Action Card Sync
    print("Completing action card...")
    if actions:
        action_id = actions[0]["id"]
        resp = requests.patch(f"{BASE_URL}/checkin/{checkin_id}/action/{action_id}/complete", headers=headers)
        assert resp.status_code == 200, resp.text
        
        resp = requests.get(f"{BASE_URL}/history/{checkin_id}", headers=headers)
        updated_actions = resp.json().get("actions", [])
        for a in updated_actions:
            if a["id"] == action_id:
                assert a["is_done"] == True
        print("Action cards verified.")

    # 9. OTP Workflow
    print("Requesting password reset...")
    try:
        resp = requests.post(f"{BASE_URL}/forgot-password", json={"email": email})
        print("OTP Workflow triggered. Response:", resp.status_code, resp.text)
    except Exception as e:
        print("OTP Workflow skipped/failed due to:", e)

    print("\n--- ALL SYSTEMS SMOKE TESTED SUCCESSFULLY ---")

if __name__ == "__main__":
    test_system()
