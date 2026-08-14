package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.launch

class AppNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d("AppNotificationListener", "Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d("AppNotificationListener", "Listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
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
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            val intent = android.content.Intent(ACTION_NOTIFICATION_REMOVED)
            intent.putExtra("package", it.packageName)
            sendBroadcast(intent)
        }
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CLEAR_ALL) {
            cancelAllNotifications()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        var instance: AppNotificationListener? = null
            private set
            
        const val ACTION_NOTIFICATION_POSTED = "com.example.ACTION_NOTIFICATION_POSTED"
        const val ACTION_NOTIFICATION_REMOVED = "com.example.ACTION_NOTIFICATION_REMOVED"
        const val ACTION_CLEAR_ALL = "com.example.ACTION_CLEAR_ALL"
    }
}
