package com.example.feature.system_hub

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.R
import kotlin.math.max
import kotlin.math.min

class CursorManager(private val service: AccessibilityService) {
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    
    private var pointerView: ImageView? = null
    private var controlView: View? = null
    private var trackpadView: View? = null
    
    var isRunning = false
    private var isPaused = false
    private var isGlassShield = true // True = Full screen, False = Trackpad
    
    private var pointerX = 0f
    private var pointerY = 0f
    
    private var screenWidth = 0
    private var screenHeight = 0
    
    fun start() {
        if (isRunning) return
        isRunning = true
        isPaused = false
        
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        
        pointerX = screenWidth / 2f
        pointerY = screenHeight / 2f
        
        createPointerView()
        createTrackpadView()
        createControlView()
        updateTrackpadLayout()
    }
    
    fun stop() {
        if (!isRunning) return
        isRunning = false
        
        pointerView?.let { windowManager.removeView(it) }
        controlView?.let { windowManager.removeView(it) }
        trackpadView?.let { windowManager.removeView(it) }
        
        pointerView = null
        controlView = null
        trackpadView = null
    }
    
    private fun createPointerView() {
        pointerView = ImageView(service).apply {
            setImageResource(R.drawable.ic_cursor_pointer)
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = pointerX.toInt()
            y = pointerY.toInt()
        }
        
        windowManager.addView(pointerView, params)
    }
    
    private fun createControlView() {
        val layout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#DD222222"))
                cornerRadius = 24f * service.resources.displayMetrics.density
            }
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }
        
        val btnPause = ImageButton(service).apply {
            setImageResource(android.R.drawable.ic_media_pause)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                isPaused = !isPaused
                setImageResource(if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
                trackpadView?.visibility = if (isPaused) View.GONE else View.VISIBLE
            }
        }
        
        val btnMode = ImageButton(service).apply {
            setImageResource(android.R.drawable.ic_menu_crop)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setPadding(48, 0, 48, 0)
            setOnClickListener {
                isGlassShield = !isGlassShield
                setImageResource(if (isGlassShield) android.R.drawable.ic_menu_crop else android.R.drawable.ic_menu_gallery)
                updateTrackpadLayout()
            }
        }
        
        val btnExit = ImageButton(service).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setOnClickListener { stop() }
        }
        
        layout.addView(btnPause)
        layout.addView(btnMode)
        layout.addView(btnExit)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = (100 * service.resources.displayMetrics.density).toInt()
        }
        
        controlView = layout
        windowManager.addView(controlView, params)
    }
    
    private fun createTrackpadView() {
        trackpadView = FrameLayout(service).apply {
            var lastX = 0f
            var lastY = 0f
            var lastTouchTime = 0L
            
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                        
                        val now = System.currentTimeMillis()
                        if (now - lastTouchTime < 300) {
                            val tipX = pointerX + (1 * service.resources.displayMetrics.density)
                            val tipY = pointerY + (1 * service.resources.displayMetrics.density)
                            performClick(tipX, tipY)
                        }
                        lastTouchTime = now
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - lastX
                        val dy = event.rawY - lastY
                        lastX = event.rawX
                        lastY = event.rawY
                        
                        pointerX += dx * 1.5f
                        pointerY += dy * 1.5f
                        
                        pointerX = max(0f, min(screenWidth.toFloat(), pointerX))
                        pointerY = max(0f, min(screenHeight.toFloat(), pointerY))
                        
                        updatePointerPosition()
                    }
                }
                true
            }
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(trackpadView, params)
    }
    
    private fun updateTrackpadLayout() {
        val params = trackpadView?.layoutParams as? WindowManager.LayoutParams ?: return
        
        if (isGlassShield) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 0
            trackpadView?.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val sizeWidth = (300 * service.resources.displayMetrics.density).toInt()
            val sizeHeight = (300 * service.resources.displayMetrics.density).toInt()
            params.width = sizeWidth
            params.height = sizeHeight
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.y = (180 * service.resources.displayMetrics.density).toInt()
            params.x = (16 * service.resources.displayMetrics.density).toInt()
            trackpadView?.setBackgroundColor(Color.parseColor("#44888888"))
        }
        
        windowManager.updateViewLayout(trackpadView, params)
    }
    
    private fun updatePointerPosition() {
        val params = pointerView?.layoutParams as? WindowManager.LayoutParams ?: return
        params.x = pointerX.toInt()
        params.y = pointerY.toInt()
        windowManager.updateViewLayout(pointerView, params)
    }
    
    private fun performClick(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        service.dispatchGesture(gestureBuilder.build(), null, null)
    }
}
