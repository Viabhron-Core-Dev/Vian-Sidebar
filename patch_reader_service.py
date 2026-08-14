import re

filepath = 'app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# 1. Add bubbleView
content = content.replace("private var isFolded = true", "private var isFolded = true\n    private var bubbleView: View? = null\n    private var bubbleLayoutParams: WindowManager.LayoutParams? = null")

# 2. Add closeReader method before setFolded
close_method = """
    private fun closeReader() {
        saveCurrentPosition()
        com.example.core.LogKeeper.writeLog("eBookReader", "Closing reader")
        try {
            if (floatingView.windowToken != null) {
                windowManager.removeView(floatingView)
            }
        } catch (e: Exception) {}
        try {
            bubbleView?.let { if (it.windowToken != null) windowManager.removeView(it) }
        } catch (e: Exception) {}
        stopSelf()
    }

    fun setFolded(folded: Boolean) {"""
content = content.replace("fun setFolded(folded: Boolean) {", close_method)

# 3. Replace btn_exit_bottom logic
old_exit = """        floatingView.findViewById<View>(R.id.btn_exit_bottom)?.setOnClickListener {
            saveCurrentPosition()
            com.example.core.LogKeeper.writeLog("eBookReader", "Closing reader window")
            floatingView.visibility = View.GONE
            
        }"""
new_exit = """        floatingView.findViewById<View>(R.id.btn_exit_bottom)?.setOnClickListener {
            closeReader()
        }"""
content = content.replace(old_exit, new_exit)
# There are two btn_exit_bottom listeners in the file! I'll replace all.
old_exit2 = """        floatingView.findViewById<View>(R.id.btn_exit_bottom)?.setOnClickListener {
            saveCurrentPosition()
            com.example.core.LogKeeper.writeLog("eBookReader", "Closing reader")
            floatingView.visibility = View.GONE
            
        }"""
content = content.replace(old_exit2, new_exit)

# 4. Refactor setFolded
old_set_folded = """    fun setFolded(folded: Boolean) {
        isFolded = folded
        if (folded) {
            // Save expanded position before folding
            savedWindowX = layoutParams.x
            savedWindowY = layoutParams.y
            prefs.edit()
                .putInt("win_x", savedWindowX)
                .putInt("win_y", savedWindowY)
                .apply()

            bubbleIcon.visibility = View.VISIBLE
            windowContainer.visibility = View.GONE
            floatingView.visibility = View.VISIBLE
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            
            // Restore bubble position
            layoutParams.x = foldedX
            layoutParams.y = foldedY
            
            isAutoScrolling = false // pause scroll
        } else {
            // Save bubble position before expanding
            foldedX = layoutParams.x
            foldedY = layoutParams.y
            prefs.edit()
                .putInt("fold_x", foldedX)
                .putInt("fold_y", foldedY)
                .apply()

            bubbleIcon.visibility = View.GONE
            windowContainer.visibility = View.VISIBLE
            floatingView.visibility = View.VISIBLE
            toolbarContainer.visibility = View.GONE
            
            val metrics = resources.displayMetrics
            val maxW = metrics.widthPixels
            val maxH = metrics.heightPixels
            
            layoutParams.width = Math.min(savedWindowWidth, maxW)
            layoutParams.height = Math.min(savedWindowHeight, maxH)
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            
            layoutParams.x = savedWindowX
            layoutParams.y = savedWindowY
            
            if (layoutParams.x + layoutParams.width > maxW) {
                layoutParams.x = maxW - layoutParams.width
            }
            if (layoutParams.x < 0) layoutParams.x = 0
            
            if (layoutParams.y + layoutParams.height > maxH) {
                layoutParams.y = maxH - layoutParams.height
            }
            if (layoutParams.y < 0) layoutParams.y = 0
        }
        windowManager.updateViewLayout(floatingView, layoutParams)
        updatePersistentNotification()
    }"""

new_set_folded = """    fun setFolded(folded: Boolean) {
        isFolded = folded
        if (folded) {
            savedWindowX = layoutParams.x
            savedWindowY = layoutParams.y
            prefs.edit()
                .putInt("win_x", savedWindowX)
                .putInt("win_y", savedWindowY)
                .apply()

            try {
                if (floatingView.windowToken != null) {
                    windowManager.removeView(floatingView)
                }
            } catch (e: Exception) {}

            if (bubbleView == null) {
                bubbleView = LayoutInflater.from(this).inflate(R.layout.layout_floating_bubble, null)
                val icon = bubbleView?.findViewById<ImageView>(R.id.bubble_icon)
                icon?.setImageResource(R.mipmap.app_icon)
                
                var bInitialX = 0
                var bInitialY = 0
                var bInitialTouchX = 0f
                var bInitialTouchY = 0f
                var lastClickTime = 0L

                bubbleView?.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            bInitialX = bubbleLayoutParams?.x ?: 0
                            bInitialY = bubbleLayoutParams?.y ?: 0
                            bInitialTouchX = event.rawX
                            bInitialTouchY = event.rawY
                            lastClickTime = System.currentTimeMillis()
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            bubbleLayoutParams?.x = bInitialX + (event.rawX - bInitialTouchX).toInt()
                            bubbleLayoutParams?.y = bInitialY + (event.rawY - bInitialTouchY).toInt()
                            windowManager.updateViewLayout(bubbleView, bubbleLayoutParams)
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (System.currentTimeMillis() - lastClickTime < 200) {
                                setFolded(false)
                            } else {
                                foldedX = bubbleLayoutParams?.x ?: 0
                                foldedY = bubbleLayoutParams?.y ?: 0
                                prefs.edit().putInt("fold_x", foldedX).putInt("fold_y", foldedY).apply()
                            }
                            true
                        }
                        else -> false
                    }
                }
                
                bubbleLayoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = foldedX
                    y = foldedY
                }
            }
            
            try {
                if (bubbleView?.windowToken == null) {
                    windowManager.addView(bubbleView, bubbleLayoutParams)
                }
            } catch (e: Exception) {}
            
            isAutoScrolling = false
        } else {
            if (bubbleLayoutParams != null) {
                foldedX = bubbleLayoutParams!!.x
                foldedY = bubbleLayoutParams!!.y
                prefs.edit().putInt("fold_x", foldedX).putInt("fold_y", foldedY).apply()
            }
            try {
                bubbleView?.let { if (it.windowToken != null) windowManager.removeView(it) }
            } catch (e: Exception) {}

            toolbarContainer.visibility = View.GONE
            
            val metrics = resources.displayMetrics
            val maxW = metrics.widthPixels
            val maxH = metrics.heightPixels
            
            layoutParams.width = Math.min(savedWindowWidth, maxW)
            layoutParams.height = Math.min(savedWindowHeight, maxH)
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            
            layoutParams.x = savedWindowX
            layoutParams.y = savedWindowY
            
            if (layoutParams.x + layoutParams.width > maxW) layoutParams.x = maxW - layoutParams.width
            if (layoutParams.x < 0) layoutParams.x = 0
            
            if (layoutParams.y + layoutParams.height > maxH) layoutParams.y = maxH - layoutParams.height
            if (layoutParams.y < 0) layoutParams.y = 0
            
            try {
                if (floatingView.windowToken == null) {
                    windowManager.addView(floatingView, layoutParams)
                } else {
                    windowManager.updateViewLayout(floatingView, layoutParams)
                }
            } catch (e: Exception) {}
        }
        updatePersistentNotification()
    }"""
content = content.replace(old_set_folded, new_set_folded)

with open(filepath, 'w') as f:
    f.write(content)
print("FloatingReaderService updated")
