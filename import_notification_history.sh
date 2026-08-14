#!/bin/bash
mkdir -p app/src/main/java/com/example/data

# 1. NotificationHistory.kt
cat << 'KTEOF' > app/src/main/java/com/example/data/NotificationHistory.kt
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
KTEOF

# 2. NotificationHistoryDao.kt
cat << 'KTEOF' > app/src/main/java/com/example/data/NotificationHistoryDao.kt
package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {
    @Insert
    suspend fun insert(notification: NotificationHistory)

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NotificationHistory>>
    
    @Query("SELECT * FROM notification_history WHERE packageName NOT IN (:excludedPackages) ORDER BY timestamp DESC")
    fun getFiltered(excludedPackages: List<String>): Flow<List<NotificationHistory>>

    @Query("SELECT * FROM notification_history WHERE (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appName LIKE '%' || :query || '%') ORDER BY timestamp DESC")
    fun searchAll(query: String): Flow<List<NotificationHistory>>

    @Query("SELECT * FROM notification_history WHERE (title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appName LIKE '%' || :query || '%') AND packageName NOT IN (:excludedPackages) ORDER BY timestamp DESC")
    fun search(query: String, excludedPackages: List<String>): Flow<List<NotificationHistory>>

    @Query("DELETE FROM notification_history")
    suspend fun deleteAll()
}
KTEOF

# 3. NotificationHistoryActivity.kt
cat reference/app/src/main/java/com/example/NotificationHistoryActivity.kt | \
sed 's/com.example.db.NotificationHistory/com.example.data.NotificationHistory/g' | \
sed 's/com.example.db.AppDatabase/com.example.data.AppDatabase/g' \
> app/src/main/java/com/example/NotificationHistoryActivity.kt

echo "Files imported"
