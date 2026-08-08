package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.*
import com.example.R
import com.example.data.AppDatabase
import com.example.data.EpubBook
import com.example.util.AppLogger
import kotlinx.coroutines.*
import java.io.File
import android.text.*
import android.text.style.*
import android.graphics.Color
import java.util.Locale
import kotlin.math.max
import androidx.documentfile.provider.DocumentFile
import android.net.Uri
import com.example.utils.PageManager
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class SidebarService : Service() {
    
    private var tts: TextToSpeech? = null

    private var serviceLifecycleOwner: ServiceLifecycleOwner? = null
    private lateinit var windowManager: WindowManager
    
    // UI Refs


    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())


    private lateinit var prefs: SharedPreferences
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key != null && key.startsWith("handle_") && key != "handles_list") {
            triggerHandleViews.forEach { it.updatePosition() }
            readerHandleView?.updatePosition()
        }
        when (key) {
            "handles_list" -> {
                reloadHandles()
            }

            "keep_screen_on" -> {
            }
            "use_scoped_dir" -> {
            }
            "font_size_scale" -> {
            }
            "use_dark_theme" -> {
                applyThemeFromPrefs()
            }
            "trigger_position", "sidebar_position" -> {
                triggerHandleViews.forEach { it.updatePosition() }
                readerHandleView?.updatePosition()
            }

            "handle_reader_y", "handle_reader_width", "handle_reader_height", "handle_reader_color", "handle_reader_shape", "handle_reader_edge" -> {
                readerHandleView?.updatePosition()
            }
            "sidebar_pages", "sidebar_default_page_index",
            "sidebar_columns", "sidebar_width", "sidebar_height", 
            "sidebar_wrap_content", "sidebar_transparency", "sidebar_position_left" -> {
                val wasAttached = sidebarView?.windowToken != null
                if (wasAttached) {
                    showSidebar(currentPhysicalHandleId, currentHandleId)
                }
            }
            "reader_handle_enabled" -> {
                if (sharedPreferences.getBoolean("reader_handle_enabled", false)) {
                    readerHandleView?.attach()
                } else {
                    readerHandleView?.detach()
                }
            }
            "speed_indicator_enabled" -> {
                netSpeedEnabled = sharedPreferences.getBoolean("speed_indicator_enabled", false)
                if (netSpeedEnabled) {
                    if (netSpeedManager == null) {
                        netSpeedManager = NetSpeedManager(this@SidebarService, prefs, 
                            onSpeedUpdate = { down, up ->
                                downSpeed = down
                                upSpeed = up
                                updatePersistentNotification()
                            },
                            onDailyDataUpdate = { mobile, wifi ->
                                mobileMb = mobile
                                wifiMb = wifi
                                updatePersistentNotification()
                            }
                        )
                    }
                    netSpeedManager?.start()
                } else {
                    netSpeedManager?.stop()
                }
                updatePersistentNotification()
            }
            "call_recorder_enabled" -> {
                if (sharedPreferences.getBoolean("call_recorder_enabled", false)) {
                    callRecorderManager?.startListening()
                } else {
                    callRecorderManager?.stopListening()
                }
            }
        }
    }
    
    private fun applyThemeFromPrefs() {
        val isDark = prefs.getBoolean("use_dark_theme", true)
        
    }
    private val triggerHandleViews = mutableListOf<TriggerHandleView>()
    private var readerHandleView: ReaderHandleView? = null
    private var sidebarView: SidebarView? = null
    private var standaloneSidebarView: SidebarView? = null
    private var sidebarPagesList = mutableListOf<View>()
    private var sidebarDefaultIndex = 0
    private val appsManagers = mutableMapOf<String, SidebarAppsManager>()
    private var callRecorderManager: CallRecorderManager? = null
    private var pendingElementCallback: ((String) -> Unit)? = null
    
    private var netSpeedManager: NetSpeedManager? = null
    private var widgetPickerReceiver: android.content.BroadcastReceiver? = null
    private var wasSidebarEditOpen = false
    private var wasWidgetsGridEditOpen = false
    private var lastWidgetsGridPageId = ""
    private var currentHandleId: String = "sidebar"
    private var currentPhysicalHandleId: String = "sidebar"
    private var screenStateReceiver: android.content.BroadcastReceiver? = null
    private var netSpeedEnabled = false
    private var mobileMb: Long = 0
    private var wifiMb: Long = 0
    private var downSpeed: Long = 0
    private var upSpeed: Long = 0
    



    companion object {
        var instance: SidebarService? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null


    fun setTriggerVisibility(visible: Boolean) {
        triggerHandleViews.forEach { it.setVisibility(visible) }
        readerHandleView?.setVisibility(visible)
    }

    private fun reloadHandles() {
        if (!android.provider.Settings.canDrawOverlays(this)) return

        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()

        val handles = com.example.HandleManager.getHandles(prefs)
        for (handle in handles) {
            if (handle.enabled) {
                val view = TriggerHandleView(this@SidebarService, prefs, windowManager, handle.id)
                view.attach()
                triggerHandleViews.add(view)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        
        serviceLifecycleOwner = ServiceLifecycleOwner()
        serviceLifecycleOwner?.onCreate()
        serviceLifecycleOwner?.onStart()
        serviceLifecycleOwner?.onResume()
        
        prefs = getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_handle_edit_mode", false).apply()

        // Start Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "reader_channel",
                "Floating Reader",
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = android.content.Intent(this, com.example.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(this, 0, notificationIntent, android.app.PendingIntent.FLAG_IMMUTABLE)

        val notification = androidx.core.app.NotificationCompat.Builder(this, "reader_channel")
            .setContentTitle("LiteReader")
            .setContentText("Reading active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()
            
        
        

        if (Build.VERSION.SDK_INT >= 29) {
            val foregroundServiceTypes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var types = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    types = types or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(this@SidebarService, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        types = types or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    types = types or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                }

                }
                types
            } else {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            }
            startForeground(1, notification, foregroundServiceTypes)
        } else {
            startForeground(1, notification)
        }

        prefs.registerOnSharedPreferenceChangeListener(prefListener)
        loadSettingsFromPrefs()

        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        callRecorderManager = CallRecorderManager(this, prefs)
        callRecorderManager?.startListening()
        
        rebuildSidebarPages("sidebar")

        reloadHandles()

        readerHandleView = ReaderHandleView(this, prefs, windowManager)
        if (prefs.getBoolean("reader_handle_enabled", false)) {
            readerHandleView?.attach()
        }
        
        setupNetSpeed()
        updatePersistentNotification()
    }
    
    private var dictWindowManager: DictionaryWindowManager? = null
    private var translationWindowManager: TranslationWindowManager? = null
    private var hybridGridWindowManager: HybridGridWindowManager? = null
    private val pwaWindows = mutableMapOf<Int, PwaWindowManager>()

    fun launchPwa(pwa: PwaEntry) {
        if (!pwaWindows.containsKey(pwa.id)) {
            val windowManager = PwaWindowManager(this, pwa)
            pwaWindows[pwa.id] = windowManager
            windowManager.show()
        }
    }

    fun removePwaWindow(id: Int) {
        pwaWindows.remove(id.toInt())
    }

    fun toggleDictionaryWindow() {
        if (dictWindowManager == null) {
            dictWindowManager = DictionaryWindowManager(this)
        }
        dictWindowManager?.show(false)
        pwaWindows.values.forEach { }

    }
    
    private fun setupNetSpeed() {
        val dailyMobileRx = prefs.getLong("daily_mobile_rx", 0)
        val dailyMobileTx = prefs.getLong("daily_mobile_tx", 0)
        val dailyWifiRx = prefs.getLong("daily_wifi_rx", 0)
        val dailyWifiTx = prefs.getLong("daily_wifi_tx", 0)
        mobileMb = (dailyMobileRx + dailyMobileTx) / (1024 * 1024)
        wifiMb = (dailyWifiRx + dailyWifiTx) / (1024 * 1024)

        netSpeedEnabled = prefs.getBoolean("speed_indicator_enabled", false)
        if (netSpeedEnabled) {
            netSpeedManager = NetSpeedManager(this, prefs, 
                onSpeedUpdate = { down, up ->
                    downSpeed = down
                    upSpeed = up
                    updatePersistentNotification()
                },
                onDailyDataUpdate = { mobile, wifi ->
                    mobileMb = mobile
                    wifiMb = wifi
                    updatePersistentNotification()
                }
            )
            netSpeedManager?.start()
        }
        
        widgetPickerReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "WIDGET_PICKER_OPENED") {
                    if (false) {
                        wasSidebarEditOpen = true
                        
                    }
                    if (false) {
                        wasWidgetsGridEditOpen = true
                        
                    }
                    closeSidebar()
                } else if (intent.action == "WIDGET_PICKER_CLOSED") {
                    val actionType = intent.getStringExtra("ACTION_TYPE")
                    if (actionType == "ADD_TO_WIDGETS_GRID" || wasWidgetsGridEditOpen) {
                        val pageId = intent.getStringExtra("PAGE_ID") ?: lastWidgetsGridPageId
                        if (pageId.isNotEmpty()) {
                            showSidebar(currentPhysicalHandleId, currentHandleId)
                            showWidgetsGridEditOverlay(pageId)
                        }
                    } else if (actionType == "ADD_ELEMENT" || wasSidebarEditOpen) {
                        showSidebar(currentPhysicalHandleId, currentHandleId)
                        
                    }
                    wasSidebarEditOpen = false
                    wasWidgetsGridEditOpen = false
                }
            }
        }
        val widgetFilter = android.content.IntentFilter().apply {
            addAction("WIDGET_PICKER_OPENED")
            addAction("WIDGET_PICKER_CLOSED")
        }
        registerReceiver(widgetPickerReceiver, widgetFilter, Context.RECEIVER_NOT_EXPORTED)

        screenStateReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        if (netSpeedEnabled) netSpeedManager?.start()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        if (netSpeedEnabled) netSpeedManager?.stop()
                    }
                }
            }
        }
        val filter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        
        // Setup AlarmManager for midnight reset
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, MidnightResetReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this, 0, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            android.app.AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    
    private fun updatePersistentNotification() {
        val manager = getSystemService(android.app.NotificationManager::class.java)
        
        val notificationIntent = Intent(this, com.example.MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(this, 0, notificationIntent, android.app.PendingIntent.FLAG_IMMUTABLE)

        val settingsIntent = Intent(this, com.example.SettingsActivity::class.java).apply {
            putExtra("start_route", "netspeed")
        }
        val settingsPendingIntent = android.app.PendingIntent.getActivity(this, 1, settingsIntent, android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT)

        val speedUnits = prefs.getString("speed_units", "Auto") ?: "Auto"
        val formatSpeed = { bytesPerSec: Long ->
            val kbps = bytesPerSec / 1024.0
            val mbps = kbps / 1024.0
            when (speedUnits) {
                "KB/s" -> String.format("%.0f KB/s", kotlin.math.max(0.0, kbps))
                "MB/s" -> String.format("%.2f MB/s", kotlin.math.max(0.0, mbps))
                else -> if (kbps >= 1024) String.format("%.1f MB/s", mbps) else String.format("%.0f KB/s", kotlin.math.max(0.0, kbps))
            }
        }

        val dataUnits = prefs.getString("data_units", "Auto") ?: "Auto"
        val totalMb = mobileMb + wifiMb
        val dataText = when(dataUnits) {
            "MB" -> "Data: $totalMb MB"
            "GB" -> String.format("Data: %.2f GB", totalMb / 1000.0)
            "GiB" -> String.format("Data: %.2f GiB", totalMb / 1024.0)
            else -> if (totalMb >= 1024) String.format("Data: %.2f GiB", totalMb / 1024.0) else "Data: $totalMb MB"
        }

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(this, "reader_channel")
            .setContentTitle(dataText)
            .setContentText("Down: ${formatSpeed(downSpeed)}   Up: ${formatSpeed(upSpeed)}")
            .setContentIntent(settingsPendingIntent)
            .setOnlyAlertOnce(true)

        if (netSpeedEnabled) {
            val speedIcon = createSpeedIcon(kotlin.math.max(downSpeed, upSpeed))
            notificationBuilder.setSmallIcon(speedIcon)
        } else {
            notificationBuilder.setSmallIcon(android.R.drawable.ic_media_play)
        }
            
        manager.notify(1, notificationBuilder.build())
    }

    private fun createSpeedIcon(speedBytes: Long): androidx.core.graphics.drawable.IconCompat {
        // High resolution for sharpness (system will scale it down smoothly for status bar)
        val size = 200
        
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val kbps = speedBytes / 1024.0
        val mbps = kbps / 1024.0
        val valueStr: String
        val unitStr: String
        
        val speedUnits = prefs.getString("speed_units", "Auto") ?: "Auto"
        when (speedUnits) {
            "KB/s" -> {
                valueStr = String.format("%.0f", kotlin.math.max(0.0, kbps))
                unitStr = "KB/s"
            }
            "MB/s" -> {
                valueStr = String.format("%.2f", kotlin.math.max(0.0, mbps))
                unitStr = "MB/s"
            }
            else -> {
                if (kbps >= 1024) {
                    valueStr = String.format("%.1f", mbps)
                    unitStr = "MB/s"
                } else {
                    valueStr = String.format("%.0f", kotlin.math.max(0.0, kbps))
                    unitStr = "KB/s"
                }
            }
        }
        
        // Use sans-serif-medium like system time
        textPaint.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        var valueTextSize = size * 0.55f
        textPaint.textSize = valueTextSize
        while (textPaint.measureText(valueStr) > size - 8f && valueTextSize > size * 0.2f) {
            valueTextSize -= 2f
            textPaint.textSize = valueTextSize
        }
        // Top text
        val valueY = -textPaint.ascent() + size * 0.05f
        canvas.drawText(valueStr, size / 2f, valueY, textPaint)
        
        var unitTextSize = size * 0.35f
        textPaint.textSize = unitTextSize
        while (textPaint.measureText(unitStr) > size - 8f && unitTextSize > size * 0.1f) {
            unitTextSize -= 2f
            textPaint.textSize = unitTextSize
        }
        // Bottom text
        val unitY = size * 0.95f - textPaint.descent()
        canvas.drawText(unitStr, size / 2f, unitY, textPaint)
        
        return androidx.core.graphics.drawable.IconCompat.createWithBitmap(bitmap)
    }
    
    private val appsPageViews = mutableListOf<AppsPageView>()

    private fun rebuildSidebarPages(handleId: String) {
        val pageConfigs = PageManager.getPages(prefs, handleId)
        sidebarDefaultIndex = PageManager.getDefaultPageIndex(prefs, handleId)
        sidebarPagesList.clear()
        appsPageViews.clear()
        
        pageConfigs.forEach { config ->
            val pageView = when (config.type) {
                "apps" -> {
                    var p: AppsPageView? = null
                    val prefKey = "sidebar_apps_" + handleId + "_" + config.id
                    val manager = appsManagers.getOrPut(prefKey) {
                        SidebarAppsManager(this, prefs, serviceScope, prefKey) {
                            appsPageViews.find { it.pageConfig?.id == config.id }?.updateData(appsManagers[prefKey]?.activeItems ?: emptyList())
                        }
                    }
                    manager.ensureLoaded()
                    p = AppsPageView(this, handleId, config, manager, serviceScope,
                        onCloseSidebar = { closeSidebar() },
                        onHeightChanged = { newHeight ->
                            if (sidebarView != null && p != null && sidebarPagesList.indexOf(p!!) == sidebarView!!.getCurrentPageIndex()) {
                                sidebarView?.updatePageStyles(config, newHeight)
                            }
                        }
                    )
                    p.updateData(manager.activeItems)
                    appsPageViews.add(p)
                    p
                }
                "scheduler" -> SchedulerPageView(this, serviceScope)
                "calculator" -> CalculatorPageView(this)
                "compass" -> CompassPageView(this)
                "notifications" -> {
                    var p: NotificationPageView? = null
                    p = NotificationPageView(this, { closeSidebar() }, { _ -> })
                    p
                }
                "widgets_grid" -> {
                    var p: WidgetsGridPageView? = null
                    p = WidgetsGridPageView(this, config.id) { newHeight ->
                        if (sidebarView != null && p != null && sidebarPagesList.indexOf(p!!) == sidebarView!!.getCurrentPageIndex()) {
                            sidebarView?.updatePageStyles(config, newHeight)
                        }
                    }
                    p
                }
                "hybrid_grid" -> {
                    var p: HybridGridPageView? = null
                    p = HybridGridPageView(this, config.id) { newHeight ->
                        if (sidebarView != null && p != null && sidebarPagesList.indexOf(p!!) == sidebarView!!.getCurrentPageIndex()) {
                            sidebarView?.updatePageStyles(config, newHeight)
                        }
                    }
                    p
                }
                "pwa_loader" -> { null }
                "dictionary" -> { null }
                "resources_tracker" -> {
                    var p: ResourcesTrackerPageView? = null
                    p = ResourcesTrackerPageView(this, serviceScope)
                    p
                }
                "app_tracker" -> {
                    var p: AppTrackerPageView? = null
                    p = AppTrackerPageView(this, { closeSidebar() }, { _ -> })
                    p
                }
                "media_player" -> {
                    var p: MediaPlayerPageView? = null
                    p = MediaPlayerPageView(this, { closeSidebar() }) { newHeight ->
                        if (sidebarView != null && p != null && sidebarPagesList.indexOf(p!!) == sidebarView!!.getCurrentPageIndex()) {
                            sidebarView?.updatePageStyles(config, newHeight)
                        }
                    }
                    p
                }
                "widget" -> {
                    var p: WidgetPageView? = null
                    p = WidgetPageView(this, config.id) { newHeight ->
                        if (sidebarView != null && p != null && sidebarPagesList.indexOf(p!!) == sidebarView!!.getCurrentPageIndex()) {
                            sidebarView?.updatePageStyles(config, newHeight)
                        }
                    }
                    p
                }
                else -> {
                    TextView(this).apply {
                        text = "${config.title} coming soon..."
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER
                        textSize = 16f
                    }
                }
            }
            if (pageView != null) { sidebarPagesList.add(pageView) }
        }
        
        // Ensure index is valid
        if (sidebarDefaultIndex >= sidebarPagesList.size) {
            sidebarDefaultIndex = 0
        }
    }
    

    fun closeSidebar() {
        sidebarView?.detach()
        sidebarView = null
        standaloneSidebarView?.detach()
        standaloneSidebarView = null
    }

    private fun showSidebar(handleId: String, containerId: String = handleId) {
        currentHandleId = containerId
        currentPhysicalHandleId = handleId
        if (sidebarView == null) {
            rebuildSidebarPages(containerId)
            sidebarView = SidebarView(this, prefs, windowManager, handleId, containerId, sidebarPagesList, PageManager.getPages(prefs, containerId), sidebarDefaultIndex, onClose = { closeSidebar() },
                onEditPageClicked = { page, config ->
                    if (page is AppsPageView) {
                        showSidebarEditOverlay(config.id)
                    } else if (page is WidgetsGridPageView) {
                        showWidgetsGridEditOverlay(config.id)
                    } else if (page is HybridGridPageView) {
                        showHybridGridEditOverlay(config.id)
                    } else if (page is AppTrackerPageView) {
                        val intent = android.content.Intent(this@SidebarService, com.example.AppTrackerSettingsActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                },
            )
            serviceLifecycleOwner?.let {
                sidebarView?.setViewTreeLifecycleOwner(it)
                sidebarView?.setViewTreeViewModelStoreOwner(it)
                sidebarView?.setViewTreeSavedStateRegistryOwner(it)
            }
            val defaultPage = sidebarPagesList.getOrNull(sidebarDefaultIndex)
            val defaultPageConfig = PageManager.getPages(prefs, handleId).getOrNull(sidebarDefaultIndex)
            if (defaultPage is AppsPageView) {
                sidebarView?.updatePageStyles(defaultPageConfig, (defaultPage).height)
            } else if (defaultPage is NotificationPageView) {
                sidebarView?.updatePageStyles(defaultPageConfig, (defaultPage).height)
            } else if (defaultPage is WidgetsGridPageView) {
                sidebarView?.updatePageStyles(defaultPageConfig, (defaultPage).height)
            } else if (defaultPage is HybridGridPageView) {
                sidebarView?.updatePageStyles(defaultPageConfig, (defaultPage).height)
            } else if (defaultPage != null) {
                val density = resources.displayMetrics.density
                sidebarView?.updatePageStyles(defaultPageConfig, (450 * density).toInt())
            }
        }
        sidebarView?.goToPage(sidebarDefaultIndex)
        sidebarView?.attach()
    }

    
    fun openSidebarPage(handleId: String, type: String) {
        val pageConfigs = PageManager.getPages(prefs, handleId)
        val index = pageConfigs.indexOfFirst { it.type == type }
        if (index != -1) {
            showSidebar(handleId)
            sidebarView?.goToPage(index)
        } else {
            showStandalonePage(handleId, type)
        }
    }

    fun openGestureSidebar(handleId: String, gesture: String) {
        val containerId = "${handleId}_$gesture"
        if (sidebarView != null) {
            closeSidebar()
        } else {
            showSidebar(handleId, containerId)
        }
    }

    fun openGestureSidebarPage(handleId: String, gesture: String, type: String) {
        val containerId = "${handleId}_$gesture"
        val pageConfigs = PageManager.getPages(prefs, containerId)
        val index = pageConfigs.indexOfFirst { it.type == type }
        if (index != -1) {
            showSidebar(handleId, containerId)
            sidebarView?.goToPage(index)
        } else {
            showStandalonePage(handleId, type, containerId)
        }
    }

    private fun showStandalonePage(handleId: String, type: String, containerId: String = handleId) {
        currentHandleId = containerId
        currentPhysicalHandleId = handleId
        if (standaloneSidebarView != null) {
            windowManager.removeView(standaloneSidebarView)
            standaloneSidebarView = null
        }
        
        val config = com.example.utils.SidebarPage(id = "standalone_$type", title = type.replaceFirstChar { it.uppercase() }, type = type)
        val tempPagesList = mutableListOf<View>()
        val pageView = when (config.type) {
            "apps" -> {
                val prefKey = "sidebar_apps_" + containerId + "_" + config.id
                val manager = appsManagers.getOrPut(prefKey) {
                    SidebarAppsManager(this, prefs, serviceScope, prefKey) {}
                }
                manager.ensureLoaded()
                val p = AppsPageView(this, containerId, config, manager, serviceScope,
                    onCloseSidebar = { standaloneSidebarView?.close() },
                    onHeightChanged = { newHeight -> standaloneSidebarView?.updatePageStyles(config, newHeight) }
                )
                p.updateData(manager.activeItems)
                p
            }
            "scheduler" -> SchedulerPageView(this, serviceScope)
            "calculator" -> CalculatorPageView(this)
            "compass" -> CompassPageView(this)
            "notifications" -> NotificationPageView(this, { standaloneSidebarView?.close() }, { _ -> })
            "widgets_grid" -> WidgetsGridPageView(this, config.id) { newHeight ->
                standaloneSidebarView?.updatePageStyles(config, newHeight)
            }
            "hybrid_grid" -> HybridGridPageView(this, config.id) { newHeight ->
                standaloneSidebarView?.updatePageStyles(config, newHeight)
            }
            "pwa_loader" -> null
            "dictionary" -> null // Removed from sidebar
            "app_tracker" -> AppTrackerPageView(this, { standaloneSidebarView?.close() }, { _ -> })
            "resources_tracker" -> ResourcesTrackerPageView(this, serviceScope)
            "media_player" -> MediaPlayerPageView(this, { standaloneSidebarView?.close() }) { newHeight ->
                standaloneSidebarView?.updatePageStyles(config, newHeight)
            }
            else -> null
        }
        
        if (pageView != null) {
            tempPagesList.add(pageView)
            standaloneSidebarView = SidebarView(this, prefs, windowManager, handleId, containerId, tempPagesList, listOf(config), 0, onClose = { 
                standaloneSidebarView?.detach()
                standaloneSidebarView = null 
            }, onEditPageClicked = { page, _ ->
                if (page is AppTrackerPageView) {
                    val intent = android.content.Intent(this@SidebarService, com.example.AppTrackerSettingsActivity::class.java)
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            })
            
            serviceLifecycleOwner?.let {
                standaloneSidebarView?.setViewTreeLifecycleOwner(it)
                standaloneSidebarView?.setViewTreeViewModelStoreOwner(it)
                standaloneSidebarView?.setViewTreeSavedStateRegistryOwner(it)
            }
            
            standaloneSidebarView?.attach()
            if (pageView is AppsPageView) {
                standaloneSidebarView?.updatePageStyles(config, pageView.height)
            } else if (pageView is WidgetsGridPageView) {
                standaloneSidebarView?.updatePageStyles(config, pageView.height)
            }
        }
    }



    fun showWidgetsGridEditOverlay(pageId: String) {
        lastWidgetsGridPageId = pageId
        val intent = android.content.Intent(this, com.example.WidgetsGridEditActivity::class.java).apply {
            putExtra("PAGE_ID", pageId)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun showHybridGridEditOverlay(pageId: String) {
        val intent = android.content.Intent(this, com.example.HybridGridEditActivity::class.java).apply {
            putExtra("PAGE_ID", pageId)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun showSidebarEditOverlay(pageId: String = "default_apps") {
        val intent = android.content.Intent(this, com.example.SidebarEditActivity::class.java).apply {
            putExtra("PAGE_ID", pageId)
            putExtra("HANDLE_ID", currentHandleId)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }






    fun executeElementAction(id: String) {
        if (id.startsWith("app:")) {
            val pkg = id.removePrefix("app:")
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try { startActivity(launchIntent) } catch (e: Exception) {}
            }
        } else if (id.startsWith("pwa:")) {
            val pwaIdStr = id.removePrefix("pwa:")
            val pwaId = pwaIdStr.toIntOrNull()
            if (pwaId != null) {
                serviceScope.launch(Dispatchers.IO) {
                    val db = PwaDatabase.getDatabase(applicationContext)
                    val pwas = db.pwaDao().getAllPwasSync()
                    val pwa = pwas.find { it.id == pwaId }
                    if (pwa != null) {
                        withContext(Dispatchers.Main) {
                            launchPwa(pwa)
                        }
                    }
                }
            }
        } else if (id.startsWith("floating_trigger:")) {
            val targetId = id.removePrefix("floating_trigger:")
            val intent = Intent(this, com.example.service.FloatingTriggerService::class.java).apply {
                action = "TOGGLE"
                putExtra("TARGET_ID", targetId)
            }
            startService(intent)
        } else if (id.startsWith("page_window:")) {
            val pageType = id.removePrefix("page_window:")
            val intent = Intent(this, PageWindowService::class.java).apply {
                action = "TOGGLE"
                putExtra("PAGE_TYPE", pageType)
            }
            startService(intent)
        } else if (id.startsWith("quicktile:")) {
            val action = id.removePrefix("quicktile:")
            QuickTileHandler.handleQuickTileAction(this, action)
        } else if (id.startsWith("system:")) {
            val action = id.removePrefix("system:")
            if (action == "log_keeper") {
                val intent = Intent(this, com.example.LogKeeperActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (action == "dictionary_floating") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(false)
            } else if (action == "hybrid_grid_floating" || action == "hybrid_grid_floating_exit_edit") {
                if (hybridGridWindowManager == null) {
                    hybridGridWindowManager = HybridGridWindowManager(this@SidebarService)
                }
                hybridGridWindowManager?.show(action == "hybrid_grid_floating_exit_edit")
                hybridGridWindowManager?.reloadGrid()

            } else if (action == "translation_floating") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(false, true)

            } else if (action == "ebook_reader") {
                val intent = Intent(this, FloatingReaderService::class.java)
                intent.putExtra("UNFOLD", true)
                startService(intent)
            } else if (action == "work_notes") {
                val intent = Intent(this, WorkNotesService::class.java)
                intent.action = "TOGGLE"
                startService(intent)
            } else if (action == "barcode_scanner") {
                val intent = Intent(this, com.example.service.BarcodeScannerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (action == "screen_record") {
                val intent = Intent(this, com.example.service.ScreenRecordActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else if (action == "settings") {
                val intent = Intent(this, com.example.SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                val service = VianSideAccessibilityService.instance
                if (service != null && service.performAction(action)) {
                    // success
                } else {
                    android.widget.Toast.makeText(this, "Please enable VianSide Accessibility Service", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else if (id.startsWith("volume:")) {
            val actionId = id.removePrefix("volume:")
            try {
                val streamStr = actionId.split("_")[0]
                val actionStr = actionId.split("_")[1]
                MediaVolumeHandler.handleVolumeAction(this, streamStr, actionStr)
            } catch (e: Exception) {}
        } else if (id.startsWith("media:")) {
            val actionId = id.removePrefix("media:")
            MediaVolumeHandler.handleMediaAction(this, actionId)
        } else if (id.startsWith("display:")) {
            val actionId = id.removePrefix("display:")
            DisplayHandler.handleDisplayAction(this, actionId)
        } else if (id.startsWith("intent:")) {
            val componentStr = id.removePrefix("intent:")
            val parts = componentStr.split("/")
            if (parts.size == 2) {
                val launchIntent = Intent()
                launchIntent.setComponent(android.content.ComponentName(parts[0], parts[1]))
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try { startActivity(launchIntent) } catch (e: Exception) {}
            }
        } else if (id.startsWith("shortcut:")) {
            try {
                val parts = id.split(":", limit = 3)
                val jsonStr = parts[2]
                val obj = org.json.JSONObject(jsonStr)
                val url = obj.getString("url")
                val launchIntent = if (url.startsWith("intent:")) {
                    Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                } else {
                    Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                }
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            } catch (e: Exception) {}
        }
        sidebarView?.close()
        hybridGridWindowManager?.show(false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "READ_ALOUD") {
            val text = intent.getStringExtra("TEXT") ?: ""
            if (tts == null) {
                tts = TextToSpeech(this) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale.US
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "read_aloud")
                    }
                }
            } else {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "read_aloud")
            }
            return START_NOT_STICKY
        }
        if (intent?.action == "OPEN_DICTIONARY") {
            val query = intent.getStringExtra("QUERY")
            if (dictWindowManager == null) {
                dictWindowManager = DictionaryWindowManager(this)
            }
            if (query != null) {
                dictWindowManager?.searchWord(query)
            } else {
                dictWindowManager?.show(false)
            }
            return START_NOT_STICKY
        }
        if (intent?.action == "EXECUTE_ACTION") {
            val actionId = intent.getStringExtra("ACTION_ID")
            if (actionId != null) {
                executeElementAction(actionId)
            }
            return START_NOT_STICKY
        }
        if (intent?.action == "UPDATE_CONFIG") {
            appsManagers.values.forEach { it.reloadActiveApps() }
            return START_NOT_STICKY
        }
        if (intent?.action == "ADD_ELEMENT") {
            val elementId = intent.getStringExtra("element_id") ?: return START_NOT_STICKY
            val folderUuid = intent.getStringExtra("FOLDER_UUID")
            val isElementCallback = intent.getBooleanExtra("IS_ELEMENT_CALLBACK", false)
            if (isElementCallback) {
                pendingElementCallback?.invoke(elementId)
                pendingElementCallback = null
            } else {
                val handlePages = PageManager.getPages(prefs, currentHandleId)
                val targetPage = handlePages.firstOrNull()
                
                if (targetPage?.type == "hybrid_grid") {
                    val pageId = targetPage.id
                    val itemsJson = prefs.getString("hybrid_grid_$pageId", "[]") ?: "[]"
                    val arr = org.json.JSONArray(itemsJson)
                    val obj = org.json.JSONObject()
                    obj.put("id", elementId)
                    if (elementId.startsWith("widget:")) {
                        obj.put("cols", 2)
                        obj.put("rows", 2)
                    } else {
                        obj.put("cols", 1)
                        obj.put("rows", 1)
                    }
                    obj.put("x", 0)
                    obj.put("y", 0)
                    arr.put(obj)
                    prefs.edit().putString("hybrid_grid_$pageId", arr.toString())
                         .putBoolean("hybrid_grid_modified_$pageId", true)
                         .apply()
                    
                    val bIntent = android.content.Intent("ELEMENT_ADDED_TO_HYBRID")
                    bIntent.putExtra("PAGE_ID", pageId)
                    bIntent.setPackage(packageName)
                    sendBroadcast(bIntent)
                } else if (targetPage != null) {
                    val prefKey = "sidebar_apps_" + currentHandleId + "_" + targetPage.id
                    val manager = appsManagers.getOrPut(prefKey) {
                        SidebarAppsManager(this, prefs, serviceScope, prefKey) {}
                    }
                    if (folderUuid != null) {
                        manager.addItemToFolder(folderUuid, elementId)
                    } else {
                        manager.addItem(elementId)
                    }
                }
            }
            return START_NOT_STICKY
        }
        
        
        val bookId = intent?.getIntExtra("BOOK_ID", -1) ?: -1
        val fromLauncher = intent?.getBooleanExtra("OPEN_FROM_LAUNCHER", false) ?: false
        return START_NOT_STICKY
    }

    private lateinit var bottomWindowControls: View

    private var toastJob: kotlinx.coroutines.Job? = null

    private fun showToast(message: String) {
        serviceScope.launch(Dispatchers.Main) {
            Toast.makeText(this@SidebarService, message, Toast.LENGTH_SHORT).show()
        }
    }

    private var currentLibraryTab = "Recent"
    private var currentExplorerDir: java.io.File? = null
    private var rootExplorerDir: java.io.File? = null
    private var explorerSortByName: Boolean = true
    private var explorerSortAscending: Boolean = true

    private var isFullScreen = false
    private var preFullScreenX = 0
    private var preFullScreenY = 0
    private var preFullScreenWidth = 0
    private var preFullScreenHeight = 0


    private fun loadSettingsFromPrefs() {
        currentLibraryTab = prefs.getString("last_library_tab", "Recent") ?: "Recent"
        val lastDirPath = prefs.getString("last_explorer_dir", null)
        explorerSortByName = prefs.getBoolean("explorer_sort_name", true)
        explorerSortAscending = prefs.getBoolean("explorer_sort_asc", true)
        if (lastDirPath != null) {
            val dir = java.io.File(lastDirPath)
            if (dir.exists() && dir.isDirectory) {
                rootExplorerDir = android.os.Environment.getExternalStorageDirectory()
                explorerStack.clear()
                if (rootExplorerDir != null) {
                    explorerStack.add(rootExplorerDir!!)
                    var current: java.io.File? = dir
                    val temp = mutableListOf<java.io.File>()
                    while (current != null && current.absolutePath != rootExplorerDir?.absolutePath) {
                        temp.add(0, current)
                        current = current.parentFile
                    }
                    if (current?.absolutePath == rootExplorerDir?.absolutePath) {
                        explorerStack.addAll(temp)
                    } else {
                        explorerStack.clear()
                        explorerStack.add(rootExplorerDir!!)
                        explorerStack.add(dir) // simple stack recovery
                    }
                }
            }
        }
    }

    private val explorerStack = mutableListOf<java.io.File>()

        private val saveScrollRunnable = Runnable {
        }

    private var lastKnownScrollY = 0


    private var autoSaveJob: Job? = null
    private var searchJob: Job? = null



    



    

    // --- Quick Notes Implementation ---
    
    

    

    
    

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        try {
            val importsDir = java.io.File(filesDir, "pwa_imports")
            if (importsDir.exists() && importsDir.isDirectory) {
                importsDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tts?.stop()
        tts?.shutdown()
        if (this::prefs.isInitialized) {
            prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        }
        widgetPickerReceiver?.let { unregisterReceiver(it) }
        screenStateReceiver?.let { unregisterReceiver(it) }
        netSpeedManager?.stop()
        callRecorderManager?.stopListening()
        instance = null
        closeSidebar()
        sidebarView = null
        sidebarPagesList.clear()
        appsPageViews.clear()
        appsManagers.values.forEach { it.destroy() }
        appsManagers.clear()
        triggerHandleViews.forEach { it.detach() }
        triggerHandleViews.clear()

        readerHandleView?.detach()
        readerHandleView = null
        serviceScope.cancel()
        serviceLifecycleOwner?.onPause()
        serviceLifecycleOwner?.onStop()
        serviceLifecycleOwner?.onDestroy()
        super.onDestroy()
    }
}
