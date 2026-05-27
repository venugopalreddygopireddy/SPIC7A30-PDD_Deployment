from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
import onnxruntime as ort
import numpy as np
import pandas as pd
import joblib
import os
from typing import List

# Database Imports
import models
import schemas
import crud

from database import engine, get_db

from sqlalchemy import text
from sqlalchemy.engine.reflection import Inspector

models.Base.metadata.create_all(bind=engine)
print(models.Base.metadata.tables.keys())

try:
    with engine.connect() as conn:
        conn.execute(text("SELECT 1"))
        print("PostgreSQL Connected Successfully")
        
        # Log active database connection details (mask password)
        raw_url = str(engine.url)
        masked_url = raw_url
        if ':' in raw_url and '@' in raw_url:
            parts = raw_url.split('@')
            auth_parts = parts[0].split(':')
            if len(auth_parts) >= 3:
                masked_url = f"{auth_parts[0]}:{auth_parts[1]}:****@{parts[1]}"
        
        print(f"--- DATABASE CONNECTION INFO ---")
        print(f"URL: {masked_url}")
        print(f"Host: {engine.url.host}")
        print(f"Database Name: {engine.url.database}")
        print(f"Schema: public (default)")
        print(f"Active Table: stress_checkins")
        print(f"--------------------------------")
        
        # Perform lightweight migration to add any missing columns (e.g. the 25 new fields)
        inspector = Inspector.from_engine(engine)
        if 'stress_checkins' in inspector.get_table_names():
            columns = [col['name'] for col in inspector.get_columns('stress_checkins')]
            
            for column in models.StressCheckIn.__table__.columns:
                if column.name not in columns:
                    try:
                        col_type = column.type.compile(engine.dialect)
                        conn.execute(text(f"ALTER TABLE stress_checkins ADD COLUMN {column.name} {col_type}"))
                        print(f"Added missing column: {column.name}")
                    except Exception as col_e:
                        print(f"Failed to add column {column.name}: {col_e}")
            
            try:
                conn.commit()
            except AttributeError:
                pass # SQLAlchemy 1.4 connection doesn't always need explicit commit like 2.0
except Exception as e:
    print(f"Error connecting to PostgreSQL: {e}")

# ============================================
# FASTAPI APP
# ============================================

app = FastAPI(title="CortiSense AI Backend")

# Add CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================
# LOAD AI MODEL (ONNX)
# ============================================

MODEL_PATH = "stress_model.onnx"
ENCODER_PATH = "encoders.pkl"
SCALER_PATH = "scaler.pkl"
TARGET_ENCODER_PATH = "target_encoder.pkl"

if os.path.exists(MODEL_PATH):
    # Load ONNX session
    session = ort.InferenceSession(MODEL_PATH)
    input_name = session.get_inputs()[0].name
    
    label_encoders = joblib.load(ENCODER_PATH)
    scaler = joblib.load(SCALER_PATH)
    target_encoder = joblib.load(TARGET_ENCODER_PATH)
    print("AI ONNX Model Loaded Successfully")
else:
    print(f"WARNING: AI model file {MODEL_PATH} not found")


# ============================================
# HOME ROUTE
# ============================================

@app.get("/")
def home():

    return {
        "message": "CortiSense AI + PostgreSQL Backend Running"
    }


# ============================================
# CHECK-IN API
# ============================================

@app.post("/checkin", response_model=schemas.AIAnalysisResult)
def analyze_stress(
    data: schemas.CheckInRequest,
    db: Session = Depends(get_db)
):

    # ========================================
    # PREPARE INPUT DATA
    # ========================================

    input_data = {

        "Age": data.age,
        "Gender": data.gender,
        "Occupation": data.occupation,
        "Marital_Status": data.marital_status,

        "Sleep_Duration": data.sleep_duration,
        "Sleep_Quality": data.sleep_quality,

        "Wake_Up_Time": data.wake_up_time,
        "Bed_Time": data.bed_time,

        "Physical_Activity": data.physical_activity,
        "Screen_Time": data.screen_time,

        "Caffeine_Intake": data.caffeine_intake,
        "Alcohol_Intake": data.alcohol_intake,
        "Smoking_Habit": data.smoking_habit,

        "Work_Hours": data.work_hours,
        "Travel_Time": data.travel_time,

        "Social_Interactions": data.social_interactions,

        "Meditation_Practice": data.meditation_practice,
        "Exercise_Type": data.exercise_type,

        "Blood_Pressure": data.blood_pressure,
        "Blood_Sugar_Level": data.blood_sugar_level,

        "mood": data.mood,
        "anxiety": data.anxiety,
        "caffeine_dependency": data.caffeine_dependency,
        "workload": data.workload,
        "body_feeling": data.body_feeling
    }

    # ========================================
    # CREATE DATAFRAME
    # ========================================

    df = pd.DataFrame([input_data])

    # ========================================
    # COLUMN ORDER
    # ========================================

    training_columns = [

        "Age",
        "Gender",
        "Occupation",
        "Marital_Status",

        "Sleep_Duration",
        "Sleep_Quality",

        "Wake_Up_Time",
        "Bed_Time",

        "Physical_Activity",
        "Screen_Time",

        "Caffeine_Intake",
        "Alcohol_Intake",
        "Smoking_Habit",

        "Work_Hours",
        "Travel_Time",

        "Social_Interactions",

        "Meditation_Practice",
        "Exercise_Type",

        "Blood_Pressure",
        "Blood_Sugar_Level",

        "mood",
        "anxiety",
        "caffeine_dependency",
        "workload",
        "body_feeling"
    ]

    df = df[training_columns]

    # ========================================
    # ENCODING
    # ========================================

    for column in df.columns:

        if column in label_encoders:

            le = label_encoders[column]

            try:

                df[column] = le.transform(
                    df[column].astype(str)
                )

            except:

                df[column] = 0

    # ========================================
    # SCALING
    # ========================================

    scaled_input = scaler.transform(df)

    # ========================================
    # PREDICTION (ONNX)
    # ========================================

    ort_inputs = {input_name: scaled_input.astype(np.float32)}
    prediction = session.run(None, ort_inputs)[0]

    predicted_class = np.argmax(prediction)

    confidence = float(np.max(prediction))

    stress_level = str(target_encoder.inverse_transform(
        [predicted_class]
    )[0])

    # ========================================
    # RECOMMENDATIONS
    # ========================================

    recommendations = {

        "Low":
            "Maintain your current healthy routines and continue mindfulness practices.",

        "Moderate":
            "Moderate stress detected. Take regular breaks and improve sleep quality.",

        "High":
            "High stress detected. Reduce workload and prioritize mental recovery.",

        "Critical":
            "Critical stress detected. Seek immediate support and take complete rest."
    }

    # ========================================
    # FINAL RESPONSE
    # ========================================

    analysis_result = {

        "stress_level": stress_level,

        "score": int(confidence * 100),

        "message":
            f"AI Stress Analysis Complete: {stress_level} detected.",

        "recommendation":
            recommendations.get(
                stress_level,
                "Please take care of your health."
            ),

        "is_escalated":
            bool(predicted_class >= 2)
    }

    # ========================================
    # SAVE TO POSTGRESQL
    # ========================================

    try:
        crud.create_stress_checkin(
            db,
            data,
            analysis_result
        )
        
        from sqlalchemy import text
        row_count = db.execute(text("SELECT COUNT(*) FROM stress_checkins")).scalar()
        print(f"Check-in saved successfully to PostgreSQL. Current row count in stress_checkins: {row_count}")
        
    except Exception as e:
        print("Database Save Error:", e)
        db.rollback()

    # ========================================
    # RETURN RESPONSE
    # ========================================

    return analysis_result


# ============================================
# HISTORY API
# ============================================

@app.get(
    "/history",
    response_model=List[schemas.StressCheckInResponse]
)
def read_history(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):

    history = crud.get_checkins(
        db,
        skip=skip,
        limit=limit
    )

    return history


# ============================================
# SINGLE HISTORY API
# ============================================

@app.get(
    "/history/{checkin_id}",
    response_model=schemas.StressCheckInResponse
)
def read_checkin(
    checkin_id: int,
    db: Session = Depends(get_db)
):

    db_checkin = crud.get_checkin_by_id(
        db,
        checkin_id
    )

    if db_checkin is None:

        raise HTTPException(
            status_code=404,
            detail="Check-in record not found"
        )

    return db_checkin


# ============================================
# TEMPORARY DEBUG API
# ============================================

@app.get("/debug/checkins")
def debug_checkins(db: Session = Depends(get_db)):
    """Temporary endpoint to verify PostgreSQL contents directly."""
    recent_checkins = db.query(models.StressCheckIn).order_by(models.StressCheckIn.timestamp.desc()).limit(10).all()
    
    return [
        {
            "id": c.id,
            "name": "User",  # 'name' column does not exist in models.StressCheckIn
            "stress_level": c.stress_level,
            "created_at": c.timestamp
        }
        for c in recent_checkins
    ]