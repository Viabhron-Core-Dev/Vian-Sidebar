#!/bin/bash
cat << 'INNER' > app/src/main/java/com/example/service/PwaDatabase.kt
package com.example.service

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pwa_entries")
data class PwaEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val zipPath: String,
    val isLightweight: Boolean,
    val useVirtualHost: Boolean = true,
    val persistentPort: Int = 0,
    val incognitoMode: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface PwaDao {
    @Query("SELECT * FROM pwa_entries ORDER BY addedAt DESC")
    fun getAllPwas(): Flow<List<PwaEntry>>

    @Query("SELECT * FROM pwa_entries ORDER BY addedAt DESC")
    fun getAllPwasSync(): List<PwaEntry>

    @Insert
    suspend fun insertPwa(pwa: PwaEntry)

    @Delete
    suspend fun deletePwa(pwa: PwaEntry)

    @Update
    suspend fun updatePwa(pwa: PwaEntry)
}

@Database(entities = [PwaEntry::class], version = 2, exportSchema = false)
abstract class PwaDatabase : RoomDatabase() {
    abstract fun pwaDao(): PwaDao

    companion object {
        @Volatile
        private var INSTANCE: PwaDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pwa_entries ADD COLUMN useVirtualHost INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE pwa_entries ADD COLUMN persistentPort INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE pwa_entries ADD COLUMN incognitoMode INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): PwaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PwaDatabase::class.java,
                    "pwa.db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
INNER
