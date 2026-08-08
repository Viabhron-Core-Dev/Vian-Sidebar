package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.utils.AppyworkParser
import com.example.utils.ParsedCodeBlock

class AppyworkFloatingService : Service() {

    private val parsedBlocks = mutableListOf<ParsedCodeBlock>()
    private var windowManager: AppyworkWindowManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "PARSE_TEXT") {
            val text = intent.getStringExtra("RAW_TEXT") ?: ""
            if (text.isNotBlank()) {
                val blocks = AppyworkParser.parseText(text)
                parsedBlocks.addAll(blocks)
                
                Log.d("Appywork", "Parsed ${blocks.size} blocks. Quarantined: ${blocks.count { it.isQuarantined }}")
                
                showWindowManager()
            }
        } else if (intent?.action == "SHOW_WINDOW") {
            showWindowManager()
        }
        return START_NOT_STICKY
    }

    private fun showWindowManager() {
        if (windowManager == null) {
            windowManager = AppyworkWindowManager(this, parsedBlocks) {
                windowManager = null
                stopSelf()
            }
            windowManager?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager?.close()
    }
}
