package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

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
