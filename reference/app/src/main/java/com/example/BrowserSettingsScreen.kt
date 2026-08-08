package com.example

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("FloatingBrowserPrefs", Context.MODE_PRIVATE) }
    
    var jsEnabled by remember { mutableStateOf(prefs.getBoolean("default_js_enabled", false)) }
    var imagesEnabled by remember { mutableStateOf(prefs.getBoolean("default_images_enabled", false)) }
    var wrapContent by remember { mutableStateOf(prefs.getBoolean("default_wrap_content", true)) }
    var adBlock by remember { mutableStateOf(prefs.getBoolean("default_ad_block", true)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browser Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ListItem(
                headlineContent = { Text("JavaScript") },
                supportingContent = { Text("Enable JavaScript by default (less secure)") },
                trailingContent = {
                    Switch(
                        checked = jsEnabled,
                        onCheckedChange = { 
                            jsEnabled = it
                            prefs.edit().putBoolean("default_js_enabled", it).apply()
                        }
                    )
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Images") },
                supportingContent = { Text("Load images automatically by default") },
                trailingContent = {
                    Switch(
                        checked = imagesEnabled,
                        onCheckedChange = { 
                            imagesEnabled = it
                            prefs.edit().putBoolean("default_images_enabled", it).apply()
                        }
                    )
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Wrap Content") },
                supportingContent = { Text("Wrap content to fit screen width") },
                trailingContent = {
                    Switch(
                        checked = wrapContent,
                        onCheckedChange = { 
                            wrapContent = it
                            prefs.edit().putBoolean("default_wrap_content", it).apply()
                        }
                    )
                }
            )
            Divider()
            
            ListItem(
                headlineContent = { Text("Ad/Tracker Blocker") },
                supportingContent = { Text("Basic ad and tracker blocking") },
                trailingContent = {
                    Switch(
                        checked = adBlock,
                        onCheckedChange = { 
                            adBlock = it
                            prefs.edit().putBoolean("default_ad_block", it).apply()
                        }
                    )
                }
            )
            Divider()
            
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Floating browser automatically clears all caches, history, and memory immediately upon being closed. Only saved items are kept.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
