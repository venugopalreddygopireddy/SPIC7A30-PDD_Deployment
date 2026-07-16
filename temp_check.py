import psycopg2
try:
    conn = psycopg2.connect('postgresql://postgres:venugopal5242@localhost:5432/cortisense_db')
    c = conn.cursor()
    c.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'users'")
    print(c.fetchall())
except Exception as e:
    print(e)
