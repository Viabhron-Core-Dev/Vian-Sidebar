package com.example.feature.sidebar
import com.example.service.FloatingReaderService
import com.example.service.VianSideAccessibilityService
import com.example.service.AppWidgetHelper
import com.example.service.DisplayHandler
import com.example.service.MediaVolumeHandler
import com.example.service.QuickTileHandler
import com.example.service.FloatingTriggerService

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import kotlin.math.max
import android.widget.ScrollView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class HybridGridPageView(
    context: Context,
    private val pageId: String,
    private val onHeightChanged: (Int) -> Unit
) : FrameLayout(context) {

    private val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)

    private val appsManager = SidebarAppsManager(context, prefs, CoroutineScope(Dispatchers.IO), "hg_${pageId}") {
        post { loadWidgets() }
    }

    private val gridLayout = FrameLayout(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ELEMENT_ADDED_TO_HYBRID" || intent?.action == "UPDATE_GRID") {
                val targetPageId = intent.getStringExtra("PAGE_ID")
                if (targetPageId == pageId) {
                    val widgetId = intent.getIntExtra("WIDGET_ID", -1)
                    if (widgetId != -1) {
                        addWidgetIdToPrefs(widgetId)
                    }
                    val elementId = intent.getStringExtra("ELEMENT_ID")
                    if (elementId != null) {
                        addElementIdToPrefs(elementId)
                    }
                    loadWidgets()
                }
            } else if (intent?.action == "com.example.UPDATE_SIDEBAR_ICONS") {
                loadWidgets()
            }
        }
    }

    init {
        appsManager.ensureLoaded()
        com.example.core.LogKeeper.writeLog("HybridGrid", "Opened widgets grid page")
        val scrollView = ScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            addView(gridLayout)
        }
        addView(scrollView)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && oldw != w) {
            post { loadWidgets() }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { loadWidgets() }
        val filter = IntentFilter()
        filter.addAction("ELEMENT_ADDED_TO_HYBRID")
        filter.addAction("UPDATE_GRID")
        filter.addAction("com.example.UPDATE_SIDEBAR_ICONS")
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {}
    }

    private fun getWidgetItems(): List<GridWidgetItem> {
        var jsonStr = prefs.getString("hybrid_grid_$pageId", null)
        val isModified = prefs.getBoolean("hybrid_grid_modified_$pageId", false)
        if (jsonStr == null || (jsonStr == "[]" && pageId.startsWith("default_hybrid") && !isModified)) {
            if (pageId.startsWith("default_hybrid")) {
                jsonStr = """[{"id": "system:ebook_reader", "cols": 1, "rows": 1, "x": 0, "y": 0}, {"id": "system:log_keeper", "cols": 1, "rows": 1, "x": 1, "y": 0}]"""
            } else {
                jsonStr = "[]"
            }
        }
        
        val arr = JSONArray(jsonStr)
        val list = mutableListOf<GridWidgetItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i)
            if (obj != null) {
                val idStr = if (obj.has("id")) {
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

    private fun saveWidgetItems(items: List<GridWidgetItem>) {
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
        prefs.edit().putString("hybrid_grid_$pageId", arr.toString())
            .putBoolean("hybrid_grid_modified_$pageId", true)
            .apply()
    }

    private fun addWidgetIdToPrefs(widgetId: Int) {
        val items = getWidgetItems().toMutableList()
        // Default size 2x2
        items.add(GridWidgetItem("widget:$widgetId", 2, 2, 0, 0))
        saveWidgetItems(items)
    }
    
    private fun addElementIdToPrefs(elementId: String) {
        val items = getWidgetItems().toMutableList()
        // Default size 1x1 for elements
        items.add(GridWidgetItem(elementId, 1, 1, 0, 0))
        saveWidgetItems(items)
    }

    fun getCurrentHeightPx(): Int {
        if (gridLayout.childCount == 0) {
            return 0
        }
        val lpHeight = gridLayout.layoutParams?.height ?: 0
        if (lpHeight > 0) return lpHeight
        
        gridLayout.measure(
            View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.widthPixels, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return gridLayout.measuredHeight
    }

    fun loadWidgets() {
        if (width == 0) {
            return
        }
        gridLayout.removeAllViews()
        val totalCols = prefs.getInt("hybrid_grid_cols_$pageId", 4)
        
        val gridWidth = width
        val cellWidth = gridWidth / totalCols
        val cellHeight = cellWidth 
        
        val items = getWidgetItems()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val host = AppWidgetHelper.getHost(context)
            
            
            var maxHeight = 0
            for (item in items) {
                try {
                    if (item.id.startsWith("widget:")) {
                        val wId = item.id.removePrefix("widget:").substringBefore(":").toIntOrNull() ?: continue
                        val info = appWidgetManager.getAppWidgetInfo(wId)
                        if (info != null) {
                            val hostView = host.createView(context, wId, info)
                            hostView.setPadding(0, 0, 0, 0)
                            
                            val wCols = minOf(item.cols, totalCols)
                            val wRows = item.rows
                            
                            val params = FrameLayout.LayoutParams(cellWidth * wCols, cellHeight * wRows).apply {
                                leftMargin = item.x * cellWidth
                                topMargin = item.y * cellHeight
                            }
                            gridLayout.addView(hostView, params)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                val density = context.resources.displayMetrics.density
                                val minW = ((cellWidth * wCols) / density).toInt()
                                val minH = ((cellHeight * wRows) / density).toInt()
                                hostView.updateAppWidgetSize(null, minW, minH, minW, minH)
                            }
                            maxHeight = max(maxHeight, item.y * cellHeight + cellHeight * wRows)
                            
                            hostView.setOnLongClickListener {
                                val actionList = mutableListOf("App Info", "Remove")

                                var popupWindow: PopupWindow? = null
                                val popupLayout = LinearLayout(context).apply {
                                    orientation = LinearLayout.VERTICAL
                                    val pad = (8 * context.resources.displayMetrics.density).toInt()
                                    setPadding(pad, pad, pad, pad)
                                }

                                actionList.forEach { action ->
                                    val actionView = TextView(context).apply {
                                        text = action
                                        setTextColor(Color.WHITE)
                                        setPadding(0, (12 * context.resources.displayMetrics.density).toInt(), 0, (12 * context.resources.displayMetrics.density).toInt())
                                        gravity = Gravity.CENTER
                                        
                                        val shape = android.graphics.drawable.GradientDrawable()
                                        shape.cornerRadius = 8 * context.resources.displayMetrics.density
                                        shape.setColor(Color.parseColor("#333333"))
                                        shape.setStroke(1, Color.LTGRAY)
                                        background = shape
                                        
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            setMargins(0, 0, 0, (8 * context.resources.displayMetrics.density).toInt())
                                        }
                                        
                                        setOnClickListener {
                                            popupWindow?.dismiss()
                                            when (action) {
                                                "Remove" -> {
                                                    val newItems = items.toMutableList()
                                                    newItems.removeAll { it.id == item.id }
                                                    saveWidgetItems(newItems)
                                                    context.sendBroadcast(Intent("ELEMENT_ADDED_TO_HYBRID").apply { putExtra("PAGE_ID", pageId) })
                                                }
                                                "App Info" -> {
                                                    try {
                                                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                        intent.data = android.net.Uri.parse("package:${info.provider.packageName}")
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        }
                                    }
                                    popupLayout.addView(actionView)
                                }
                                
                                popupLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                                popupWindow = PopupWindow(
                                    popupLayout,
                                    (150 * context.resources.displayMetrics.density).toInt(),
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    true
                                ).apply {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                    } else {
                                        @Suppress("DEPRECATION")
                                        windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_PHONE
                                    }
                                    setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                                    isOutsideTouchable = true
                                }
                                val location = IntArray(2)
                                hostView.getLocationOnScreen(location)
                                val x = location[0]
                                var y = location[1] - popupLayout.measuredHeight
                                if (y < 0) y = location[1] + hostView.height
                                popupWindow?.showAtLocation(hostView, Gravity.NO_GRAVITY, x, y)
                                true
                            }
                        }
                    } else {
                        val parsed = appsManager.parseId(item.id)
                        if (parsed != null) {
                            val elementView = android.view.LayoutInflater.from(context).inflate(com.example.R.layout.item_sidebar_app, null, false)
                            val icon = elementView.findViewById<android.widget.ImageView>(com.example.R.id.app_icon)
                            val label = elementView.findViewById<android.widget.TextView>(com.example.R.id.app_label)
                            icon.maxWidth = Int.MAX_VALUE
                            icon.maxHeight = Int.MAX_VALUE
                            val lp = icon.layoutParams as android.widget.LinearLayout.LayoutParams
                            lp.height = 0
                            lp.weight = 1f
                            icon.layoutParams = lp
                            
                            label.text = parsed.label
                            
                            appsManager.bindIcon(item.id, icon, prefs, CoroutineScope(Dispatchers.Main)) {
                                appsManager.bindIcon(item.id, icon, prefs, CoroutineScope(Dispatchers.Main)) {}
                            }
                            
                            elementView.setOnClickListener {
                                if (parsed is SidebarItem.App) {
                                    val intent = context.packageManager.getLaunchIntentForPackage(parsed.packageName)
                                    if (intent != null) {
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        try { context.startActivity(intent) } catch (e: Exception) {}
                                    }
                                } else if (parsed is SidebarItem.PageWindow) {
                                    com.example.feature.miniapps.MiniAppManager.toggleApp(context, parsed.pageType)
                                } else if (parsed is SidebarItem.FloatingTrigger) {
                                    val intent = Intent(context, FloatingTriggerService::class.java)
                                    intent.putExtra("TARGET_ID", parsed.targetId)
                                    context.startService(intent)
                                } else if (parsed is SidebarItem.Folder) {
                                    showFolderPopup(elementView, parsed, appsManager)
                                } else if (parsed is SidebarItem.PopupWidget) {
                                    showWidgetPopup(elementView, parsed)
                                } else if (parsed is SidebarItem.Link) {
                                    try {
                                        val intent = if (parsed.url.startsWith("intent:")) {
                                            Intent.parseUri(parsed.url, Intent.URI_INTENT_SCHEME)
                                        } else {
                                            Intent(Intent.ACTION_VIEW, android.net.Uri.parse(parsed.url))
                                        }
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                } else if (parsed is SidebarItem.QuickTile) {
                                    QuickTileHandler.handleQuickTileAction(context, parsed.action)
                                } else if (parsed is SidebarItem.IntentAction) {
                                    try {
                                        val intent = Intent.parseUri(parsed.uri, Intent.URI_INTENT_SCHEME)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                } else if (parsed is SidebarItem.SystemAction) {
                                    if (parsed.action == "screen_record") {
                                        val intent = Intent(context, ScreenRecordActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else {
                                        val intent = Intent(context, com.example.service.SidebarService::class.java)
                                        intent.action = "EXECUTE_ACTION"
                                        intent.putExtra("ACTION_ID", "system:" + parsed.action)
                                        context.startService(intent)
                                    }
                                } else if (parsed is SidebarItem.VolumeAction) {
                                    try {
                                        MediaVolumeHandler.handleVolumeAction(context, parsed.stream, parsed.action)
                                    } catch (e: Exception) {}
                                } else if (parsed is SidebarItem.MediaAction) {
                                    try {
                                        MediaVolumeHandler.handleMediaAction(context, parsed.action)
                                    } catch (e: Exception) {}
                                } else if (parsed is SidebarItem.DisplayAction) {
                                    try {
                                        DisplayHandler.handleDisplayAction(context, parsed.action)
                                    } catch (e: Exception) {}
                                } else if (parsed is SidebarItem.SettingsShortcut) {
                                    val settingsIntent = when (parsed.action) {
                                        "wifi" -> Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                                        "bluetooth" -> Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                                        "display" -> Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)
                                        "sound" -> Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                                        "location" -> Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        "apps" -> Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                                        "security" -> Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                                        "battery" -> Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
                                        "date" -> Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
                                        else -> Intent(android.provider.Settings.ACTION_SETTINGS)
                                    }
                                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    try { context.startActivity(settingsIntent) } catch (e: Exception) {}
                                }
                            }
                            
                            elementView.setOnLongClickListener {
                                val actionList = mutableListOf<String>()
                                if (parsed is SidebarItem.App) {
                                    actionList.add("App Info")
                                }
                                actionList.add("Change Icon")
                                val customIconFile = java.io.File(context.filesDir, "custom_icons/${item.id.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.webp")
                                if (customIconFile.exists()) {
                                    actionList.add("Reset Icon")
                                }
                                actionList.add("Remove")

                                var popupWindow: PopupWindow? = null
                                val popupLayout = LinearLayout(context).apply {
                                    orientation = LinearLayout.VERTICAL
                                    val pad = (8 * context.resources.displayMetrics.density).toInt()
                                    setPadding(pad, pad, pad, pad)
                                }

                                actionList.forEach { action ->
                                    val actionView = TextView(context).apply {
                                        text = action
                                        setTextColor(Color.WHITE)
                                        setPadding(0, (12 * context.resources.displayMetrics.density).toInt(), 0, (12 * context.resources.displayMetrics.density).toInt())
                                        gravity = Gravity.CENTER
                                        
                                        val shape = android.graphics.drawable.GradientDrawable()
                                        shape.cornerRadius = 8 * context.resources.displayMetrics.density
                                        shape.setColor(Color.parseColor("#333333"))
                                        shape.setStroke(1, Color.LTGRAY)
                                        background = shape
                                        
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            setMargins(0, 0, 0, (8 * context.resources.displayMetrics.density).toInt())
                                        }
                                        
                                        setOnClickListener {
                                            popupWindow?.dismiss()
                                            when (action) {
                                                "Remove" -> {
                                                    // Need to remove from items and save
                                                    val newItems = items.toMutableList()
                                                    newItems.removeAll { it.id == item.id }
                                                    saveWidgetItems(newItems)
                                                    // Trigger reload
                                                    context.sendBroadcast(Intent("ELEMENT_ADDED_TO_HYBRID").apply { putExtra("PAGE_ID", pageId) })
                                                }
                                                "Change Icon" -> {
                                                    val intent = Intent(context, com.example.IconPickerActivity::class.java).apply {
                                                        putExtra("item_id", item.id)
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                                "Reset Icon" -> {
                                                    val file = java.io.File(context.filesDir, "custom_icons/${item.id.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.webp")
                                                    if (file.exists()) file.delete()
                                                    appsManager.iconCache.remove("custom_${item.id}")
                                                    appsManager.iconCache.remove(item.id)
                                                    context.sendBroadcast(Intent("com.example.UPDATE_SIDEBAR_ICONS").apply {
                                                        putExtra("item_id", item.id)
                                                    })
                                                }
                                                "App Info" -> {
                                                    if (parsed is SidebarItem.App) {
                                                        try {
                                                            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                            intent.data = android.net.Uri.parse("package:${parsed.packageName}")
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {}
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    popupLayout.addView(actionView)
                                }
                                
                                popupLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                                popupWindow = PopupWindow(
                                    popupLayout,
                                    (150 * context.resources.displayMetrics.density).toInt(),
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    true
                                ).apply {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                                    } else {
                                        @Suppress("DEPRECATION")
                                        windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_PHONE
                                    }
                                    setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                                    isOutsideTouchable = true
                                }
                                val location = IntArray(2)
                                elementView.getLocationOnScreen(location)
                                val x = location[0]
                                var y = location[1] - popupLayout.measuredHeight
                                if (y < 0) y = location[1] + elementView.height
                                popupWindow?.showAtLocation(elementView, Gravity.NO_GRAVITY, x, y)
                                true
                            }
                            
                            val wCols = minOf(item.cols, totalCols)
                            val wRows = item.rows
                            
                            val params = FrameLayout.LayoutParams(cellWidth * wCols, cellHeight * wRows).apply {
                                leftMargin = item.x * cellWidth
                                topMargin = item.y * cellHeight
                            }
                            gridLayout.addView(elementView, params)
                            maxHeight = max(maxHeight, item.y * cellHeight + cellHeight * wRows)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            gridLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, maxHeight)
            onHeightChanged(getCurrentHeightPx())
    }

    private fun showFolderPopup(anchor: View, folder: SidebarItem.Folder, appsManager: SidebarAppsManager) {
        val density = context.resources.displayMetrics.density
        val popupView = FrameLayout(context)
        val recyclerView = RecyclerView(context)
        val padding = (16 * density).toInt()
        recyclerView.setPadding(padding, padding, padding, padding)
        popupView.addView(recyclerView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))

        val maxCols = if (folder.popupColumns > 0) folder.popupColumns else prefs.getInt("sidebar_columns", 3)
        val columns = if (folder.items.size <= maxCols && folder.items.isNotEmpty()) folder.items.size else maxCols
        val validCols = if (columns > 0) columns else 1
        
        recyclerView.layoutManager = GridLayoutManager(context, validCols)

        val popupOpacity = prefs.getFloat("sidebar_transparency", 0.9f)
        val popupBg = android.graphics.drawable.GradientDrawable()
        popupBg.setColor(Color.parseColor("#1A1A1A"))
        popupBg.alpha = (popupOpacity * 255).toInt()
        popupBg.cornerRadius = 16 * density
        popupView.background = popupBg

        val itemWidthDp = 72
        val itemHeightDp = 72
        val autoRows = Math.ceil(folder.items.size.toDouble() / validCols).toInt()
        val rows = if (folder.popupRows > 0) kotlin.math.min(folder.popupRows, autoRows) else autoRows
        val displayRows = if (folder.popupRows > 0) folder.popupRows else rows

        val totalWidth = (validCols * itemWidthDp * density + padding * 2).toInt()
        val totalHeight = (displayRows * itemHeightDp * density + padding * 2).toInt()
        
        popupView.layoutParams = ViewGroup.LayoutParams(totalWidth, totalHeight)

        var popupWindow: PopupWindow? = null
        
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = android.view.LayoutInflater.from(context).inflate(com.example.R.layout.item_sidebar_app, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }
            override fun getItemCount() = folder.items.size
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val itemId = folder.items[position]
                val parsed = appsManager.parseId(itemId) ?: return
                val icon = holder.itemView.findViewById<android.widget.ImageView>(com.example.R.id.app_icon)
                val label = holder.itemView.findViewById<android.widget.TextView>(com.example.R.id.app_label)
                label.text = parsed.label
                
                appsManager.bindIcon(itemId, icon, prefs, CoroutineScope(Dispatchers.Main)) {}
                
                holder.itemView.setOnClickListener {
                    if (parsed is SidebarItem.App) {
                        val intent = context.packageManager.getLaunchIntentForPackage(parsed.packageName)
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try { context.startActivity(intent) } catch (e: Exception) {}
                            popupWindow?.dismiss()
                        }
                    } else if (parsed is SidebarItem.PageWindow) {
                        com.example.feature.miniapps.MiniAppManager.toggleApp(context, parsed.pageType)
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.Link) {
                        try {
                            val intent = if (parsed.url.startsWith("intent:")) {
                                Intent.parseUri(parsed.url, Intent.URI_INTENT_SCHEME)
                            } else {
                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(parsed.url))
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.QuickTile) {
                        QuickTileHandler.handleQuickTileAction(context, parsed.action)
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.IntentAction) {
                        try {
                            val intent = Intent.parseUri(parsed.uri, Intent.URI_INTENT_SCHEME)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.SystemAction) {
                        if (parsed.action == "screen_record") {
                            val intent = Intent(context, ScreenRecordActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(context, com.example.service.SidebarService::class.java)
                            intent.action = "EXECUTE_ACTION"
                            intent.putExtra("ACTION_ID", "system:" + parsed.action)
                            context.startService(intent)
                        }
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.VolumeAction) {
                        try {
                            MediaVolumeHandler.handleVolumeAction(context, parsed.stream, parsed.action)
                        } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.MediaAction) {
                        try {
                            MediaVolumeHandler.handleMediaAction(context, parsed.action)
                        } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.DisplayAction) {
                        try {
                            DisplayHandler.handleDisplayAction(context, parsed.action)
                        } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    } else if (parsed is SidebarItem.SettingsShortcut) {
                        val settingsIntent = when (parsed.action) {
                            "wifi" -> Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                            "bluetooth" -> Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                            "display" -> Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)
                            "sound" -> Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                            "location" -> Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            "apps" -> Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                            "security" -> Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS)
                            "battery" -> Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
                            "date" -> Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
                            else -> Intent(android.provider.Settings.ACTION_SETTINGS)
                        }
                        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try { context.startActivity(settingsIntent) } catch (e: Exception) {}
                        popupWindow?.dismiss()
                    }
                }
                
                holder.itemView.setOnLongClickListener {
                    val actionList = mutableListOf<String>()
                    if (parsed is SidebarItem.App) {
                        actionList.add("App Info")
                    }
                    actionList.add("Change Icon")
                    val customIconFile = java.io.File(context.filesDir, "custom_icons/${itemId.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.webp")
                    if (customIconFile.exists()) {
                        actionList.add("Reset Icon")
                    }
                    actionList.add("Remove")
                    
                    var actionMenuPopup: PopupWindow? = null
                    val popupLayout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        val pad = (8 * density).toInt()
                        setPadding(pad, pad, pad, pad)
                    }
                    
                    actionList.forEach { action ->
                        val actionView = TextView(context).apply {
                            text = action
                            setTextColor(Color.BLACK)
                            val padV = (10 * density).toInt()
                            val padH = (16 * density).toInt()
                            setPadding(padH, padV, padH, padV)
                            textSize = 14f
                            
                            val shape = android.graphics.drawable.GradientDrawable()
                            shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                            shape.cornerRadius = 24f * density
                            shape.setColor(Color.WHITE)
                            shape.setStroke(1, Color.LTGRAY)
                            background = shape
                            
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, (8 * density).toInt())
                            }
                            
                            setOnClickListener {
                                actionMenuPopup?.dismiss()
                                when (action) {
                                    "Remove" -> {
                                        appsManager.removeItem(itemId)
                                        popupWindow?.dismiss()
                                    }
                                    "Change Icon" -> {
                                        val intent = Intent(context, com.example.IconPickerActivity::class.java).apply {
                                            putExtra("item_id", itemId)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                        popupWindow?.dismiss()
                                    }
                                    "Reset Icon" -> {
                                        val file = java.io.File(context.filesDir, "custom_icons/${itemId.replace(Regex("[^a-zA-Z0-9.-]"), "_")}.webp")
                                        if (file.exists()) file.delete()
                                        appsManager.iconCache.remove("custom_${itemId}")
                                        appsManager.iconCache.remove(itemId)
                                        context.sendBroadcast(Intent("com.example.UPDATE_SIDEBAR_ICONS").apply {
                                            putExtra("item_id", itemId)
                                        })
                                        popupWindow?.dismiss()
                                    }
                                    "App Info" -> {
                                        if (parsed is SidebarItem.App) {
                                            try {
                                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                intent.data = android.net.Uri.parse("package:${parsed.packageName}")
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                context.startActivity(intent)
                                                popupWindow?.dismiss()
                                            } catch (e: Exception) {}
                                        }
                                    }
                                }
                            }
                        }
                        popupLayout.addView(actionView)
                    }
                    
                    actionMenuPopup = PopupWindow(popupLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true).apply {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        } else {
                            @Suppress("DEPRECATION")
                            windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_PHONE
                        }
                        setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
                        elevation = 8f * density
                    }
                    
                    val loc = IntArray(2)
                    holder.itemView.getLocationOnScreen(loc)
                    actionMenuPopup?.showAtLocation(holder.itemView, Gravity.NO_GRAVITY, loc[0] + holder.itemView.width / 4, loc[1] + holder.itemView.height / 2)
                    
                    true
                }
            }
        }
        recyclerView.adapter = adapter

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_PHONE
            }
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
        }

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels

        var x = anchorX
        if (anchorX > screenWidth / 2) {
            x = anchorX - totalWidth
        } else {
            x = anchorX + anchor.width
        }

        var y = anchorY - (totalHeight / 2) + (anchor.height / 2)
        if (y < 0) y = 0
        if (y + totalHeight > screenHeight) y = screenHeight - totalHeight

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
    }

    private fun showWidgetPopup(anchor: View, widget: SidebarItem.PopupWidget) {
        val density = context.resources.displayMetrics.density
        val popupView = FrameLayout(context)
        
        val padding = (8 * density).toInt()
        popupView.setPadding(padding, padding, padding, padding)

        val popupOpacity = prefs.getFloat("sidebar_transparency", 0.9f)
        val popupBg = android.graphics.drawable.GradientDrawable()
        popupBg.setColor(Color.parseColor("#1A1A1A"))
        popupBg.alpha = (popupOpacity * 255).toInt()
        popupBg.cornerRadius = 16 * density
        popupView.background = popupBg
        
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val host = com.example.service.AppWidgetHelper.getHost(context)
        val info = appWidgetManager.getAppWidgetInfo(widget.widgetId)
        
        var popupWindow: PopupWindow? = null
        if (info != null) {
            val hostView = host.createView(context, widget.widgetId, info)
            hostView.setPadding(0, 0, 0, 0)
            
            val minW = info.minWidth
            val minH = info.minHeight
            val w = if (minW > 0) minW else (200 * density).toInt()
            val h = if (minH > 0) minH else (200 * density).toInt()
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                hostView.updateAppWidgetSize(null, (w / density).toInt(), (h / density).toInt(), (w / density).toInt(), (h / density).toInt())
            }
            
            val params = FrameLayout.LayoutParams(w, h)
            popupView.addView(hostView, params)
        }

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val totalWidth = popupView.measuredWidth
        val totalHeight = popupView.measuredHeight

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                windowLayoutType = android.view.WindowManager.LayoutParams.TYPE_PHONE
            }
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
        }
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        var x = anchorX
        if (anchorX > screenWidth / 2) {
            x = anchorX - totalWidth
        } else {
            x = anchorX + anchor.width
        }
        var y = anchorY - (totalHeight / 2) + (anchor.height / 2)
        if (y < 0) y = 0
        if (y + totalHeight > screenHeight) y = screenHeight - totalHeight
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
    }
}
