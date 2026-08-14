filepath = 'app/src/main/java/com/example/service/AppNotificationListener.kt'
with open(filepath, 'r') as f:
    content = f.read()

import re

# We want to replace onNotificationPosted
old_on_posted = """    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val intent = android.content.Intent(ACTION_NOTIFICATION_POSTED)
            intent.putExtra("package", it.packageName)
            sendBroadcast(intent)
        }
    }"""

new_on_posted = """    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val intent = android.content.Intent(ACTION_NOTIFICATION_POSTED)
            intent.putExtra("package", it.packageName)
            sendBroadcast(intent)
            
            val title = it.notification.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = it.notification.extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
            val packageName = it.packageName
            
            if (title.isNotBlank() || text.isNotBlank()) {
                val pm = applicationContext.packageManager
                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
                } catch (e: Exception) {
                    packageName
                }
                
                val history = com.example.data.NotificationHistory(
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        com.example.data.AppDatabase.getDatabase(applicationContext).notificationHistoryDao().insert(history)
                    } catch (e: Exception) {
                        Log.e("AppNotificationListener", "Error inserting notification", e)
                    }
                }
            }
        }
    }"""

content = content.replace(old_on_posted, new_on_posted)

# Need to add import for kotlinx.coroutines.launch
if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import android.util.Log", "import android.util.Log\nimport kotlinx.coroutines.launch")

with open(filepath, 'w') as f:
    f.write(content)
print("AppNotificationListener updated")
