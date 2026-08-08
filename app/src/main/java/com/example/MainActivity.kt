package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.feature.settings.PermissionManagerScreen
import com.example.feature.settings.SettingsActivity
import com.example.service.SidebarService
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val firstLaunch = prefs.getBoolean("first_launch", true)
        
        if (!firstLaunch) {
            startSidebarService()
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            return
        }
        
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionManagerScreen(
                        onContinue = {
                            prefs.edit().putBoolean("first_launch", false).apply()
                            preSeedHandleConfig()
                            startSidebarService()
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            finish()
                        },
                        isFirstLaunch = true
                    )
                }
            }
        }
    }

    private fun startSidebarService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            val svcIntent = Intent(this, SidebarService::class.java).apply {
                putExtra("OPEN_FROM_LAUNCHER", true)
            }
            ContextCompat.startForegroundService(this, svcIntent)
        }
    }

    private fun preSeedHandleConfig() {
        val prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        if (!prefs.contains("handles_config")) {
            val handlesArray = JSONArray()
            val handle1 = JSONObject().apply {
                put("id", "handle_1")
                put("name", "Main Handle")
                put("color", 0xFF6200EE.toInt())
                val appsArray = JSONArray().apply {
                    put("Home Grid")
                    put("Log Keeper")
                    put("eReader")
                }
                put("apps", appsArray)
            }
            handlesArray.put(handle1)
            prefs.edit().putString("handles_config", handlesArray.toString()).apply()
        }
    }
}
