package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.example.R
import com.example.HybridGridEditActivity
import kotlin.math.max

class HybridGridWindowManager(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
    
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isFolded = false
    
    private val PAGE_ID = "floating_hybrid_grid"
    private var gridPageView: HybridGridPageView? = null
    private var windowContainer: LinearLayout? = null
    
    @SuppressLint("ClickableViewAccessibility")
    fun show(isEditModeExiting: Boolean = false) {
        if (floatingView != null) return
        
        val defaultW = (context.resources.displayMetrics.widthPixels * 0.85).toInt()
        val defaultH = WindowManager.LayoutParams.WRAP_CONTENT
        
        val width = prefs.getInt("hybrid_grid_width", defaultW)
        val height = WindowManager.LayoutParams.WRAP_CONTENT
        val x = prefs.getInt("hybrid_grid_x", 100)
        val y = prefs.getInt("hybrid_grid_y", 100)
        
        layoutParams = WindowManager.LayoutParams(
            width,
            height,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }
        
        val inflater = LayoutInflater.from(context)
        floatingView = inflater.inflate(R.layout.layout_hybrid_grid_floating, null)
        
        val bubbleIcon = floatingView!!.findViewById<ImageView>(R.id.bubble_icon)
        windowContainer = floatingView!!.findViewById<LinearLayout>(R.id.window_container)
        val topDragBar = floatingView!!.findViewById<LinearLayout>(R.id.top_drag_bar)
        val btnEditGrid = floatingView!!.findViewById<ImageView>(R.id.btn_edit_grid)
        val resizeHandle = floatingView!!.findViewById<ImageView>(R.id.resize_handle)
        val btnMinimizeBottom = floatingView!!.findViewById<ImageView>(R.id.btn_minimize_bottom)
        val btnExitBottom = floatingView!!.findViewById<ImageView>(R.id.btn_exit_bottom)
        val hybridGridContainer = floatingView!!.findViewById<FrameLayout>(R.id.hybrid_grid_container)
        
        gridPageView = HybridGridPageView(context, PAGE_ID) { newHeight ->
            if (layoutParams?.height == WindowManager.LayoutParams.WRAP_CONTENT) {
                // If it's wrap content, we don't strictly need to force height, but we can call updateViewLayout
                windowManager.updateViewLayout(floatingView, layoutParams)
            }
        }
        // Insert at index 0 so bottom controls stay on top
        hybridGridContainer.addView(gridPageView, 0, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 
            FrameLayout.LayoutParams.WRAP_CONTENT
        ))
        
        isFolded = prefs.getBoolean("hybrid_grid_folded", false)
        if (isFolded) {
            bubbleIcon.visibility = View.VISIBLE
            windowContainer?.visibility = View.GONE
        } else {
            bubbleIcon.visibility = View.GONE
            windowContainer?.visibility = View.VISIBLE
        }
        
        if (isEditModeExiting) {
            // Force unfold if exiting edit mode
            isFolded = false
            bubbleIcon.visibility = View.GONE
            windowContainer?.visibility = View.VISIBLE
            prefs.edit().putBoolean("hybrid_grid_folded", false).apply()
        }
        
        // --- Bubble Drag and Click ---
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        
        bubbleIcon.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams!!.x
                    initialY = layoutParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) isDragging = true
                    
                    layoutParams!!.x = initialX + dx.toInt()
                    layoutParams!!.y = initialY + dy.toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        isFolded = false
                        bubbleIcon.visibility = View.GONE
                        windowContainer?.visibility = View.VISIBLE
                        prefs.edit().putBoolean("hybrid_grid_folded", false).apply()
                    } else {
                        prefs.edit()
                            .putInt("hybrid_grid_x", layoutParams!!.x)
                            .putInt("hybrid_grid_y", layoutParams!!.y)
                            .apply()
                    }
                    true
                }
                else -> false
            }
        }
        
        // --- Top Bar Drag ---
        topDragBar.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams!!.x
                    initialY = layoutParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit()
                        .putInt("hybrid_grid_x", layoutParams!!.x)
                        .putInt("hybrid_grid_y", layoutParams!!.y)
                        .apply()
                    true
                }
                else -> false
            }
        }
        
        // --- Resize Handle ---
        var startResizeWidth = 0
        var startResizeHeight = 0
        var startResizeTouchX = 0f
        var startResizeTouchY = 0f
        
        resizeHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startResizeWidth = floatingView!!.width
                    startResizeHeight = floatingView!!.height
                    startResizeTouchX = event.rawX
                    startResizeTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - startResizeTouchX
                    val dy = event.rawY - startResizeTouchY
                    val newW = max(300, startResizeWidth + dx.toInt())
                    layoutParams!!.width = newW
                    layoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit()
                        .putInt("hybrid_grid_width", layoutParams!!.width)
                        .putInt("hybrid_grid_height", layoutParams!!.height)
                        .apply()
                    true
                }
                else -> false
            }
        }
        
        btnMinimizeBottom.setOnClickListener {
            isFolded = true
            bubbleIcon.visibility = View.VISIBLE
            windowContainer?.visibility = View.GONE
            prefs.edit().putBoolean("hybrid_grid_folded", true).apply()
        }
        
        btnExitBottom.setOnClickListener {
            hide()
        }
        
        btnEditGrid.setOnClickListener {
            // Hide the floating window
            hide()
            // Open Hybrid Grid Edit Activity
            val intent = Intent(context, HybridGridEditActivity::class.java).apply {
                putExtra("PAGE_ID", PAGE_ID)
                putExtra("IS_FLOATING", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
        
        windowManager.addView(floatingView, layoutParams)
    }
    
    fun hide() {
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        floatingView = null
        gridPageView = null
    }
    
    fun reloadGrid() {
        gridPageView?.loadWidgets()
    }
}
