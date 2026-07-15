import psycopg2

def clean_db():
    conn = psycopg2.connect('postgresql://postgres:venugopal5242@localhost:5432/cortisense_db')
    cur = conn.cursor()
    
    cur.execute("DELETE FROM stress_checkins WHERE alcohol_intake = 0 OR meditation_practice = 0")
    conn.commit()
    print("Cleaned!")

if __name__ == "__main__":
    clean_db()
