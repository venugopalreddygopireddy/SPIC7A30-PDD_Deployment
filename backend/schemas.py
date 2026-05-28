from pydantic import BaseModel, EmailStr
from typing import Optional
from datetime import datetime

# =========================================================
# AUTHENTICATION SCHEMAS
# =========================================================

class UserCreate(BaseModel):
    first_name: str
    last_name: str
    age: int
    gender: str
    email: str
    password: str

class UserLogin(BaseModel):
    email: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str

class UserResponse(BaseModel):
    id: int
    first_name: str
    last_name: str
    email: str
    age: int
    gender: str
    created_at: datetime

    class Config:
        from_attributes = True

class ForgotPasswordRequest(BaseModel):
    email: str

class VerifyOTPRequest(BaseModel):
    email: str
    otp: str

class ResetPasswordRequest(BaseModel):
    email: str
    otp: str
    new_password: str



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

    sleep_duration: float
    sleep_quality: int
    physical_activity: int
    screen_time: float
    workload: str
    mood: str
    anxiety: str

    class Config:
        from_attributes = True

# =========================================================
# ANALYTICS RESPONSE
# =========================================================

from typing import Dict, List, Any

class WeeklyAnalyticsResponse(BaseModel):
    avg_score: int
    highest_score: int
    lowest_score: int
    total_checkins: int
    distribution: Dict[str, int]

class MonthlyAnalyticsResponse(BaseModel):
    avg_score: int
    total_checkins: int
    distribution: Dict[str, int]
    calendar_activity: Dict[str, int]

class DailyTrend(BaseModel):
    date: str
    score: int
    level: str

class TrendsResponse(BaseModel):
    trends: List[DailyTrend]

class FactorsResponse(BaseModel):
    sleep_avg: float
    screen_time_avg: float
    caffeine_avg: float
    physical_activity_avg: float
    top_mood: str
    top_workload: str
    top_exercise: str