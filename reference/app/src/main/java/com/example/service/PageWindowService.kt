package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PageWindowService : Service() {
    private val windows = mutableMapOf<String, PageWindowManager>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pageType = intent?.getStringExtra("PAGE_TYPE") ?: return START_NOT_STICKY
        
        if (intent.action == "TOGGLE") {
            if (windows.containsKey(pageType)) {
                windows[pageType]?.close()
                windows.remove(pageType)
            } else {
                val window = PageWindowManager(this, pageType) { windows.remove(pageType) }
                windows[pageType] = window
                window.show()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windows.values.forEach { it.close() }
        windows.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
