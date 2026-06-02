from sqlalchemy.orm import Session
import models
import schemas


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()

def create_user(db: Session, user: schemas.UserCreate, hashed_password: str):
    db_user = models.User(
        first_name=user.first_name,
        last_name=user.last_name,
        email=user.email,
        hashed_password=hashed_password,
        age=user.age,
        gender=user.gender
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def create_stress_checkin(db: Session, checkin_data: schemas.CheckInRequest, analysis_result: dict, user_id: int):
    db_checkin = models.StressCheckIn(
        user_id=user_id,
        **checkin_data.dict(),
        stress_level=analysis_result["stress_level"],
        score=analysis_result["score"],
        recommendation=analysis_result["recommendation"],
        actions=analysis_result.get("actions", []),
        is_escalated=analysis_result["is_escalated"]
    )
    db.add(db_checkin)
    db.commit()
    db.refresh(db_checkin)
    return db_checkin

def get_checkins(db: Session, user_id: int, skip: int = 0, limit: int = 100):
    return db.query(models.StressCheckIn).filter(models.StressCheckIn.user_id == user_id).order_by(models.StressCheckIn.timestamp.desc()).offset(skip).limit(limit).all()

def get_checkin_by_id(db: Session, checkin_id: int):
    return db.query(models.StressCheckIn).filter(models.StressCheckIn.id == checkin_id).first()

def delete_user(db: Session, user_id: int):
    # First, delete all checkins associated with the user
    db.query(models.StressCheckIn).filter(models.StressCheckIn.user_id == user_id).delete()
    # Next, delete the user
    db.query(models.User).filter(models.User.id == user_id).delete()
    db.commit()
    return True
