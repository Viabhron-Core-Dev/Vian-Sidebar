package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.math.roundToInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner


class PageWindowManager(private val context: Context, private val pageType: String, private val onCloseCallback: (() -> Unit)? = null) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)

    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var isFullScreen = false
    private var preFullScreenWidth = 800
    private var preFullScreenHeight = 1000
    private var preFullScreenX = 100
    private var preFullScreenY = 100
    
    private var isFolded = false

    private fun toggleFullScreen(windowContainer: View, topDragBar: View) {
        if (!isFullScreen) {
            preFullScreenWidth = layoutParams?.width ?: 800
            preFullScreenHeight = layoutParams?.height ?: 1000
            preFullScreenX = layoutParams?.x ?: 100
            preFullScreenY = layoutParams?.y ?: 100
            
            val metrics = context.resources.displayMetrics
            layoutParams?.width = metrics.widthPixels
            layoutParams?.height = metrics.heightPixels
            layoutParams?.x = 0
            layoutParams?.y = 0
            isFullScreen = true
            windowContainer.background = null
        } else {
            layoutParams?.width = preFullScreenWidth
            layoutParams?.height = preFullScreenHeight
            layoutParams?.x = preFullScreenX
            layoutParams?.y = preFullScreenY
            isFullScreen = false
            windowContainer.setBackgroundResource(R.drawable.bg_floating_window)
        }
        windowManager.updateViewLayout(floatingView, layoutParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        val defaultW = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val defaultH = (context.resources.displayMetrics.heightPixels * 0.6).toInt()
        if (floatingView != null) return

        val width = prefs.getInt("page_${pageType}_width", defaultW)
        val height = prefs.getInt("page_${pageType}_height", defaultH)
        val x = prefs.getInt("page_${pageType}_x", 100)
        val y = prefs.getInt("page_${pageType}_y", 100)

        layoutParams = WindowManager.LayoutParams(
            width,
            height,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        floatingView = LayoutInflater.from(context).inflate(R.layout.layout_page, null)
        
        val bubbleIcon = floatingView!!.findViewById<ImageView>(R.id.bubble_icon)
        val windowContainer = floatingView!!.findViewById<LinearLayout>(R.id.window_container)
        val topDragBar = floatingView!!.findViewById<LinearLayout>(R.id.top_drag_bar)
        
        val tvTitle = floatingView!!.findViewById<TextView>(R.id.tv_title)
        val contentContainer = floatingView!!.findViewById<FrameLayout>(R.id.page_content_container)
        
        val btnClose = floatingView!!.findViewById<ImageView>(R.id.btn_exit_bottom)
        val btnMinimize = floatingView!!.findViewById<ImageView>(R.id.btn_minimize_bottom)
        val btnResize = floatingView!!.findViewById<ImageView>(R.id.resize_handle)

        val title = when (pageType) {
            "calculator" -> "Calculator"
            "compass" -> "Compass"
            "scheduler" -> "Short Reminders"
            "notifications" -> "Notifications"
            "app_tracker" -> "App Tracker"
            "resources_tracker" -> "Resources Tracker"
            "file_explorer" -> "File Explorer"
            "local_terminal" -> "Local Terminal"
            "termux" -> "Termux (PRoot)"
            else -> "Page Window"
        }
        tvTitle.text = title

        val estMb = when (pageType) {
            "calculator" -> 8
            "compass" -> 6
            "scheduler" -> 10
            "notifications" -> 12
            "app_tracker" -> 15
            "resources_tracker" -> 8
            "file_explorer" -> 10
            "local_terminal" -> 5
            "termux" -> 25
            else -> 10
        }
        com.example.utils.ActiveAppTracker.addApp("page_$pageType", title, "Floating Window", estMb)

        // Map the correct custom view
        val pageView = when (pageType) {
            "calculator" -> CalculatorPageView(context)
            "compass" -> CompassPageView(context)
            "scheduler" -> SchedulerPageView(context, CoroutineScope(Dispatchers.Main + Job()))
            "notifications" -> NotificationPageView(context, { close() }) { }
            "app_tracker" -> AppTrackerPageView(context, { close() }) { }
            "resources_tracker" -> ResourcesTrackerPageView(context, CoroutineScope(Dispatchers.Main + Job()))
            "file_explorer" -> FileExplorerPageView(context)
            "local_terminal" -> LocalTerminalPageView(context)
            "termux" -> TermuxPageView(context)
            else -> FrameLayout(context)
        }
        contentContainer.addView(pageView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        // --- Dragging Window ---
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var lastTouchTime = 0L

        topDragBar.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams!!.x
                    initialY = layoutParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    
                    val clickTime = System.currentTimeMillis()
                    if (clickTime - lastTouchTime < 300) {
                        toggleFullScreen(windowContainer, topDragBar)
                    }
                    lastTouchTime = clickTime
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isFullScreen) {
                        layoutParams!!.x = initialX + (event.rawX - initialTouchX).roundToInt()
                        layoutParams!!.y = initialY + (event.rawY - initialTouchY).roundToInt()
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isFullScreen) {
                        prefs.edit()
                            .putInt("page_${pageType}_x", layoutParams!!.x)
                            .putInt("page_${pageType}_y", layoutParams!!.y)
                            .apply()
                    }
                    true
                }
                else -> false
            }
        }

        // --- Resizing ---
        btnResize.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams!!.width
                    initialY = layoutParams!!.height
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isFullScreen) {
                        layoutParams!!.width = Math.max(300, initialX + (event.rawX - initialTouchX).roundToInt())
                        layoutParams!!.height = Math.max(300, initialY + (event.rawY - initialTouchY).roundToInt())
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isFullScreen) {
                        prefs.edit()
                            .putInt("page_${pageType}_width", layoutParams!!.width)
                            .putInt("page_${pageType}_height", layoutParams!!.height)
                            .apply()
                    }
                    true
                }
                else -> false
            }
        }
        
        // --- Dragging Bubble ---
        bubbleIcon.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams!!.x
                    initialY = layoutParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    
                    val clickTime = System.currentTimeMillis()
                    if (clickTime - lastTouchTime < 300) {
                        unfold()
                    }
                    lastTouchTime = clickTime
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams!!.x = initialX + (event.rawX - initialTouchX).roundToInt()
                    layoutParams!!.y = initialY + (event.rawY - initialTouchY).roundToInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = Math.abs(event.rawX - initialTouchX)
                    val dy = Math.abs(event.rawY - initialTouchY)
                    if (dx < 10 && dy < 10) {
                        unfold()
                    } else {
                        prefs.edit()
                            .putInt("page_${pageType}_x", layoutParams!!.x)
                            .putInt("page_${pageType}_y", layoutParams!!.y)
                            .apply()
                    }
                    true
                }
                else -> false
            }
        }

        btnClose.setOnClickListener { close() }
        btnMinimize.setOnClickListener { fold() }

        setupLifecycle(floatingView!!)
        windowManager.addView(floatingView, layoutParams)

        if (isFolded) {
            fold()
        } else {
            unfold()
        }
    }

    fun fold() {
        isFolded = true
        if (floatingView != null) {
            val bubbleIcon = floatingView!!.findViewById<ImageView>(R.id.bubble_icon)
            val windowContainer = floatingView!!.findViewById<LinearLayout>(R.id.window_container)
            
            windowContainer.visibility = View.GONE
            bubbleIcon.visibility = View.VISIBLE
            
            layoutParams?.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
    }

    private fun unfold() {
        val defaultW = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val defaultH = (context.resources.displayMetrics.heightPixels * 0.6).toInt()
        isFolded = false
        if (floatingView != null) {
            val bubbleIcon = floatingView!!.findViewById<ImageView>(R.id.bubble_icon)
            val windowContainer = floatingView!!.findViewById<LinearLayout>(R.id.window_container)
            
            bubbleIcon.visibility = View.GONE
            windowContainer.visibility = View.VISIBLE
            
            if (isFullScreen) {
                val metrics = context.resources.displayMetrics
                layoutParams?.width = metrics.widthPixels
                layoutParams?.height = metrics.heightPixels
                layoutParams?.x = 0
                layoutParams?.y = 0
            } else {
                layoutParams?.width = prefs.getInt("page_${pageType}_width", defaultW)
                layoutParams?.height = prefs.getInt("page_${pageType}_height", defaultH)
            }
            layoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
    }

    fun close() {
        if (floatingView != null) {
            com.example.utils.ActiveAppTracker.removeApp("page_$pageType")
            windowManager.removeView(floatingView)
            floatingView = null
            onCloseCallback?.invoke()
        }
        
    }


    private fun setupLifecycle(view: View) {
        val lifecycleOwner = CustomLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
        view.setViewTreeLifecycleOwner(lifecycleOwner)
        view.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        view.setViewTreeViewModelStoreOwner(lifecycleOwner)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
    }

    class CustomLifecycleOwner : androidx.savedstate.SavedStateRegistryOwner, androidx.lifecycle.ViewModelStoreOwner {
        private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
        private val savedStateRegistryController = androidx.savedstate.SavedStateRegistryController.create(this)
        private val store = androidx.lifecycle.ViewModelStore()

        override val lifecycle: androidx.lifecycle.Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: androidx.savedstate.SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
        override val viewModelStore: androidx.lifecycle.ViewModelStore get() = store

        fun handleLifecycleEvent(event: androidx.lifecycle.Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

        fun performRestore(savedState: android.os.Bundle?) {
            savedStateRegistryController.performRestore(savedState)
        }
    }
}
