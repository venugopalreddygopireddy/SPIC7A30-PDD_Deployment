from pydantic import BaseModel
from typing import Optional
from datetime import datetime


# =========================================================
# CHECK-IN REQUEST
# =========================================================

class CheckInRequest(BaseModel):

    age: int
    gender: str
    occupation: str
    marital_status: str

    sleep_duration: float
    sleep_quality: int

    wake_up_time: str
    bed_time: str

    physical_activity: int
    screen_time: float

    caffeine_intake: int
    alcohol_intake: str
    smoking_habit: str

    work_hours: float
    travel_time: int

    social_interactions: int

    meditation_practice: str
    exercise_type: str

    blood_pressure: int
    blood_sugar_level: int

    mood: str
    anxiety: str

    caffeine_dependency: str
    workload: str
    body_feeling: str


# =========================================================
# CREATE SCHEMA
# =========================================================

class CheckInCreate(CheckInRequest):
    pass


# =========================================================
# AI RESPONSE SCHEMA
# =========================================================

class AIAnalysisResult(BaseModel):

    stress_level: str

    score: int

    message: str

    recommendation: str

    is_escalated: bool


# =========================================================
# HISTORY RESPONSE
# =========================================================

class StressCheckInResponse(BaseModel):

    id: int

    timestamp: datetime

    stress_level: str

    score: int

    recommendation: str

    is_escalated: bool

    class Config:
        from_attributes = True