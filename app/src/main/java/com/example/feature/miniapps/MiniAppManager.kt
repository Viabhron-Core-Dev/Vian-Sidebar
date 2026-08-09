package com.example.feature.miniapps

import android.content.Context
import android.view.WindowManager
import com.example.core.FloatingWindow
import com.example.core.FloatingWindowManager

object MiniAppManager {
    fun toggleApp(context: Context, pageType: String) {
        // Check if window already exists in FloatingWindowManager
        val windows = FloatingWindowManager.activeWindows
        
        val existing = windows.find { 
            (it is CalculatorFloatingWindow && pageType == "calculator") ||
            (it is CompassFloatingWindow && pageType == "compass") ||
            (it is DictionaryFloatingWindow && pageType == "dictionary")
        }
        
        if (existing != null) {
            FloatingWindowManager.removeWindow(existing)
            return
        }

        val newWindow: FloatingWindow = when (pageType) {
            "calculator" -> CalculatorFloatingWindow(context)
            "compass" -> CompassFloatingWindow(context)
            "dictionary" -> DictionaryFloatingWindow(context)
            else -> return // Handle others later
        }

        FloatingWindowManager.addWindow(newWindow)
    }
}
