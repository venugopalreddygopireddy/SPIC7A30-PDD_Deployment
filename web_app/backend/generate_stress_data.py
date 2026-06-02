import pandas as pd
import numpy as np
import random

# Seed for reproducibility
np.random.seed(42)
random.seed(42)

def generate_realistic_data(n_samples=5000):
    data = []
    
    occupations = ["Engineer", "Student", "Healthcare", "Teacher", "Business", "Artist", "IT Professional", "Unemployed"]
    genders = ["Male", "Female", "Other"]
    marital_statuses = ["Single", "Married", "Divorced"]
    smoking_habits = ["Non-smoker", "Occasional", "Frequent"]
    exercise_types = ["None", "Gym", "Yoga", "Cardio", "Walking"]
    moods = ["Happy", "Calm", "Neutral", "Sad", "Depressed", "Anxious"]
    anxiety_levels = ["Low", "Mild", "Moderate", "High", "Extreme"]
    body_feelings = ["Relaxed", "Normal", "Tired", "Exhausted", "Burnout"]
    caffeine_dependencies = ["No", "Slight", "Yes"]
    workload_levels = ["Light", "Normal", "Moderate", "Heavy", "Extreme"]
    blood_pressures = ["110/70", "120/80", "130/85", "140/90", "150/95"]

    for _ in range(n_samples):
        # 1. Personal Info
        age = np.random.randint(18, 65)
        gender = random.choice(genders)
        occupation = random.choice(occupations)
        marital_status = random.choice(marital_statuses)

        # 2. Sleep (Base: 7 hours, Quality 3)
        sleep_duration = np.random.normal(7, 1.5)
        sleep_duration = np.clip(sleep_duration, 3, 11)
        sleep_quality = np.random.randint(1, 6)
        
        # 3. Habits
        physical_activity = np.random.randint(1, 6)
        screen_time = np.random.uniform(2, 12)
        caffeine_intake = np.random.randint(0, 6)
        alcohol_intake = np.random.randint(0, 4)
        smoking_habit = random.choice(smoking_habits)
        
        # 4. Work/Routine
        work_hours = np.random.uniform(4, 12)
        travel_time = np.random.uniform(0.5, 3)
        social_interactions = np.random.randint(1, 6)
        workload = random.choice(workload_levels)
        
        # 5. Wellness
        meditation = np.random.randint(0, 45)
        exercise_type = random.choice(exercise_types)
        blood_pressure = random.choice(blood_pressures)
        blood_sugar = np.random.randint(80, 160)
        
        # 6. Mental State
        mood = random.choice(moods)
        anxiety = random.choice(anxiety_levels)
        body_feeling = random.choice(body_feelings)
        caffeine_dependency = random.choice(caffeine_dependencies)

        # --- CALCULATE STRESS SCORE (WITH OVERLAP) ---
        score = 0
        
        # Weighted factors
        score += (8 - sleep_duration) * 5  # Less sleep = more stress
        score += (5 - sleep_quality) * 4   # Poor quality = more stress
        score += (work_hours - 8) * 3      # Long hours = more stress
        score += screen_time * 1.5
        score += (6 - physical_activity) * 2
        score += (6 - social_interactions) * 2
        
        # Categorical Weights
        if workload in ["Heavy", "Extreme"]: score += 15
        if anxiety in ["High", "Extreme"]: score += 20
        if body_feeling in ["Exhausted", "Burnout"]: score += 18
        if mood in ["Sad", "Depressed", "Anxious"]: score += 12
        
        # Mitigating factors (Meditation, Exercise)
        score -= (meditation / 10) * 3
        if exercise_type != "None": score -= 5
        
        # ADD NOISE (Balanced for 70-90% accuracy)
        noise = np.random.normal(0, 7) # Reduced variance
        total_score = score + noise
        
        # Map to classes with overlaps
        if total_score < 15:
            stress_label = "Low"
        elif total_score < 40:
            stress_label = "Moderate"
        elif total_score < 65:
            stress_label = "High"
        else:
            stress_label = "Critical"

        # Force some "Low" stress cases to have "Bad" traits occasionally
        if random.random() < 0.05: # 5% outliers
            stress_label = random.choice(["Low", "Moderate", "High", "Critical"])

        data.append([
            age, gender, occupation, marital_status,
            round(sleep_duration, 1), sleep_quality, "07:00", "23:00",
            physical_activity, round(screen_time, 1), caffeine_intake,
            alcohol_intake, smoking_habit, round(work_hours, 1),
            round(travel_time, 1), social_interactions, meditation,
            exercise_type, blood_pressure, blood_sugar,
            mood, anxiety, caffeine_dependency, workload, body_feeling,
            stress_label
        ])

    columns = [
        "Age", "Gender", "Occupation", "Marital_Status",
        "Sleep_Duration", "Sleep_Quality", "Wake_Up_Time", "Bed_Time",
        "Physical_Activity", "Screen_Time", "Caffeine_Intake",
        "Alcohol_Intake", "Smoking_Habit", "Work_Hours",
        "Travel_Time", "Social_Interactions", "Meditation_Practice",
        "Exercise_Type", "Blood_Pressure", "Blood_Sugar_Level",
        "mood", "anxiety", "caffeine_dependency", "workload", "body_feeling",
        "Stress_Detection"
    ]
    
    df = pd.DataFrame(data, columns=columns)
    df.to_excel("improved_stress_dataset.xlsx", index=False)
    print(f"Realistic dataset generated with {n_samples} samples.")
    print("Stress Level Distribution:")
    print(df["Stress_Detection"].value_counts())

if __name__ == "__main__":
    generate_realistic_data()
