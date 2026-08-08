import os

# Create FileExplorerFloatingService.kt
service_code = """package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class FileExplorerFloatingService : Service() {
    private var windowManager: FileExplorerWindowManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val path = intent?.getStringExtra("START_PATH")
        showWindowManager(path)
        return START_NOT_STICKY
    }

    private fun showWindowManager(startPath: String?) {
        if (windowManager == null) {
            windowManager = FileExplorerWindowManager(this) {
                windowManager = null
                stopSelf()
            }
        }
        if (startPath != null) {
            windowManager?.openPath(startPath)
        }
        windowManager?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.close()
    }
}
"""

with open("app/src/main/java/com/example/service/FileExplorerFloatingService.kt", "w") as f:
    f.write(service_code)

# Register service and launcher in AndroidManifest.xml
