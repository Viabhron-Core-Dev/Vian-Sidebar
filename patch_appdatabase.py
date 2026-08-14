filepath = 'app/src/main/java/com/example/data/AppDatabase.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Add to entities
content = content.replace("entities = [EpubBook::class", "entities = [NotificationHistory::class, EpubBook::class")

# Change version to 9
content = content.replace("version = 8", "version = 9")

# Add DAO
dao_line = "    abstract fun appyworkDao(): AppyworkDao\n    abstract fun notificationHistoryDao(): NotificationHistoryDao"
content = content.replace("    abstract fun appyworkDao(): AppyworkDao", dao_line)

# Add Migration 8 to 9
migration_8_9 = """        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `notification_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `packageName` TEXT NOT NULL, `appName` TEXT NOT NULL, `title` TEXT NOT NULL, `text` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
            }
        }"""
content = content.replace("        fun getDatabase(context: Context): AppDatabase {", migration_8_9 + "\n        fun getDatabase(context: Context): AppDatabase {")

# Add MIGRATION_8_9 to addMigrations
content = content.replace("MIGRATION_7_8).fallback", "MIGRATION_7_8, MIGRATION_8_9).fallback")

with open(filepath, 'w') as f:
    f.write(content)
print("AppDatabase updated")
