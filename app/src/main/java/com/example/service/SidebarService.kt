package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.view.WindowManager
import com.example.feature.sidebar.SidebarManager

class SidebarService : Service() {

    private lateinit var sidebarManager: SidebarManager
    private lateinit var prefs: SharedPreferences
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sidebarManager = SidebarManager(this, prefs, windowManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sidebarManager.handleIntent(intent)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sidebarManager.closeSidebar()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        sidebarManager.onTrimMemory(level)
    }
}
