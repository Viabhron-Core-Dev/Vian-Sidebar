package com.example.feature.miniapps
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DictionaryEntry::class], version = 2, exportSchema = false)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao
}
