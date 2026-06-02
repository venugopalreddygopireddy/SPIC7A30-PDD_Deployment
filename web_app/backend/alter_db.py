import os
from sqlalchemy import create_engine
from sqlalchemy.sql import text
from dotenv import load_dotenv

load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")
if DATABASE_URL:
    try:
        engine = create_engine(DATABASE_URL)
        with engine.connect() as conn:
            conn.execute(text("ALTER TABLE stress_checkins ADD COLUMN mobile_number VARCHAR DEFAULT '';"))
            conn.commit()
            print("Successfully added mobile_number column.")
    except Exception as e:
        print(f"Error: {e}")
