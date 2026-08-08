package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import android.database.sqlite.SQLiteDatabase

@RunWith(RobolectricTestRunner::class)
class SqliteTest {
    @Test
    fun testEmptyNotIn() {
        val db = SQLiteDatabase.create(null)
        db.execSQL("CREATE TABLE test (name TEXT)")
        db.execSQL("INSERT INTO test VALUES ('a')")
        try {
            val cursor = db.rawQuery("SELECT * FROM test WHERE name NOT IN ()", null)
            cursor.moveToFirst()
            println("Success, count: ${cursor.count}")
            cursor.close()
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }
        db.close()
    }
}
