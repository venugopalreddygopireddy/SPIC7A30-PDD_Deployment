from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
import onnxruntime as ort
import numpy as np
import pandas as pd
import joblib
import os
import random
from typing import List
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail
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

def upgrade_schema():
    try:
        from sqlalchemy.sql import text
        with engine.connect() as conn:
            # Attempt to add new columns if they don't exist. Ignore errors if they do.
            statements = [
                "ALTER TABLE stress_checkins ADD COLUMN caffeine_dependency VARCHAR DEFAULT 'No'",
                "ALTER TABLE stress_checkins ADD COLUMN workload VARCHAR DEFAULT 'Normal'",
                "ALTER TABLE stress_checkins ADD COLUMN body_feeling VARCHAR DEFAULT 'Normal'"
            ]
            for stmt in statements:
                try:
                    conn.execute(text(stmt))
                    conn.commit()
                except Exception:
                    conn.rollback() # Rollback if column already exists
    except Exception as e:
        pass

upgrade_schema()

try:
    with engine.connect() as conn:
        conn.execute(text("SELECT 1"))
        print("PostgreSQL Connected Successfully")
        
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
        if 'users' in inspector.get_table_names():
            columns = [col['name'] for col in inspector.get_columns('users')]
            for column in models.User.__table__.columns:
                if column.name not in columns:
                    try:
                        col_type = column.type.compile(engine.dialect)
                        conn.execute(text(f"ALTER TABLE users ADD COLUMN {column.name} {col_type}"))
                    except Exception as col_e:
                        pass
                        
            
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
    if len(user.password) > 64:
        raise HTTPException(status_code=400, detail="Password cannot be longer than 64 characters")
        
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

def send_otp_email(to_email: str, otp: str):
    sg_api_key = os.getenv("SENDGRID_API_KEY")
    from_email = os.getenv("FROM_EMAIL")
    
    if not sg_api_key or not from_email:
        print("SMTP WARNING: SENDGRID_API_KEY or FROM_EMAIL env vars are not set.")
        return False
        
    try:
        print("SendGrid initialized")
        print("Sending OTP email via SendGrid...")
        
        message = Mail(
            from_email=from_email,
            to_emails=to_email,
            subject='CortiSense Password Reset OTP',
            plain_text_content=f"Your CortiSense password reset OTP is: {otp}\nIt expires in 15 minutes."
        )
        
        sg = SendGridAPIClient(sg_api_key)
        response = sg.send(message)
        print("SendGrid email sent successfully")
        return True
    except Exception as e:
        print(f"SendGrid Exception: {e}")
        return False

@app.post("/forgot-password")
def forgot_password(request: schemas.ForgotPasswordRequest, db: Session = Depends(get_db)):
    user = crud.get_user_by_email(db, email=request.email)
    if not user:
        raise HTTPException(status_code=404, detail="Email not found")
        
    otp = str(random.randint(100000, 999999))
    user.reset_otp = otp
    user.reset_otp_expires_at = datetime.utcnow() + timedelta(minutes=15)
    db.commit()
    
    success = send_otp_email(request.email, otp)
    if not success:
        # We still return 200, or maybe 500 depending on preference. Let's return 500 so the app knows it failed.
        raise HTTPException(status_code=500, detail="Failed to send OTP email")
    
    return {"message": "OTP sent successfully"}

@app.post("/verify-otp")
def verify_otp(request: schemas.VerifyOTPRequest, db: Session = Depends(get_db)):
    user = crud.get_user_by_email(db, email=request.email)
    if not user or user.reset_otp != request.otp:
        raise HTTPException(status_code=400, detail="Invalid OTP")
        
    if not user.reset_otp_expires_at or user.reset_otp_expires_at < datetime.utcnow():
        raise HTTPException(status_code=400, detail="OTP expired")
        
    return {"message": "OTP verified successfully"}

@app.post("/reset-password")
def reset_password(request: schemas.ResetPasswordRequest, db: Session = Depends(get_db)):
    user = crud.get_user_by_email(db, email=request.email)
    if not user or user.reset_otp != request.otp:
        raise HTTPException(status_code=400, detail="Invalid OTP")
        
    if not user.reset_otp_expires_at or user.reset_otp_expires_at < datetime.utcnow():
        raise HTTPException(status_code=400, detail="OTP expired")
        
    if len(request.new_password) > 64:
        raise HTTPException(status_code=400, detail="Password cannot be longer than 64 characters")
        
    user.hashed_password = get_password_hash(request.new_password)
    user.reset_otp = None
    user.reset_otp_expires_at = None
    db.commit()
    
    return {"message": "Password reset successful"}


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

# ============================================
# ANALYTICS APIs
# ============================================

from collections import Counter

@app.get("/analytics/weekly", response_model=schemas.WeeklyAnalyticsResponse)
def get_weekly_analytics(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    checkins = crud.get_checkins(db, user_id=current_user.id, limit=1000)
    week_ago = datetime.utcnow() - timedelta(days=7)
    recent = [c for c in checkins if c.timestamp >= week_ago]
    
    if not recent:
        return {"avg_score": 0, "highest_score": 0, "lowest_score": 0, "total_checkins": 0, "distribution": {"low": 0, "moderate": 0, "high": 0}}
        
    scores = [c.score for c in recent]
    distribution = {"low": 0, "moderate": 0, "high": 0}
    for c in recent:
        if "low" in c.stress_level.lower():
            distribution["low"] += 1
        elif "moderate" in c.stress_level.lower():
            distribution["moderate"] += 1
        else:
            distribution["high"] += 1
            
    return {
        "avg_score": sum(scores) // len(scores),
        "highest_score": max(scores),
        "lowest_score": min(scores),
        "total_checkins": len(recent),
        "distribution": distribution
    }

@app.get("/analytics/monthly", response_model=schemas.MonthlyAnalyticsResponse)
def get_monthly_analytics(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    checkins = crud.get_checkins(db, user_id=current_user.id, limit=1000)
    month_ago = datetime.utcnow() - timedelta(days=30)
    recent = [c for c in checkins if c.timestamp >= month_ago]
    
    if not recent:
        return {"avg_score": 0, "total_checkins": 0, "distribution": {"low": 0, "moderate": 0, "high": 0}, "calendar_activity": {}}
        
    scores = [c.score for c in recent]
    distribution = {"low": 0, "moderate": 0, "high": 0}
    calendar_activity = {}
    for c in recent:
        if "low" in c.stress_level.lower():
            distribution["low"] += 1
        elif "moderate" in c.stress_level.lower():
            distribution["moderate"] += 1
        else:
            distribution["high"] += 1
            
        date_str = c.timestamp.strftime("%Y-%m-%d")
        if date_str not in calendar_activity or calendar_activity[date_str] < c.score:
            calendar_activity[date_str] = c.score
            
    return {
        "avg_score": sum(scores) // len(scores),
        "total_checkins": len(recent),
        "distribution": distribution,
        "calendar_activity": calendar_activity
    }

@app.get("/analytics/trends", response_model=schemas.TrendsResponse)
def get_trends_analytics(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    checkins = crud.get_checkins(db, user_id=current_user.id, limit=1000)
    week_ago = datetime.utcnow() - timedelta(days=7)
    recent = [c for c in checkins if c.timestamp >= week_ago]
    
    daily_stats = {}
    for c in recent:
        date_str = c.timestamp.strftime("%Y-%m-%d")
        if date_str not in daily_stats:
            daily_stats[date_str] = {"scores": [], "levels": []}
        daily_stats[date_str]["scores"].append(c.score)
        daily_stats[date_str]["levels"].append(c.stress_level)
        
    trends = []
    for date_str, stats in daily_stats.items():
        avg = sum(stats["scores"]) // len(stats["scores"])
        most_common_level = max(set(stats["levels"]), key=stats["levels"].count)
        trends.append({"date": date_str, "score": avg, "level": most_common_level})
        
    trends.sort(key=lambda x: x["date"])
    return {"trends": trends}

@app.get("/analytics/factors", response_model=schemas.FactorsResponse)
def get_factors_analytics(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    checkins = crud.get_checkins(db, user_id=current_user.id, limit=1000)
    
    if not checkins:
        return {
            "sleep_avg": 0.0, "screen_time_avg": 0.0, "caffeine_avg": 0.0, "physical_activity_avg": 0.0,
            "top_mood": "None", "top_workload": "None", "top_exercise": "None"
        }
        
    sleep = [c.sleep_duration for c in checkins if c.sleep_duration is not None]
    screen = [c.screen_time for c in checkins if c.screen_time is not None]
    caffeine = [c.caffeine_intake for c in checkins if c.caffeine_intake is not None]
    physical = [c.physical_activity for c in checkins if c.physical_activity is not None]
    
    moods = [c.mood for c in checkins if c.mood]
    workloads = [c.workload for c in checkins if c.workload]
    exercises = [c.exercise_type for c in checkins if c.exercise_type]
    
    def avg(lst): return sum(lst) / len(lst) if lst else 0.0
    def top(lst): return Counter(lst).most_common(1)[0][0] if lst else "None"
    
    return {
        "sleep_avg": avg(sleep),
        "screen_time_avg": avg(screen),
        "caffeine_avg": avg(caffeine),
        "physical_activity_avg": avg(physical),
        "top_mood": top(moods),
        "top_workload": top(workloads),
        "top_exercise": top(exercises)
    }

@app.get("/dashboard/summary", response_model=schemas.DashboardSummaryResponse)
def get_dashboard_summary(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    checkins = crud.get_checkins(db, user_id=current_user.id, limit=10000)
    
    total_checkins = len(checkins)
    if not checkins:
        return {
            "total_checkins": 0,
            "latest_stress_score": 0,
            "latest_sleep_duration": 0.0,
            "latest_stress_category": "No Data",
            "current_streak": 0,
            "longest_streak": 0,
            "today_checkins_count": 0,
            "today_lowest_score": 0,
            "avg_stress_this_week": 0,
            "best_day_this_week": "None"
        }
    
    latest = checkins[0]
    
    # Calculate Streaks
    dates = sorted(list(set([c.timestamp.date() for c in checkins])), reverse=True)
    current_streak = 0
    longest_streak = 0
    temp_streak = 0
    prev_date = None
    
    import datetime
    today = datetime.datetime.utcnow().date()
    yesterday = today - datetime.timedelta(days=1)
    
    # Simple streak calculation
    if dates and (dates[0] == today or dates[0] == yesterday):
        current_streak = 1
        for i in range(1, len(dates)):
            if (dates[i-1] - dates[i]).days == 1:
                current_streak += 1
            else:
                break
    
    # Longest streak calculation
    if dates:
        temp_streak = 1
        longest_streak = 1
        for i in range(1, len(dates)):
            if (dates[i-1] - dates[i]).days == 1:
                temp_streak += 1
                if temp_streak > longest_streak:
                    longest_streak = temp_streak
            else:
                temp_streak = 1
                
    # Today stats
    today_checkins = [c for c in checkins if c.timestamp.date() == today]
    today_count = len(today_checkins)
    today_lowest = min([c.score for c in today_checkins]) if today_checkins else 0
    
    # This week stats
    one_week_ago = today - datetime.timedelta(days=7)
    week_checkins = [c for c in checkins if c.timestamp.date() > one_week_ago]
    avg_week = int(sum([c.score for c in week_checkins]) / len(week_checkins)) if week_checkins else 0
    
    best_day = "None"
    if week_checkins:
        best_c = min(week_checkins, key=lambda x: x.score)
        best_day = best_c.timestamp.strftime("%A")
        
    return {
        "total_checkins": total_checkins,
        "latest_stress_score": latest.score,
        "latest_sleep_duration": latest.sleep_duration or 0.0,
        "latest_stress_category": latest.stress_level,
        "current_streak": current_streak,
        "longest_streak": longest_streak,
        "today_checkins_count": today_count,
        "today_lowest_score": today_lowest,
        "avg_stress_this_week": avg_week,
        "best_day_this_week": best_day
    }