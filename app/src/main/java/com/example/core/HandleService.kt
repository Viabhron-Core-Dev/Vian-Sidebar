package com.example.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.AppLogger

class HandleService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var windowManager: WindowManager
    private lateinit var prefs: SharedPreferences
    private val triggerHandleViews = mutableListOf<TriggerHandleView>()

    companion object {
        const val ACTION_RELOAD_HANDLES = "com.example.ACTION_RELOAD_HANDLES"
    }

    private val reloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_RELOAD_HANDLES) {
                reloadHandles()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.d("HandleService", "onCreate")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)

        startForegroundService()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(reloadReceiver, IntentFilter(ACTION_RELOAD_HANDLES), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(reloadReceiver, IntentFilter(ACTION_RELOAD_HANDLES))
        }

        reloadHandles()
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "handle_channel",
                "App Handles",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(this, "handle_channel")
            .setContentTitle("Handles Active")
            .setContentText("Listening for edge gestures")
            .setSmallIcon(android.R.drawable.ic_menu_crop) // Placeholder icon
            .setContentIntent(pendingIntent)
            .build()

        startForeground(2, notification) // ID 2 so it doesn't conflict if there's ID 1
    }

    private fun reloadHandles() {
        AppLogger.d("HandleService", "reloadHandles")
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()

        val handles = HandleManager.getHandles(prefs)
        for (handle in handles) {
            if (handle.enabled) {
                val view = TriggerHandleView(this, prefs, windowManager, handle.id)
                view.attach()
                triggerHandleViews.add(view)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null && (key.startsWith("handle_") || key == "handles_list")) {
            // Need to update or recreate
            if (key.endsWith("_height") || key.endsWith("_width") || key.endsWith("_y") || key.endsWith("_edge") || key.endsWith("_color") || key.endsWith("_opacity") || key.endsWith("_position")) {
                triggerHandleViews.forEach { it.updatePosition() }
            } else if (key == "is_handle_edit_mode") {
                val editMode = prefs.getBoolean("is_handle_edit_mode", false)
                triggerHandleViews.forEach { it.setVisibility(if (editMode) true else null) }
            } else {
                reloadHandles()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.d("HandleService", "onStartCommand")
        return START_STICKY
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AppLogger.d("HandleService", "onTrimMemory level: $level")
        FloatingWindowManager.onTrimMemory(level)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d("HandleService", "onDestroy")
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        unregisterReceiver(reloadReceiver)
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
