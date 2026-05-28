from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
import onnxruntime as ort
import numpy as np
import pandas as pd
import joblib
import os
from typing import List
from passlib.context import CryptContext
import jwt
from datetime import datetime, timedelta
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

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

@app.on_event("startup")
async def startup_event():
    print("--- REGISTERED ROUTES ---")
    for route in app.routes:
        print(f"Path: {getattr(route, 'path', 'N/A')}, Name: {getattr(route, 'name', 'N/A')}, Methods: {getattr(route, 'methods', 'N/A')}")
    print("-------------------------")

# ============================================
# AUTHENTICATION LOGIC
# ============================================

SECRET_KEY = os.getenv("JWT_SECRET", "cortisense_super_secret_production_key_123!")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7 # 7 days

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
security = HTTPBearer()

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security), db: Session = Depends(get_db)):
    token = credentials.credentials
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    except jwt.PyJWTError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
    
    user = crud.get_user_by_email(db, email=email)
    if user is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
    return user


# ============================================
# AUTHENTICATION ROUTES
# ============================================

@app.post("/register", response_model=schemas.Token)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_email(db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    hashed_password = get_password_hash(user.password)
    new_user = crud.create_user(db=db, user=user, hashed_password=hashed_password)
    
    access_token = create_access_token(data={"sub": new_user.email})
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/login", response_model=schemas.Token)
def login(user_credentials: schemas.UserLogin, db: Session = Depends(get_db)):
    user = crud.get_user_by_email(db, email=user_credentials.email)
    if not user or not verify_password(user_credentials.password, user.hashed_password):
        raise HTTPException(status_code=401, detail="Invalid email or password")
    
    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}


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
def receive_checkin(
    data: schemas.CheckInRequest,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):

    print(f"Received check-in data from user {current_user.email}:", data)
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
            analysis_result,
            user_id=current_user.id
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
def get_history(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):

    checkins = crud.get_checkins(
        db,
        user_id=current_user.id,
        skip=skip,
        limit=limit
    )

    return checkins


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