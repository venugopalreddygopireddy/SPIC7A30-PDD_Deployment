from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime
from database import Base
from datetime import datetime


class StressCheckIn(Base):

    __tablename__ = "stress_checkins"

    id = Column(Integer, primary_key=True, index=True)

    timestamp = Column(DateTime, default=datetime.utcnow)

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