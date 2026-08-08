import sqlite3

conn = sqlite3.connect(":memory:")
c = conn.cursor()
c.execute("CREATE TABLE test (name TEXT)")
c.execute("INSERT INTO test VALUES ('a'), ('b')")
conn.commit()

try:
    c.execute("SELECT * FROM test WHERE name NOT IN ()")
    print("Empty IN clause worked:", c.fetchall())
except Exception as e:
    print("Error:", e)
