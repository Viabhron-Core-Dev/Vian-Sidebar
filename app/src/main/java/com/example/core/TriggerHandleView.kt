package com.example.core

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.util.Utils
import com.example.util.HandleShapeDrawable
import com.example.util.getEdgeFlag
import kotlin.math.abs
import androidx.core.content.ContextCompat

class TriggerHandleView(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val windowManager: WindowManager,
    private val handleId: String
) {
    private val prefix = "handle_${handleId}_"
    private var handleView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isAttached = false

    fun attach() {
        if (isAttached) return
        
        handleView = View(context)
        
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        updatePosition()
        setupListeners()
        
        windowManager.addView(handleView, layoutParams)
        isAttached = true
    }
    
    fun setVisibility(visible: Boolean?) {
        handleView?.visibility = if (visible == true) View.VISIBLE else View.GONE
    }

    fun detach() {
        if (!isAttached) return
        if (handleView != null) {
            try {
                windowManager.removeView(handleView)
            } catch (e: Exception) {}
        }
        isAttached = false
    }

    fun updatePosition() {
        if (handleView == null || layoutParams == null) return
        
        val edgeStr = prefs.getString("${prefix}edge", "right") ?: "right"
        val gravity = getEdgeFlag(edgeStr)
        
        val yPos = prefs.getInt("${prefix}y", 50)
        
        layoutParams?.gravity = gravity or Gravity.TOP
        layoutParams?.y = (Utils.getScreenHeight(context) * (yPos / 100f)).toInt()
        
        val heightDp = try {
            prefs.getInt("${prefix}height", if (handleId == "reader") 60 else 120)
        } catch (e: Exception) {
            val str = prefs.getString("${prefix}height", "medium") ?: "medium"
            val map = mapOf("small" to 60, "medium" to 120, "large" to 200, "xlarge" to 300)
            map[str] ?: (if (handleId == "reader") 60 else 120)
        }
        
        val widthDp = try {
            prefs.getInt("${prefix}width", if (handleId == "reader") 16 else 12)
        } catch (e: Exception) {
            val str = prefs.getString("${prefix}width", "medium") ?: "medium"
            val map = mapOf("small" to 4, "medium" to 6, "large" to 10, "xlarge" to 16)
            map[str] ?: (if (handleId == "reader") 16 else 12)
        }
        
        val heightPx = Utils.dpToPx(context, heightDp)
        val widthPx = Utils.dpToPx(context, widthDp)
        
        layoutParams?.height = heightPx
        layoutParams?.width = widthPx
        
        val colorInt = try {
            val c = prefs.all["${prefix}color"]
            when (c) {
                is Int -> c
                is String -> android.graphics.Color.parseColor(c)
                else -> android.graphics.Color.parseColor(if (handleId == "reader") "#44102d42" else "#242962ff")
            }
        } catch(e: Exception) { android.graphics.Color.parseColor(if (handleId == "reader") "#44102d42" else "#242962ff") }
        
        val shapeStr = prefs.getString("${prefix}shape", if (handleId == "reader") "half_oval" else "slanted_block") ?: if (handleId == "reader") "half_oval" else "slanted_block"
        val edgeStrForShape = prefs.getString("${prefix}edge", "right") ?: "right"
        handleView?.background = HandleShapeDrawable(colorInt, shapeStr, edgeStrForShape)
        
        if (isAttached) {
            windowManager.updateViewLayout(handleView, layoutParams)
        }
    }

    private fun setupListeners() {
        var startRawX = 0f
        var startRawY = 0f
        var gestureHandled = false

        val gestureDetector = android.view.GestureDetector(context, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                gestureHandled = false
                return true
            }
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                gestureHandled = true
                handleAction("tap")
                return true
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                gestureHandled = true
                handleAction("double_tap")
                return true
            }
            override fun onLongPress(e: MotionEvent) {
                gestureHandled = true
                handleAction("long_press")
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 != null) {
                    val dx = e2.x - e1.x
                    val dy = e2.y - e1.y
                    if (abs(dx) > abs(dy)) {
                        if (dx > 30) {
                            gestureHandled = true
                            handleAction("swipe_right")
                            return true
                        } else if (dx < -30) {
                            gestureHandled = true
                            handleAction("swipe_left")
                            return true
                        }
                    } else {
                        if (dy > 30) {
                            gestureHandled = true
                            handleAction("swipe_down")
                            return true
                        } else if (dy < -30) {
                            gestureHandled = true
                            handleAction("swipe_up")
                            return true
                        }
                    }
                }
                return false
            }
        })
        
        handleView?.setOnTouchListener { _, event ->
            val detected = gestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startRawX = event.rawX
                    startRawY = event.rawY
                    gestureHandled = false
                }
                MotionEvent.ACTION_UP -> {
                    if (!gestureHandled) {
                        val dx = event.rawX - startRawX
                        val dy = event.rawY - startRawY
                        val density = context.resources.displayMetrics.density
                        val minThreshold = 20 * density
                        if (abs(dx) > abs(dy) && abs(dx) >= minThreshold) {
                            if (dx > 0) handleAction("swipe_right") else handleAction("swipe_left")
                        } else if (abs(dy) >= minThreshold) {
                            if (dy > 0) handleAction("swipe_down") else handleAction("swipe_up")
                        }
                    }
                }
            }
            detected || true
        }
    }

    private fun handleAction(gesture: String) {
        val action = prefs.getString("$prefix$gesture", "none") ?: "none"
        com.example.core.LogKeeper.writeLog("Handle", "Handle ($handleId) gesture: $gesture -> action: $action")
        
        val sidebarIntent = Intent().apply {
            setClassName(context, "com.example.service.SidebarService")
            putExtra("handleId", handleId)
            putExtra("gesture", gesture)
        }

        if (action == "toggle_sidebar") {
            sidebarIntent.action = "com.example.ACTION_TOGGLE_SIDEBAR"
            context.startService(sidebarIntent)
        } else if (action == "toggle_reader") {
            val intent = Intent().apply {
                setClassName(context, "com.example.service.FloatingReaderService")
                putExtra("UNFOLD", true)
            }
            ContextCompat.startForegroundService(context, intent)
        } else if (action.startsWith("open_page:")) {
            val pageType = action.removePrefix("open_page:")
            sidebarIntent.action = "com.example.ACTION_OPEN_PAGE"
            sidebarIntent.putExtra("pageType", pageType)
            context.startService(sidebarIntent)
        } else if (action.startsWith("open_element:")) {
            val elementId = action.removePrefix("open_element:")
            sidebarIntent.action = "com.example.ACTION_EXECUTE_ELEMENT"
            sidebarIntent.putExtra("elementId", elementId)
            context.startService(sidebarIntent)
        } else if (action.startsWith("open_")) {
            val pageType = action.removePrefix("open_")
            sidebarIntent.action = "com.example.ACTION_OPEN_PAGE"
            sidebarIntent.putExtra("pageType", pageType)
            context.startService(sidebarIntent)
        } else if (action.startsWith("action_") || action.startsWith("action:")) {
            val sysAction = action.removePrefix("action_").removePrefix("action:")
            val accIntent = Intent("com.example.ACTION_ACCESSIBILITY_PERFORM").apply {
                putExtra("action", sysAction)
            }
            context.sendBroadcast(accIntent)
        }
    }
}
