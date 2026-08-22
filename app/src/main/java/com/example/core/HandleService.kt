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
import com.example.feature.miniapps.reader.ReaderHandleView
import com.example.core.NetSpeedManager
import com.example.feature.system_hub.CallRecorderManager

class HandleService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var windowManager: WindowManager
    private lateinit var prefs: SharedPreferences
    private val triggerHandleViews = mutableListOf<TriggerHandleView>()
    private var readerHandleView: ReaderHandleView? = null
    private var netSpeedManager: NetSpeedManager? = null
    private var callRecorderManager: CallRecorderManager? = null
    private var screenStateReceiver: BroadcastReceiver? = null
    private var downSpeed: Long = 0
    private var upSpeed: Long = 0
    private var dailyMobileBytes: Long = 0
    private var dailyWifiBytes: Long = 0

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
        
        dailyMobileBytes = prefs.getLong("daily_mobile_rx", 0) + prefs.getLong("daily_mobile_tx", 0)
        dailyWifiBytes = prefs.getLong("daily_wifi_rx", 0) + prefs.getLong("daily_wifi_tx", 0)

        startForegroundService()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(reloadReceiver, IntentFilter(ACTION_RELOAD_HANDLES), Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(reloadReceiver, IntentFilter(ACTION_RELOAD_HANDLES))
        }
        
        readerHandleView = ReaderHandleView(this, prefs, windowManager)
        if (prefs.getBoolean("reader_handle_enabled", false)) {
            readerHandleView?.attach()
        }
        reloadHandles()
        setupScreenStateReceiver()
        setupNetSpeed()
        setupCallRecorder()
    }


    private fun setupNetSpeed() {
        if (prefs.getBoolean("netspeed_enabled", true) || prefs.getBoolean("speed_indicator_enabled", true)) {
            if (netSpeedManager == null) {
                netSpeedManager = NetSpeedManager(this, prefs, 
                    onSpeedUpdate = { down, up ->
                        downSpeed = down
                        upSpeed = up
                        updateForegroundNotification()
                    },
                    onDailyDataUpdate = { mobile, wifi ->
                        dailyMobileBytes = mobile
                        dailyWifiBytes = wifi
                    }
                )
            }
            netSpeedManager?.start()
        } else {
            netSpeedManager?.stop()
            downSpeed = 0
            upSpeed = 0
            updateForegroundNotification()
        }
    }

    private fun setupCallRecorder() {
        if (callRecorderManager == null) {
            callRecorderManager = CallRecorderManager(this, prefs)
        }
        if (prefs.getBoolean("call_recorder_enabled", false) || prefs.getBoolean("call_recorder_manual_enabled", false)) {
            callRecorderManager?.startListening()
        } else {
            callRecorderManager?.stopListening()
        }
    }

    private fun setupScreenStateReceiver() {
        screenStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        if (prefs.getBoolean("netspeed_enabled", true) || prefs.getBoolean("speed_indicator_enabled", true)) {
                            netSpeedManager?.start()
                        }
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        netSpeedManager?.stop()
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
    }
    

    
    private fun formatSpeed(bytes: Long): String {
        if (bytes < 1000) return "${bytes} B/s"
        val kb = (bytes + 512) / 1024
        if (kb < 1000) return "${kb} kB/s"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb < 10.0) String.format(java.util.Locale.US, "%.1f MB/s", mb) else String.format(java.util.Locale.US, "%.0f MB/s", mb)
    }

    private fun formatDataBytes(bytes: Long): String {
        if (bytes < 1024 * 1024) {
            return String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
        }
        val mb = bytes / (1024.0 * 1024.0)
        if (mb < 1024.0) {
            return String.format(java.util.Locale.US, "%.2f MB", mb)
        }
        val gb = mb / 1024.0
        return String.format(java.util.Locale.US, "%.2f GB", gb)
    }

    private fun buildNotification(): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        
        val isNetSpeedActive = prefs.getBoolean("netspeed_enabled", true) || prefs.getBoolean("speed_indicator_enabled", true)
        val totalTodayBytes = dailyMobileBytes + dailyWifiBytes
        val totalSpeed = downSpeed + upSpeed

        val contentTitle = if (isNetSpeedActive) {
            if (totalTodayBytes > 0) "Data: ${formatDataBytes(totalTodayBytes)}" else "Internet Speed Monitor"
        } else {
            "Handles Active"
        }

        val contentText = if (isNetSpeedActive) {
            "Down: ${formatSpeed(downSpeed)}   Up: ${formatSpeed(upSpeed)}"
        } else {
            "Listening for edge gestures"
        }
        
        val builder = NotificationCompat.Builder(this, "handle_channel")
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            
        if (isNetSpeedActive) {
            val speedUnits = prefs.getString("speed_units", "Auto")
            val bigText = "Down: ${formatSpeed(downSpeed)}   Up: ${formatSpeed(upSpeed)}\nMobile: ${formatDataBytes(dailyMobileBytes)} • Wi-Fi: ${formatDataBytes(dailyWifiBytes)}"
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            builder.setSmallIcon(DynamicSpeedIconGenerator.generateIconCompat(this, totalSpeed, speedUnits))
        } else {
            builder.setSmallIcon(android.R.drawable.ic_menu_crop)
        }
        
        return builder.build()
    }

    private fun updateForegroundNotification() {
        val notification = buildNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(2, notification)
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
        val notification = buildNotification()
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
        if (key == "netspeed_enabled" || key == "speed_indicator_enabled") {
            setupNetSpeed()
        } else if (key == "call_recorder_enabled" || key == "call_recorder_manual_enabled") {
            setupCallRecorder()
        }
        
        if (key != null && (key.startsWith("handle_") || key == "handles_list")) {
            // Need to update or recreate
            if (key.endsWith("_height") || key.endsWith("_width") || key.endsWith("_y") || key.endsWith("_edge") || key.endsWith("_color") || key.endsWith("_opacity") || key.endsWith("_position")) {
                triggerHandleViews.forEach { it.updatePosition() }
                readerHandleView?.updatePosition()
            } else if (key == "is_handle_edit_mode") {
                val editMode = prefs.getBoolean("is_handle_edit_mode", false)
                triggerHandleViews.forEach { it.setVisibility(if (editMode) true else null) }
                readerHandleView?.setVisibility(editMode)
            } else {
                reloadHandles()
            }
        } else if (key == "reader_handle_enabled") {
            if (prefs.getBoolean("reader_handle_enabled", false)) {
                readerHandleView?.attach()
            } else {
                readerHandleView?.detach()
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
        screenStateReceiver?.let { unregisterReceiver(it) }
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()
        readerHandleView?.detach()
        readerHandleView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
