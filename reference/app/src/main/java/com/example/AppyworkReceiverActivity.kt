package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.service.AppyworkFloatingService

class AppyworkReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var text: String? = null
        if (intent.action == Intent.ACTION_PROCESS_TEXT) {
            text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        }
        
        if (!text.isNullOrBlank()) {
            val serviceIntent = Intent(this, AppyworkFloatingService::class.java).apply {
                action = "PARSE_TEXT"
                putExtra("RAW_TEXT", text)
            }
            startService(serviceIntent)
        }
        
        finish()
    }
}
