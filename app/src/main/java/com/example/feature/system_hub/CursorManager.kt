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
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class CursorManager(private val service: AccessibilityService) {
    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    
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
        
        pointerView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        controlView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        trackpadView?.let { try { windowManager.removeView(it) } catch (e: Exception) {} }
        
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
        val density = service.resources.displayMetrics.density
        val layout = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EE222222"))
                cornerRadius = 24f * density
                setStroke((1 * density).toInt(), Color.parseColor("#44FFFFFF"))
            }
            setPadding((12 * density).toInt(), (8 * density).toInt(), (12 * density).toInt(), (8 * density).toInt())
            gravity = Gravity.CENTER
        }
        
        val btnPause = ImageButton(service).apply {
            setImageResource(android.R.drawable.ic_media_pause)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
            setOnClickListener {
                isPaused = !isPaused
                setImageResource(if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
                trackpadView?.visibility = if (isPaused) View.GONE else View.VISIBLE
            }
        }

        val btnClick = ImageButton(service).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.parseColor("#4CAF50"))
            setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
            setOnClickListener {
                val tipX = pointerX + (1f * density)
                val tipY = pointerY + (1f * density)
                performClick(tipX, tipY)
            }
        }
        
        val btnMode = ImageButton(service).apply {
            setImageResource(if (isGlassShield) android.R.drawable.ic_menu_crop else android.R.drawable.ic_menu_gallery)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
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
            setPadding((10 * density).toInt(), (6 * density).toInt(), (10 * density).toInt(), (6 * density).toInt())
            setOnClickListener { stop() }
        }
        
        layout.addView(btnPause)
        layout.addView(btnClick)
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
            y = (100 * density).toInt()
        }
        
        controlView = layout
        windowManager.addView(controlView, params)
    }
    
    private fun createTrackpadView() {
        val density = service.resources.displayMetrics.density
        trackpadView = FrameLayout(service).apply {
            var downX = 0f
            var downY = 0f
            var lastX = 0f
            var lastY = 0f
            var lastTapUpTime = 0L
            var lastTapUpX = 0f
            var lastTapUpY = 0f
            var isDoubleTapCandidate = false
            
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        lastX = event.rawX
                        lastY = event.rawY
                        
                        val now = System.currentTimeMillis()
                        val timeDiff = now - lastTapUpTime
                        val distFromLastTap = hypot((event.rawX - lastTapUpX).toDouble(), (event.rawY - lastTapUpY).toDouble())
                        
                        isDoubleTapCandidate = (timeDiff in 40..400) && (distFromLastTap < 40 * density)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - lastX
                        val dy = event.rawY - lastY
                        val distFromDown = hypot((event.rawX - downX).toDouble(), (event.rawY - downY).toDouble())
                        
                        if (distFromDown > 18 * density) {
                            isDoubleTapCandidate = false
                        }
                        
                        lastX = event.rawX
                        lastY = event.rawY
                        
                        pointerX += dx * 1.35f
                        pointerY += dy * 1.35f
                        
                        pointerX = max(0f, min(screenWidth.toFloat(), pointerX))
                        pointerY = max(0f, min(screenHeight.toFloat(), pointerY))
                        
                        updatePointerPosition()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val distFromDown = hypot((event.rawX - downX).toDouble(), (event.rawY - downY).toDouble())
                        if (distFromDown < 20 * density) {
                            // Valid tap released without dragging
                            if (isDoubleTapCandidate) {
                                val tipX = pointerX + (1f * density)
                                val tipY = pointerY + (1f * density)
                                performClick(tipX, tipY)
                                lastTapUpTime = 0L // Reset so 3rd tap isn't immediately double tap
                            } else {
                                lastTapUpTime = System.currentTimeMillis()
                                lastTapUpX = event.rawX
                                lastTapUpY = event.rawY
                            }
                        } else {
                            lastTapUpTime = 0L
                        }
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        isDoubleTapCandidate = false
                        lastTapUpTime = 0L
                        true
                    }
                    else -> false
                }
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
        val density = service.resources.displayMetrics.density
        
        if (isGlassShield) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.TOP or Gravity.START
            params.x = 0
            params.y = 0
            trackpadView?.setBackgroundColor(Color.TRANSPARENT)
        } else {
            val sizeWidth = (280 * density).toInt()
            val sizeHeight = (280 * density).toInt()
            params.width = sizeWidth
            params.height = sizeHeight
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.y = (170 * density).toInt()
            params.x = (16 * density).toInt()
            trackpadView?.background = GradientDrawable().apply {
                setColor(Color.parseColor("#55333333"))
                cornerRadius = 16f * density
                setStroke((1.5f * density).toInt(), Color.parseColor("#88FFFFFF"))
            }
        }
        
        windowManager.updateViewLayout(trackpadView, params)
    }
    
    private fun updatePointerPosition() {
        val params = pointerView?.layoutParams as? WindowManager.LayoutParams ?: return
        params.x = pointerX.toInt()
        params.y = pointerY.toInt()
        try {
            windowManager.updateViewLayout(pointerView, params)
        } catch (e: Exception) {}
    }
    
    private fun performClick(x: Float, y: Float) {
        // Visual tap animation feedback on the cursor
        pointerView?.animate()
            ?.scaleX(0.75f)
            ?.scaleY(0.75f)
            ?.setDuration(80)
            ?.withEndAction {
                pointerView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(80)?.start()
            }
            ?.start()

        val trackpad = trackpadView ?: return
        val originalParams = trackpad.layoutParams as? WindowManager.LayoutParams ?: return
        
        // Temporarily make trackpad not touchable so injected gesture penetrates through to target window
        val touchDisabledParams = WindowManager.LayoutParams().apply {
            copyFrom(originalParams)
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        try {
            windowManager.updateViewLayout(trackpad, touchDisabledParams)
        } catch (e: Exception) {}

        var restored = false
        fun restoreTouchable() {
            if (restored) return
            restored = true
            try {
                val currentP = trackpadView?.layoutParams as? WindowManager.LayoutParams ?: return
                currentP.flags = currentP.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                windowManager.updateViewLayout(trackpadView, currentP)
            } catch (e: Exception) {}
        }

        // Safety fallback timer to restore touchability if gesture callback does not fire
        mainHandler.postDelayed({
            restoreTouchable()
        }, 300)

        val path = Path()
        path.moveTo(x, y)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 50))
        
        try {
            service.dispatchGesture(
                gestureBuilder.build(),
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        restoreTouchable()
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        restoreTouchable()
                    }
                },
                mainHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
            restoreTouchable()
        }
    }
}

