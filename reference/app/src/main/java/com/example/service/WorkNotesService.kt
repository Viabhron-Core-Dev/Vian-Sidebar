package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WorkNotesService : Service() {
    private var windowManager: WorkNotesWindowManager? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = WorkNotesWindowManager(this)
        windowManager?.show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "TOGGLE") {
            // we could toggle but for now just show
            windowManager?.show()
        } else {
            windowManager?.show()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
