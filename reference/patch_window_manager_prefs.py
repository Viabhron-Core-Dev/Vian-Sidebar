import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

# Replace initialization of lastWidth, lastHeight, initialX, initialY with shared preferences
target_init = """    private var isMinimized = false
    private var lastWidth = 800
    private var lastHeight = 1000
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0"""

replacement_init = """    private val prefs = context.getSharedPreferences("AppyworkPrefs", Context.MODE_PRIVATE)
    private var isMinimized = prefs.getBoolean("isMinimized", false)
    private var lastWidth = prefs.getInt("lastWidth", 800)
    private var lastHeight = prefs.getInt("lastHeight", 1000)
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private var initialX = prefs.getInt("lastX", 100)
    private var initialY = prefs.getInt("lastY", 200)"""

target_show = """        windowLayoutParams = WindowManager.LayoutParams(
            lastWidth,
            lastHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }"""

replacement_show = """        windowLayoutParams = WindowManager.LayoutParams(
            if (isMinimized) (56 * density).toInt() else lastWidth,
            if (isMinimized) (56 * density).toInt() else lastHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("lastX", 100)
            y = prefs.getInt("lastY", 200)
        }"""

target_show_fold = """        val expandedContainer = floatingView?.findViewById<LinearLayout>(R.id.appywork_expanded_container)
        val bubble = floatingView?.findViewById<FrameLayout>(R.id.appywork_bubble)
        val topbar = floatingView?.findViewById<LinearLayout>(R.id.appywork_topbar)"""

replacement_show_fold = """        val expandedContainer = floatingView?.findViewById<LinearLayout>(R.id.appywork_expanded_container)
        val bubble = floatingView?.findViewById<FrameLayout>(R.id.appywork_bubble)
        val topbar = floatingView?.findViewById<LinearLayout>(R.id.appywork_topbar)
        
        if (isMinimized) {
            expandedContainer?.visibility = View.GONE
            bubble?.visibility = View.VISIBLE
        }"""

target_toggle = """        isMinimized = !isMinimized"""
replacement_toggle = """        isMinimized = !isMinimized
        prefs.edit().putBoolean("isMinimized", isMinimized).apply()"""

target_drag_up = """            MotionEvent.ACTION_UP -> {
                val diffX = Math.abs(event.rawX - initialTouchX)
                val diffY = Math.abs(event.rawY - initialTouchY)"""
replacement_drag_up = """            MotionEvent.ACTION_UP -> {
                prefs.edit().putInt("lastX", windowLayoutParams?.x ?: 100).putInt("lastY", windowLayoutParams?.y ?: 200).apply()
                val diffX = Math.abs(event.rawX - initialTouchX)
                val diffY = Math.abs(event.rawY - initialTouchY)"""

target_resize_move = """            MotionEvent.ACTION_MOVE -> {
                val newWidth = initialX + (event.rawX - initialTouchX).toInt()
                val newHeight = initialY + (event.rawY - initialTouchY).toInt()
                windowLayoutParams?.width = max((200 * density).toInt(), newWidth)
                windowLayoutParams?.height = max((200 * density).toInt(), newHeight)
                windowManager.updateViewLayout(floatingView, windowLayoutParams)
                return true
            }"""
replacement_resize_move = """            MotionEvent.ACTION_MOVE -> {
                val newWidth = initialX + (event.rawX - initialTouchX).toInt()
                val newHeight = initialY + (event.rawY - initialTouchY).toInt()
                windowLayoutParams?.width = max((200 * density).toInt(), newWidth)
                windowLayoutParams?.height = max((200 * density).toInt(), newHeight)
                lastWidth = windowLayoutParams?.width ?: 800
                lastHeight = windowLayoutParams?.height ?: 1000
                prefs.edit().putInt("lastWidth", lastWidth).putInt("lastHeight", lastHeight).apply()
                windowManager.updateViewLayout(floatingView, windowLayoutParams)
                return true
            }"""

if target_init in content:
    content = content.replace(target_init, replacement_init)
    content = content.replace(target_show, replacement_show)
    content = content.replace(target_show_fold, replacement_show_fold)
    content = content.replace(target_toggle, replacement_toggle)
    content = content.replace(target_drag_up, replacement_drag_up)
    content = content.replace(target_resize_move, replacement_resize_move)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched Prefs successfully")
else:
    print("Target Prefs not found")
