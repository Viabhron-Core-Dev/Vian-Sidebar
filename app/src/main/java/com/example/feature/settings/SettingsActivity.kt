package com.example.feature.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentRoute by remember { mutableStateOf("handles_list") }
                    var currentHandleId by remember { mutableStateOf("") }
                    var currentGestureIdStr by remember { mutableStateOf("") }

                    BackHandler(enabled = currentRoute != "handles_list") {
                        currentRoute = "handles_list"
                    }

                    when (currentRoute) {
                        "handles_list" -> {
                            HandlesListSettingsScreen(
                                onNavigateToHandle = { handleId ->
                                    currentHandleId = handleId
                                    currentRoute = "handle_edit"
                                },
                                onNavigateToSidebarSettings = { gestureIdStr ->
                                    currentGestureIdStr = gestureIdStr
                                    currentRoute = "sidebar_settings"
                                },
                                onBack = { finish() }
                            )
                        }
                        "handle_edit" -> {
                            HandleEditScreen(
                                handleId = currentHandleId,
                                onBack = { currentRoute = "handles_list" }
                            )
                        }
                        "sidebar_settings" -> {
                            // gestureIdStr might be "handle_1_swipe_left|open_page:apps"
                            val handleIdAndGesture = currentGestureIdStr.substringBefore("|")
                            val initAction = if (currentGestureIdStr.contains("|")) currentGestureIdStr.substringAfter("|") else null
                            SidebarSettingsScreen(
                                handleId = handleIdAndGesture,
                                initAction = initAction,
                                onBack = { currentRoute = "handles_list" }
                            )
                        }
                        "permissions" -> {
                            PermissionManagerScreen(
                                onContinue = { currentRoute = "handles_list" },
                                isFirstLaunch = false
                            )
                        }
                    }
                }
            }
        }
    }
}
