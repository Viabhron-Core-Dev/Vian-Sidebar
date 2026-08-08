import sqlite3
conn = sqlite3.connect(":memory:")
c = conn.cursor()
c.execute("CREATE TABLE test (name TEXT)")
c.execute("INSERT INTO test VALUES ('a'), ('b')")
conn.commit()

c.execute("SELECT * FROM test WHERE name NOT IN (NULL)")
print("NOT IN (NULL) count:", len(c.fetchall()))
