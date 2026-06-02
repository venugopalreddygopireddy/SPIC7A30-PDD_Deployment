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
            conn.execute(text("ALTER TABLE users ADD COLUMN mobile_number VARCHAR DEFAULT '';"))
            conn.execute(text("ALTER TABLE users ADD COLUMN dob VARCHAR DEFAULT '';"))
            conn.execute(text("ALTER TABLE users ADD COLUMN goal VARCHAR DEFAULT '';"))
            conn.commit()
            print("Successfully added columns to users table.")
    except Exception as e:
        print(f"Error: {e}")
