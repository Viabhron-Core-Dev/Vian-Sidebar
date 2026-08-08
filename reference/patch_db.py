import re

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "@Database(entities = [EpubBook::class, TrackerBook::class, QuickNote::class, LogEntry::class, SchedulerTask::class], version = 7, exportSchema = false)",
    "@Database(entities = [EpubBook::class, TrackerBook::class, QuickNote::class, LogEntry::class, SchedulerTask::class, AppyworkProject::class, AppyworkFileNode::class], version = 8, exportSchema = false)"
)

content = content.replace(
    "abstract fun schedulerTaskDao(): SchedulerTaskDao",
    "abstract fun schedulerTaskDao(): SchedulerTaskDao\n    abstract fun appyworkDao(): AppyworkDao"
)

new_migration = """
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `appywork_projects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `remoteUrl` TEXT NOT NULL, `authType` TEXT NOT NULL, `authToken` TEXT NOT NULL, `lastUpdated` INTEGER NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `appywork_file_nodes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `path` TEXT NOT NULL, `localHash` TEXT NOT NULL, `syncState` TEXT NOT NULL, FOREIGN KEY(`projectId`) REFERENCES `appywork_projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_appywork_file_nodes_projectId_path` ON `appywork_file_nodes` (`projectId`, `path`)")
            }
        }
"""

content = content.replace(
    "fun getDatabase(context: Context): AppDatabase {",
    new_migration + "\n        fun getDatabase(context: Context): AppDatabase {"
)

content = content.replace(
    ".addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)",
    ".addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)"
)

with open('app/src/main/java/com/example/data/AppDatabase.kt', 'w') as f:
    f.write(content)
