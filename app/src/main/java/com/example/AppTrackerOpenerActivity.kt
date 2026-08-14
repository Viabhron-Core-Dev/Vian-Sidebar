package com.example
import com.example.feature.system_hub.VianSideAccessibilityService

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

class AppTrackerOpenerActivity : Activity() {
    private var packageNames = arrayListOf<String>()
    private var currentIndex = 0
    private var isAutoForceStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        isAutoForceStop = prefs.getBoolean("app_tracker_auto_force_stop", false)
        
        packageNames = intent.getStringArrayListExtra("packages") ?: arrayListOf()
        if (packageNames.isEmpty()) {
            finish()
            return
        }
        
        if (isAutoForceStop) {
            com.example.feature.system_hub.VianSideAccessibilityService.isForceStopping = true
        }
        
        openNext()
    }

    private fun openNext() {
        if (currentIndex < packageNames.size) {
            val pkg = packageNames[currentIndex]
            currentIndex++
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$pkg")
                }
                startActivityForResult(intent, 100)
            } catch (e: Exception) {
                openNext()
            }
        } else {
            if (isAutoForceStop) {
                com.example.feature.system_hub.VianSideAccessibilityService.isForceStopping = false
            }
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            openNext()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        com.example.feature.system_hub.VianSideAccessibilityService.isForceStopping = false
    }
}
