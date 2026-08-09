package com.example.feature.settings
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.utils.PageManager
import com.example.utils.SidebarPage
import java.util.UUID
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SidebarSettingsScreen(handleId: String, initAction: String? = null, onBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val maxScreenWidth = configuration.screenWidthDp.toFloat()
    val maxScreenHeight = configuration.screenHeightDp.toFloat()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE) }
    var customisingPage by remember { mutableStateOf<SidebarPage?>(null) }
    var selectedActionPage by remember { mutableStateOf<SidebarPage?>(null) }
    var pageActionIndex by remember { mutableStateOf(-1) }
    // Pages
    var pages by remember { mutableStateOf(PageManager.getPages(prefs, handleId)) }
    var defaultIndex by remember { mutableStateOf(PageManager.getDefaultPageIndex(prefs, handleId)) }

    LaunchedEffect(initAction) {
        if (initAction != null && initAction.startsWith("open_page:")) {
            val type = initAction.removePrefix("open_page:")
            var index = pages.indexOfFirst { it.type == type }
            if (index == -1) {
                val title = when (type) {
                    "apps" -> "Apps Grid"
                    "scheduler" -> "Short Reminders"
                    "calculator" -> "Calculator"
                    "compass" -> "Compass"
                    "notifications" -> "Notifications"
                    "widgets_grid" -> "Widgets Grid"
                    "hybrid_grid" -> "Hybrid Grid"
                    "app_tracker" -> "App Tracker"
                    "resources_tracker" -> "Resources Tracker"
                    else -> type.replaceFirstChar { it.uppercase() }
                }
                val newPage = com.example.utils.SidebarPage.createDefault(id = UUID.randomUUID().toString(), type = type, title = title)
                val newPages = pages.toMutableList()
                newPages.add(newPage)
                pages = newPages
                PageManager.savePages(prefs, handleId, newPages)
                index = newPages.size - 1
            }
            if (defaultIndex != index) {
                defaultIndex = index
                PageManager.saveDefaultPageIndex(prefs, handleId, index)
            }
        }
    }
    if (customisingPage != null) {
        PageCustomizeScreen(
            page = customisingPage!!,
            handleId = handleId,
            onSave = { updated ->
                val newPages = PageManager.getPages(prefs, handleId).toMutableList()
                val idx = newPages.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    newPages[idx] = updated
                    PageManager.savePages(prefs, handleId, newPages)
                    pages = newPages
                }
            },
            onBack = {
                customisingPage = null
            }
        )
        return
    }
    // Sidebar options
    var sidebarColumns by remember { mutableStateOf(prefs.getInt("handle_${handleId}_sidebar_columns", prefs.getInt("sidebar_columns", 3))) }
    var sidebarWidth by remember { mutableStateOf(prefs.getInt("handle_${handleId}_sidebar_width", prefs.getInt("sidebar_width", 216))) }
    var sidebarHeight by remember { mutableStateOf(prefs.getInt("handle_${handleId}_sidebar_height", prefs.getInt("sidebar_height", 360))) }
    var sidebarWrapContent by remember { mutableStateOf(prefs.getBoolean("handle_${handleId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))) }
    var sidebarColorHex by remember { mutableStateOf(prefs.getString("handle_${handleId}_sidebar_color", prefs.getString("sidebar_color", "#000000")) ?: "#000000") }
    var sidebarTransparency by remember { mutableStateOf(prefs.getFloat("handle_${handleId}_sidebar_transparency", prefs.getFloat("sidebar_transparency", 0.9f))) }
    var showAddDialog by remember { mutableStateOf(false) }
    fun savePages() {
        PageManager.savePages(prefs, handleId, pages)
        PageManager.saveDefaultPageIndex(prefs, handleId, defaultIndex)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sidebar Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        savePages()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Page")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item {
                Text(
                    text = "Appearance & Layout",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp, 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Width") },
                    supportingContent = {
                        Slider(
                            value = sidebarWidth.toFloat(),
                            onValueChange = { 
                                sidebarWidth = it.toInt()
                                prefs.edit().putInt("handle_${handleId}_sidebar_width", it.toInt()).apply()
                            },
                            valueRange = 100f..maxScreenWidth,
                                steps = ((maxScreenWidth - 100f) / 10f).toInt()
                        )
                    },
                    trailingContent = { Text("${sidebarWidth}dp") }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Height (Max)") },
                    supportingContent = {
                        Slider(
                            value = sidebarHeight.toFloat(),
                            onValueChange = { 
                                sidebarHeight = it.toInt()
                                prefs.edit().putInt("handle_${handleId}_sidebar_height", it.toInt()).apply()
                            },
                            valueRange = 300f..maxScreenHeight,
                                steps = ((maxScreenHeight - 300f) / 10f).toInt()
                        )
                    },
                    trailingContent = { Text("${sidebarHeight}dp") }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Wrap Content Height") },
                    supportingContent = { Text("Shrink to fit content instead of fixed height") },
                    trailingContent = {
                        Switch(
                            checked = sidebarWrapContent,
                            onCheckedChange = { 
                                sidebarWrapContent = it
                                prefs.edit().putBoolean("handle_${handleId}_sidebar_wrap_content", it).apply()
                            }
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Sidebar Color") },
                    supportingContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val presetColors = listOf(
                                "#000000", "#FFFFFF", "#FF5252", "#4CAF50", "#2196F3", "#FFEB3B", "#87CEEB"
                            )
                            presetColors.forEach { colorString ->
                                val parsedColor = try {
                                    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorString))
                                } catch (e: Exception) {
                                    androidx.compose.ui.graphics.Color.Gray
                                }
                                val baseColorStr = if (colorString.length >= 7) colorString.substring(colorString.length - 6) else colorString
                                val currentBaseStr = if (sidebarColorHex.length >= 7) sidebarColorHex.substring(sidebarColorHex.length - 6) else sidebarColorHex
                                val isSelected = baseColorStr.equals(currentBaseStr, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(parsedColor, androidx.compose.foundation.shape.CircleShape)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Gray,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                        .clickable {
                                            sidebarColorHex = colorString
                                            prefs.edit().putString("handle_${handleId}_sidebar_color", colorString).apply()
                                        }
                                )
                            }
                        }
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Background Opacity") },
                    supportingContent = {
                        Slider(
                            value = sidebarTransparency,
                            onValueChange = { 
                                sidebarTransparency = it
                                prefs.edit().putFloat("handle_${handleId}_sidebar_transparency", it).apply()
                            },
                            valueRange = 0f..1f,
                            steps = 20
                        )
                    },
                    trailingContent = { Text("${(sidebarTransparency * 100).toInt()}%") }
                )
                
                Divider()
                Text(
                    text = "Pages Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp, 8.dp)
                )
            }
            itemsIndexed(pages) { index, page ->
                Box {
                    ListItem(
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                defaultIndex = index
                                savePages()
                            },
                            onLongClick = {
                                if (index > 0 && pages.size > 1) { // don't allow editing/removing default Apps Grid
                                    selectedActionPage = page
                                    pageActionIndex = index
                                }
                            }
                        ),
                        leadingContent = {
                            androidx.compose.material3.RadioButton(
                                selected = defaultIndex == index,
                                onClick = { 
                                    defaultIndex = index
                                    savePages()
                                }
                            )
                        },
                        headlineContent = { Text(page.title) },
                        supportingContent = { Text(page.type.replace("_", " ").capitalize()) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    if (index > 1) { // 1 instead of 0 to protect index 0 apps grid
                                        val newPages = pages.toMutableList()
                                        val temp = newPages[index]
                                        newPages[index] = newPages[index - 1]
                                        newPages[index - 1] = temp
                                        if (defaultIndex == index) defaultIndex = index - 1
                                        else if (defaultIndex == index - 1) defaultIndex = index
                                        pages = newPages
                                        savePages()
                                    }
                                }, enabled = index > 1) {
                                    Icon(Icons.Default.ArrowUpward, "Up")
                                }
                                IconButton(onClick = {
                                    if (index > 0 && index < pages.size - 1) {
                                        val newPages = pages.toMutableList()
                                        val temp = newPages[index]
                                        newPages[index] = newPages[index + 1]
                                        newPages[index + 1] = temp
                                        if (defaultIndex == index) defaultIndex = index + 1
                                        else if (defaultIndex == index + 1) defaultIndex = index
                                        pages = newPages
                                        savePages()
                                    }
                                }, enabled = index > 0 && index < pages.size - 1) {
                                    Icon(Icons.Default.ArrowDownward, "Down")
                                }
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = selectedActionPage == page && pageActionIndex == index,
                        onDismissRequest = {
                            selectedActionPage = null
                            pageActionIndex = -1
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit / Customize") },
                            onClick = {
                                customisingPage = selectedActionPage
                                selectedActionPage = null
                                pageActionIndex = -1
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                val newPages = pages.toMutableList()
                                newPages.removeAt(pageActionIndex)
                                if (defaultIndex == pageActionIndex) defaultIndex = 0
                                else if (defaultIndex > pageActionIndex) defaultIndex--
                                pages = newPages
                                savePages()
                                selectedActionPage = null
                                pageActionIndex = -1
                            }
                        )
                    }
                }
                Divider()
            }
            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
            }
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Sidebar Page") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        val types = listOf(
                            "apps" to "Apps Grid",
                            "scheduler" to "Short Reminders",
                            "calculator" to "Calculator",
                            "compass" to "Compass",
                            "notifications" to "Notifications",
                            "widgets_grid" to "Widgets Grid",
                            "hybrid_grid" to "Hybrid Grid",
                            "app_tracker" to "App Tracker",
                            "resources_tracker" to "Resources Tracker",
                        )
                        types.forEach { (type, title) ->
                            TextButton(onClick = {
                                val newPages = pages.toMutableList()
                                val newPage = com.example.utils.SidebarPage.createDefault(id = UUID.randomUUID().toString(), type = type, title = title)
                                newPages.add(newPage)
                                pages = newPages
                                savePages()
                                showAddDialog = false
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(title, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
