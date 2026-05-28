from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from database import Base
from datetime import datetime

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    first_name = Column(String)
    last_name = Column(String)
    email = Column(String, unique=True, index=True)
    hashed_password = Column(String)
    age = Column(Integer)
    gender = Column(String)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    reset_otp = Column(String, nullable=True)
    reset_otp_expires_at = Column(DateTime, nullable=True)

    checkins = relationship("StressCheckIn", back_populates="user")

class StressCheckIn(Base):

    __tablename__ = "stress_checkins"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))

    timestamp = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="checkins")

    # ====================================
    # AI RESULTS
    # ====================================

    stress_level = Column(String)

    score = Column(Integer)

    recommendation = Column(String)

    is_escalated = Column(Boolean)

    # ====================================
    # USER DATA
    # ====================================

    age = Column(Integer)

    gender = Column(String)

    occupation = Column(String)

    marital_status = Column(String)

    sleep_duration = Column(Float)

    sleep_quality = Column(Integer)

    wake_up_time = Column(String)

    bed_time = Column(String)

    physical_activity = Column(Integer)

    screen_time = Column(Float)

    caffeine_intake = Column(Integer)

    alcohol_intake = Column(String)

    smoking_habit = Column(String)

    work_hours = Column(Float)

    travel_time = Column(Integer)

    social_interactions = Column(Integer)

    meditation_practice = Column(String)

    exercise_type = Column(String)

    blood_pressure = Column(Integer)

    blood_sugar_level = Column(Integer)

    mood = Column(String)

    anxiety = Column(String)

    caffeine_dependency = Column(String)

    workload = Column(String)

    body_feeling = Column(String)