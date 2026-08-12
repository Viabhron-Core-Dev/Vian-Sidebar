package com.example.feature.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID
import com.example.core.HandleManager
import com.example.core.HandleConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandlesListSettingsScreen(
    onNavigateToHandle: (String) -> Unit,
    onNavigateToSidebarSettings: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE) }
    
    var handles by remember { mutableStateOf(HandleManager.getHandles(prefs)) }
    
    fun save() {
        HandleManager.saveHandles(prefs, handles)
        handles = handles.toList() // trigger recomposition
    }

    var expandedHandleId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Handles & Sidebar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val newId = UUID.randomUUID().toString()
                prefs.edit().putString("handle_${newId}_tap", "toggle_sidebar").apply()
                handles = handles + HandleConfig(id = newId, name = "Handle ${handles.size + 1}", enabled = true)
                save()
            }) {
                Icon(Icons.Default.Add, "Add Handle")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            items(handles) { handle ->
                HandleItem(
                    handle = handle,
                    prefs = prefs,
                    isExpanded = expandedHandleId == handle.id,
                    onExpand = {
                        expandedHandleId = if (expandedHandleId == handle.id) null else handle.id
                    },
                    onNavigateToHandle = { onNavigateToHandle(handle.id) },
                    onNavigateToSidebarSettings = { gesture, action -> onNavigateToSidebarSettings("${handle.id}_$gesture" + (if (action != null) "|$action" else "")) },
                    onUpdate = { updated ->
                        handles = handles.map { if (it.id == updated.id) updated else it }
                        save()
                    },
                    onDelete = {
                        handles = handles.filter { it.id != handle.id }
                        save()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandleItem(
    handle: HandleConfig,
    prefs: android.content.SharedPreferences,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onNavigateToHandle: () -> Unit,
    onNavigateToSidebarSettings: (String, String?) -> Unit,
    onUpdate: (HandleConfig) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAddGestureDialog by remember { mutableStateOf(false) }
                    var showChangeGestureDialog by remember { mutableStateOf(false) }
                    var gestureToChange by remember { mutableStateOf("") }
    var showChangeTriggerDialog by remember { mutableStateOf(false) }
    var triggerToChange by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpand() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(handle.name, style = MaterialTheme.typography.titleMedium)
                }
                Switch(
                    checked = handle.enabled,
                    onCheckedChange = { onUpdate(handle.copy(enabled = it)) }
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Adjust") },
                            onClick = {
                                showMenu = false
                                onNavigateToHandle()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Expanded content (Gestures)
            if (isExpanded) {
                Divider()
                Column(modifier = Modifier.padding(16.dp)) {
                    val gestureKeys = listOf("tap", "double_tap", "long_press", "swipe_up", "swipe_down", "swipe_left", "swipe_right")
                    val gestureLabels = mapOf(
                        "tap" to "Single Tap",
                        "double_tap" to "Double Tap",
                        "long_press" to "Long Press",
                        "swipe_up" to "Swipe Up",
                        "swipe_down" to "Swipe Down",
                        "swipe_left" to "Swipe Left",
                        "swipe_right" to "Swipe Right"
                    )
                    
                    val prefix = "handle_${handle.id}_"
                    val gesturesMap = remember { mutableStateMapOf<String, String>() }
                    
                    LaunchedEffect(isExpanded, handle.id) {
                        gesturesMap.clear()
                        gestureKeys.forEach { key ->
                            val action = prefs.getString("${prefix}$key", "none") ?: "none"
                            if (action != "none") {
                                gesturesMap[key] = action
                            }
                        }
                    }
                    
                    fun migrateGesture(oldGesture: String, newGesture: String) {
                        val oldPrefix = "handle_${handle.id}_${oldGesture}"
                        val newPrefix = "handle_${handle.id}_${newGesture}"
                        val editor = prefs.edit()
                        
                        val action = prefs.getString(oldPrefix, null)
                        if (action != null) {
                            editor.putString(newPrefix, action)
                            editor.remove(oldPrefix)
                        }
                        
                        prefs.all.keys.forEach { key ->
                            if (key.startsWith("${oldPrefix}_")) {
                                val newKey = key.replaceFirst(oldPrefix, newPrefix)
                                val value = prefs.all[key]
                                when (value) {
                                    is String -> editor.putString(newKey, value)
                                    is Int -> editor.putInt(newKey, value)
                                    is Boolean -> editor.putBoolean(newKey, value)
                                    is Float -> editor.putFloat(newKey, value)
                                    is Long -> editor.putLong(newKey, value)
                                }
                                editor.remove(key)
                            }
                        }
                        editor.apply()
                        
                        gesturesMap.remove(oldGesture)
                        if (action != null) {
                            gesturesMap[newGesture] = action
                        }
                    }
                    
                    fun updateGesture(gesture: String, action: String) {
                        if (action == "none") {
                            gesturesMap.remove(gesture)
                        } else {
                            gesturesMap[gesture] = action
                            if (action.startsWith("open_page:")) {
                                val pageType = action.removePrefix("open_page:")
                                val containerId = "${handle.id}_$gesture"
                                val currentPages = prefs.getString("handle_${containerId}_pages", null)
                                if (currentPages == null) {
                                    val pageTitle = when(pageType) {
                                        "apps" -> "Apps Grid"
                                        "widgets_grid" -> "Widgets Grid"
                                        "hybrid_grid" -> "Hybrid Grid"
                                        "app_tracker" -> "App Tracker"
                                        "resources_tracker" -> "Resources Tracker"
                    "media_player" -> "Media Player"
                    "widget" -> "Android Widget"
                                        "calculator" -> "Calculator"
                                        "scheduler" -> "Short Reminders"
                                        "compass" -> "Compass"
                                        "notifications" -> "Notifications"
                                        else -> "Page"
                                    }
                                    val newPage = com.example.utils.SidebarPage(id = UUID.randomUUID().toString(), type = pageType, title = pageTitle)
                                    val arr = org.json.JSONArray().apply { put(newPage.toJson()) }
                                    prefs.edit().putString("handle_${containerId}_pages", arr.toString()).apply()
                                }
                            }
                        }
                        prefs.edit().putString("${prefix}$gesture", action).apply()
                    }

                    if (gesturesMap.isEmpty()) {
                        Text("No gestures assigned.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    } else {
                        gesturesMap.forEach { (gesture, action) ->
                            val actionName = when {
                                action == "toggle_sidebar" -> "Sidebar (Default)"
                                action == "toggle_reader" -> "Toggle Reader"
                                action.startsWith("open_page:") -> "Page: ${action.removePrefix("open_page:")}"
                                action.startsWith("action:") -> "Action: ${action.removePrefix("action:")}"
                                else -> action
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = {
                                    if (action == "toggle_sidebar") {
                                        onNavigateToSidebarSettings(gesture, null)
                                    } else if (action.startsWith("open_page:")) {
                                        onNavigateToSidebarSettings(gesture, action)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(gestureLabels[gesture] ?: gesture, style = MaterialTheme.typography.titleSmall)
                                        Text(actionName, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    var showGestureMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showGestureMenu = true }) {
                                            Icon(Icons.Default.MoreVert, "More")
                                        }
                                        DropdownMenu(
                                            expanded = showGestureMenu,
                                            onDismissRequest = { showGestureMenu = false }
                                        ) {
                                            if (action == "toggle_sidebar") {
                                                DropdownMenuItem(
                                                    text = { Text("Sidebar Settings") },
                                                    onClick = {
                                                        showGestureMenu = false
                                                        onNavigateToSidebarSettings(gesture, null)
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text("Change Action") },
                                                onClick = {
                                                    showGestureMenu = false
                                                    gestureToChange = gesture
                                                    showChangeGestureDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Change Gesture") },
                                                onClick = {
                                                    showGestureMenu = false
                                                    triggerToChange = gesture
                                                    showChangeTriggerDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Remove") },
                                                onClick = {
                                                    showGestureMenu = false
                                                    updateGesture(gesture, "none")
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAddGestureDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD GESTURE")
                    }
                    
                    if (showChangeTriggerDialog) {
                        val availableGestures = gestureKeys.filter { !gesturesMap.containsKey(it) || it == triggerToChange }
                        var selectedNewGesture by remember { mutableStateOf(availableGestures.firstOrNull { it != triggerToChange } ?: availableGestures.firstOrNull() ?: "") }
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        
                        AlertDialog(
                            onDismissRequest = { showChangeTriggerDialog = false },
                            title = { Text("Change Gesture for ${gestureLabels[triggerToChange] ?: triggerToChange}") },
                            text = {
                                Column {
                                    if (availableGestures.size <= 1) {
                                        Text("No other available gestures.")
                                    } else {
                                        ActionDropdown(
                                            "Select New Gesture", 
                                            selectedNewGesture, 
                                            availableGestures.filter { it != triggerToChange }.map { it to (gestureLabels[it] ?: it) }
                                        ) { selectedNewGesture = it }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (selectedNewGesture.isNotEmpty() && selectedNewGesture != triggerToChange && availableGestures.contains(selectedNewGesture)) {
                                        migrateGesture(triggerToChange, selectedNewGesture)
                                        
                                        // Notify service to reload configuration
                                        val intent = android.content.Intent(localContext, com.example.core.HandleService::class.java).apply {
                                            action = "UPDATE_CONFIG"
                                        }
                                        localContext.startService(intent)
                                        
                                        showChangeTriggerDialog = false
                                    } else {
                                        showChangeTriggerDialog = false
                                    }
                                }) {
                                    Text("Change")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showChangeTriggerDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    
                    if (showChangeGestureDialog) {
                        val context = LocalContext.current
                        val pageConfigs = com.example.utils.PageManager.getPages(prefs, handle.id)
                        
                        val categoryOptions = listOf(
                            "page" to "Page",
                            "element" to "Action/Element",
                            "sidebar" to "Sidebar"
                        )
                        var selectedCategory by remember { mutableStateOf(categoryOptions.first().first) }
                        var selectedPageType by remember { mutableStateOf(if (pageConfigs.isNotEmpty()) pageConfigs.first().type else "") }
                        
                        AlertDialog(
                            onDismissRequest = { showChangeGestureDialog = false },
                            title = { Text("Change Action for ${gestureLabels[gestureToChange] ?: gestureToChange}") },
                            text = {
                                Column {
                                    ActionDropdown("Type/Content", selectedCategory, categoryOptions) { selectedCategory = it }
                                    
                                    if (selectedCategory == "page") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val pageOptions = listOf(
                                            "apps" to "Apps Grid",
                                            "widgets_grid" to "Widgets Grid",
                                            "hybrid_grid" to "Hybrid Grid",
                                            "app_tracker" to "App Tracker",
                                            "resources_tracker" to "Resources Tracker",
                            "media_player" to "Media Player",
                            "widget" to "Android Widget",
                                            "calculator" to "Calculator",
                                            "scheduler" to "Short Reminders",
                                            "compass" to "Compass",
                                            "notifications" to "Notifications"
                                        )
                                        if (selectedPageType.isEmpty()) selectedPageType = "apps"
                                        ActionDropdown("Select Page", selectedPageType, pageOptions) { selectedPageType = it }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (selectedCategory == "sidebar") {
                                        updateGesture(gestureToChange, "toggle_sidebar")
                                        showChangeGestureDialog = false
                                    } else if (selectedCategory == "page") {
                                        if (selectedPageType.isNotEmpty()) {
                                            updateGesture(gestureToChange, "open_page:$selectedPageType")
                                            showChangeGestureDialog = false
                                        }
                                    } else if (selectedCategory == "element") {
                                        // Not supported in this phase
                                        showChangeGestureDialog = false
                                    }
                                }) {
                                    Text("Change")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showChangeGestureDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    if (showAddGestureDialog) {
                        val context = LocalContext.current
                        val pageConfigs = com.example.utils.PageManager.getPages(prefs, handle.id)
                        var selectedGesture by remember { mutableStateOf(gestureKeys.first { !gesturesMap.containsKey(it) } ?: gestureKeys.first()) }
                        
                        val categoryOptions = listOf(
                            "page" to "Page",
                            "element" to "Action/Element",
                            "sidebar" to "Sidebar"
                        )
                        var selectedCategory by remember { mutableStateOf(categoryOptions.first().first) }
                        var selectedPageType by remember { mutableStateOf(if (pageConfigs.isNotEmpty()) pageConfigs.first().type else "") }
                        
                        AlertDialog(
                            onDismissRequest = { showAddGestureDialog = false },
                            title = { Text("Add Gesture") },
                            text = {
                                Column {
                                    ActionDropdown("Select Gesture", selectedGesture, gestureKeys.filter { !gesturesMap.containsKey(it) }.map { it to (gestureLabels[it] ?: it) }) { selectedGesture = it }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ActionDropdown("Type/Content", selectedCategory, categoryOptions) { selectedCategory = it }
                                    
                                    if (selectedCategory == "page") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val pageOptions = listOf(
                                            "apps" to "Apps Grid",
                                            "widgets_grid" to "Widgets Grid",
                                            "hybrid_grid" to "Hybrid Grid",
                                            "app_tracker" to "App Tracker",
                                            "resources_tracker" to "Resources Tracker",
                            "media_player" to "Media Player",
                            "widget" to "Android Widget",
                                            "calculator" to "Calculator",
                                            "scheduler" to "Short Reminders",
                                            "compass" to "Compass",
                                            "notifications" to "Notifications"
                                        )
                                        if (selectedPageType.isEmpty()) selectedPageType = "apps"
                                        ActionDropdown("Select Page", selectedPageType, pageOptions) { selectedPageType = it }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (selectedCategory == "sidebar") {
                                        updateGesture(selectedGesture, "toggle_sidebar")
                                        showAddGestureDialog = false
                                    } else if (selectedCategory == "page") {
                                        if (selectedPageType.isNotEmpty()) {
                                            updateGesture(selectedGesture, "open_page:$selectedPageType")
                                            showAddGestureDialog = false
                                        }
                                    } else if (selectedCategory == "element") {
                                        // Not supported in this phase
                                        showAddGestureDialog = false
                                    }
                                }) {
                                    Text("Add")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddGestureDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        var newName by remember { mutableStateOf(handle.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Handle") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdate(handle.copy(name = newName))
                    showRenameDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
