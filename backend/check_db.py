import sys
from sqlalchemy import create_engine, text

# Added ?sslmode=require
DATABASE_URL = "postgresql://cortisense_db_0l45_user:BsLvPxSPdAiB4DD3UNwKqrvSdRlwNMf9@dpg-d9aqgv8k1i2s73aeau3g-a.oregon-postgres.render.com/cortisense_db_0l45?sslmode=require"


try:
    engine = create_engine(DATABASE_URL)
    with engine.connect() as conn:
        result = conn.execute(text("SELECT id, first_name, last_name, email, age, gender FROM users;"))
        rows = result.fetchall()
        print(f"--- SUCCESS! Found {len(rows)} user(s) in the database ---")
        for row in rows:
            print(f"ID: {row[0]} | Name: {row[1]} {row[2]} | Email: {row[3]} | Age: {row[4]} | Gender: {row[5]}")
except Exception as e:
    print(f"Error connecting to DB: {e}")
