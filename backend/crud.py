from sqlalchemy.orm import Session
import models
import schemas


def create_stress_checkin(db: Session, checkin_data: schemas.CheckInRequest, analysis_result: dict):
    db_checkin = models.StressCheckIn(
        **checkin_data.dict(),
        stress_level=analysis_result["stress_level"],
        score=analysis_result["score"],
        recommendation=analysis_result["recommendation"],
        is_escalated=analysis_result["is_escalated"]
    )
    db.add(db_checkin)
    db.commit()
    db.refresh(db_checkin)
    return db_checkin

def get_checkins(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.StressCheckIn).order_by(models.StressCheckIn.timestamp.desc()).offset(skip).limit(limit).all()

def get_checkin_by_id(db: Session, checkin_id: int):
    return db.query(models.StressCheckIn).filter(models.StressCheckIn.id == checkin_id).first()
