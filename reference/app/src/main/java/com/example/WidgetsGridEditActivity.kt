package com.example

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.service.GridWidgetItem
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class WidgetsGridEditActivity : ComponentActivity() {
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var pageId: String
    private lateinit var appWidgetManager: AppWidgetManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageId = intent.getStringExtra("PAGE_ID") ?: run {
            finish()
            return
        }
        prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        appWidgetManager = AppWidgetManager.getInstance(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    WidgetGridEditor(
                        pageId = pageId,
                        prefs = prefs,
                        appWidgetManager = appWidgetManager,
                        onClose = { finish() },
                        onAddWidget = {
                            val intent = Intent(this@WidgetsGridEditActivity, WidgetPickerActivity::class.java).apply {
                                putExtra("ACTION_TYPE", "RETURN_ID")
                            }
                            startActivityForResult(intent, 201)
                        }
                    )
                }
            }
        }
        
        registerReceiver(receiver, IntentFilter("WIDGET_ADDED_TO_GRID"), Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 201 && resultCode == Activity.RESULT_OK && data != null) {
            val elementId = data.getStringExtra("ELEMENT_ID")
            if (elementId != null) {
                // Add widget to prefs directly and reload
                val prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
                val itemsJson = prefs.getString("widgets_grid_$pageId", "[]") ?: "[]"
                val arr = org.json.JSONArray(itemsJson)
                val parsedItems = mutableListOf<com.example.service.GridWidgetItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i)
                    if (obj != null) {
                        val idStr = if (obj.has("id")) {
                            val rawId = obj.get("id")
                            if (rawId is Int) "widget:$rawId" else rawId.toString()
                        } else ""
                        if (idStr.isNotEmpty()) {
                            parsedItems.add(com.example.service.GridWidgetItem(
                                id = idStr,
                                cols = obj.optInt("cols", 1),
                                rows = obj.optInt("rows", 1),
                                x = obj.optInt("x", 0),
                                y = obj.optInt("y", 0)
                            ))
                        }
                    }
                }
                var defaultCols = if (elementId.startsWith("widget:")) 2 else 1
                var defaultRows = if (elementId.startsWith("widget:")) 2 else 1
                if (elementId.startsWith("widget:")) {
                    try {
                        val parts = elementId.split(":", limit = 3)
                        if (parts.size >= 3) {
                            val json = org.json.JSONObject(parts[2])
                            if (json.has("cols")) defaultCols = json.getInt("cols")
                            if (json.has("rows")) defaultRows = json.getInt("rows")
                        }
                    } catch (e: Exception) {}
                }
                val totalCols = prefs.getInt("widgets_grid_cols_$pageId", 4)
                if (defaultCols > totalCols) {
                    android.widget.Toast.makeText(this, "Cannot add: Requires $defaultCols columns, but grid only has $totalCols.", android.widget.Toast.LENGTH_LONG).show()
                    return
                }

                var targetX = 0
                var targetY = 0
                var found = false
                var searchY = 0
                while (!found && searchY < 100) {
                    for (searchX in 0..totalCols - defaultCols + 1) {
                        if (searchX + defaultCols > totalCols) continue
                        var overlap = false
                        for (item in parsedItems) {
                            if (searchX < item.x + item.cols && searchX + defaultCols > item.x &&
                                searchY < item.y + item.rows && searchY + defaultRows > item.y) {
                                overlap = true
                                break
                            }
                        }
                        if (!overlap) {
                            targetX = searchX
                            targetY = searchY
                            found = true
                            break
                        }
                    }
                    if (!found) searchY++
                }

                parsedItems.add(com.example.service.GridWidgetItem(
                    id = elementId,
                    cols = defaultCols,
                    rows = defaultRows,
                    x = targetX,
                    y = targetY
                ))
                val newArr = org.json.JSONArray()
                parsedItems.forEach {
                    val obj = org.json.JSONObject()
                    obj.put("id", it.id)
                    obj.put("cols", it.cols)
                    obj.put("rows", it.rows)
                    obj.put("x", it.x)
                    obj.put("y", it.y)
                    newArr.put(obj)
                }
                prefs.edit().putString("widgets_grid_$pageId", newArr.toString()).apply()
                
                val intent = Intent("WIDGET_ADDED_TO_GRID")
                intent.putExtra("PAGE_ID", pageId)
                intent.setPackage(packageName)
                sendBroadcast(intent)
                
                // Refresh activity UI
                recreate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val updateIntent = Intent("UPDATE_GRID")
            updateIntent.putExtra("PAGE_ID", pageId)
            updateIntent.setPackage(packageName)
            sendBroadcast(updateIntent)
            unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // we will let compose react to changes by observing something? 
            // Better yet, just re-read when needed. We'll handle state in Compose.
        }
    }
}
@Composable
fun WidgetGridEditor(
    pageId: String,
    prefs: android.content.SharedPreferences,
    appWidgetManager: AppWidgetManager,
    onClose: () -> Unit,
    onAddWidget: () -> Unit
) {
    var cols by remember { mutableStateOf(prefs.getInt("widgets_grid_cols_$pageId", 4)) }
    var items by remember { mutableStateOf(loadLocalItems(prefs, pageId)) }
    
    // Auto-save when items or cols change
    LaunchedEffect(cols) {
        prefs.edit().putInt("widgets_grid_cols_$pageId", cols).apply()
        saveItems(prefs, pageId, items)
    }
    
    LaunchedEffect(items) {
        saveItems(prefs, pageId, items)
    }

    // Force reload when broadcast is received (we can do a simple poll or just rely on state)
    // For simplicity, a small side-effect listener for the broadcast could be added, but we update `items` directly when possible.
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Edit Widgets Grid", fontSize = 20.sp, color = Color.White)
            Button(onClick = onClose) {
                Text("Done")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Columns: $cols", color = Color.LightGray)
            Row {
                IconButton(onClick = { if (cols > 1) cols-- }) {
                    Icon(painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_rew), contentDescription = "Decrease", tint = Color.White)
                }
                IconButton(onClick = { if (cols < 8) cols++ }) {
                    Icon(painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_ff), contentDescription = "Increase", tint = Color.White)
                }
            }
        }
        
        Button(onClick = onAddWidget, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Add Widget")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Widget")
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color.DarkGray)
                .verticalScroll(rememberScrollState())
        ) {
            GridEditorCanvas(
                items = items,
                cols = cols,
                appWidgetManager = appWidgetManager,
                onUpdateItems = { newItems -> items = newItems }
            )
        }
    }
}

@Composable
fun GridEditorCanvas(
    items: List<GridWidgetItem>,
    cols: Int,
    appWidgetManager: AppWidgetManager,
    onUpdateItems: (List<GridWidgetItem>) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(2000.dp)) {
        val cellWidth = maxWidth / cols
        val cellHeight = cellWidth // Square cells
        val cellWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { cellWidth.toPx() }
        val cellHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { cellHeight.toPx() }

        // Draw grid lines
        for (i in 0..cols) {
            Box(
                modifier = Modifier
                    .offset(x = cellWidth * i)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF333333))
            )
        }
        for (i in 0..40) {
            Box(
                modifier = Modifier
                    .offset(y = cellHeight * i)
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF333333))
            )
        }

        items.forEachIndexed { index, item ->
            var offsetX by remember(item.id, item.x, cellWidthPx) { mutableStateOf(item.x * cellWidthPx) }
            var offsetY by remember(item.id, item.y, cellHeightPx) { mutableStateOf(item.y * cellHeightPx) }
            var isDragging by remember { mutableStateOf(false) }
            
            var resizeDx by remember { mutableStateOf(0f) }
            var resizeDy by remember { mutableStateOf(0f) }
            var isResizing by remember { mutableStateOf(false) }

            val currentWidthPx = item.cols * cellWidthPx + resizeDx
            val currentHeightPx = item.rows * cellHeightPx + resizeDy

            val zIndex = if (isDragging || isResizing) 1f else 0f

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(
                        width = with(androidx.compose.ui.platform.LocalDensity.current) { currentWidthPx.toDp() },
                        height = with(androidx.compose.ui.platform.LocalDensity.current) { currentHeightPx.toDp() }
                    )
                    .zIndex(zIndex)
                    .padding(2.dp)
                    .background(if (isDragging) Color(0xAA4CAF50) else Color(0xFF4CAF50))
                    .border(2.dp, Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                // Snap to grid
                                val gridX = (offsetX / cellWidthPx).roundToInt().coerceIn(0, maxOf(0, cols - item.cols))
                                val gridY = (offsetY / cellHeightPx).roundToInt().coerceAtLeast(0)
                                offsetX = gridX * cellWidthPx
                                offsetY = gridY * cellHeightPx
                                
                                val newItems = items.toMutableList()
                                newItems[index] = item.copy(x = gridX, y = gridY)
                                onUpdateItems(newItems)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        )
                    }
            ) {
                // Delete button
                IconButton(
                    onClick = {
                        val newItems = items.toMutableList()
                        newItems.removeAt(index)
                        onUpdateItems(newItems)
                    },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(4.dp).background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                // Label
                Text(
                    text = getWidgetName(androidx.compose.ui.platform.LocalContext.current, item.id, appWidgetManager),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center).padding(8.dp)
                )

                // Resize handle (bottom right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(Color.Blue)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isResizing = true },
                                onDragEnd = {
                                    isResizing = false
                                    // Snap resize to grid
                                    val finalCols = ((currentWidthPx) / cellWidthPx).roundToInt().coerceIn(1, maxOf(1, cols - item.x))
                                    val finalRows = ((currentHeightPx) / cellHeightPx).roundToInt().coerceAtLeast(1)
                                    
                                    resizeDx = 0f
                                    resizeDy = 0f
                                    
                                    val newItems = items.toMutableList()
                                    newItems[index] = item.copy(cols = finalCols, rows = finalRows)
                                    onUpdateItems(newItems)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    resizeDx += dragAmount.x
                                    resizeDy += dragAmount.y
                                }
                            )
                        }
                ) {
                    Icon(painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_crop), contentDescription = "Resize", tint = Color.White)
                }
            }
        }
    }
}

fun getWidgetName(context: Context, id: String, appWidgetManager: AppWidgetManager): String {
    if (id.startsWith("widget:")) {
        val wId = id.removePrefix("widget:").substringBefore(":").toIntOrNull() ?: return "Unknown Widget"
        val info = appWidgetManager.getAppWidgetInfo(wId)
        return info?.loadLabel(context.packageManager) ?: "Widget $wId"
    } else {
        return id
    }
}

fun loadLocalItems(prefs: android.content.SharedPreferences, pageId: String): List<GridWidgetItem> {
    val jsonStr = prefs.getString("widgets_grid_$pageId", "[]") ?: "[]"
    val arr = JSONArray(jsonStr)
    val list = mutableListOf<GridWidgetItem>()
    for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i)
        if (obj != null) {
            val idStr = if (obj.has("elementId")) obj.getString("elementId") 
                         else if (obj.has("id")) {
                            val rawId = obj.get("id")
                            if (rawId is Int) "widget:$rawId" else rawId.toString()
                        } else ""
            if (idStr.isNotEmpty()) {
                list.add(GridWidgetItem(
                    idStr,
                    obj.optInt("cols", 2),
                    obj.optInt("rows", 2),
                    obj.optInt("x", 0),
                    obj.optInt("y", 0)
                ))
            }
        } else {
            val id = arr.optInt(i, -1)
            if (id != -1) {
                list.add(GridWidgetItem("widget:$id", 2, 2, 0, 0))
            }
        }
    }
    return list
}

fun saveItems(prefs: android.content.SharedPreferences, pageId: String, items: List<GridWidgetItem>) {
    val arr = JSONArray()
    items.forEach { 
        val obj = JSONObject()
        obj.put("id", it.id)
        obj.put("cols", it.cols)
        obj.put("rows", it.rows)
        obj.put("x", it.x)
        obj.put("y", it.y)
        arr.put(obj)
    }
    prefs.edit().putString("widgets_grid_$pageId", arr.toString()).apply()
}
