import re

with open('app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt', 'r') as f:
    text = f.read()

old_on_connected = """    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        autoScrollManager = AutoScrollManager(this)
        cursorManager = CursorManager(this)
        longScreenshotManager = LongScreenshotManager(this)
        com.example.LogKeeper.writeLog("VianSideAccessibility", "Service connected")
        android.util.Log.d("VianSideAccessibility", "Service connected")
    }"""

new_on_connected = """    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        com.example.core.LogKeeper.writeLog("VianSideAccessibility", "Service connected")
        android.util.Log.d("VianSideAccessibility", "Service connected")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            "ACTION_START_CURSOR" -> {
                if (cursorManager == null) cursorManager = CursorManager(this)
                // Assuming cursorManager has a start method or similar, or it just binds to the window
            }
            "ACTION_STOP_CURSOR" -> {
                cursorManager?.destroy() // Adjust this depending on actual CursorManager methods
                cursorManager = null
            }
            "ACTION_START_AUTOSCROLL" -> {
                if (autoScrollManager == null) autoScrollManager = AutoScrollManager(this)
            }
            "ACTION_STOP_AUTOSCROLL" -> {
                autoScrollManager?.destroy()
                autoScrollManager = null
            }
            "ACTION_START_LONG_SCREENSHOT" -> {
                if (longScreenshotManager == null) longScreenshotManager = LongScreenshotManager(this)
            }
            "ACTION_STOP_LONG_SCREENSHOT" -> {
                longScreenshotManager?.destroy()
                longScreenshotManager = null
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }"""

text = text.replace(old_on_connected, new_on_connected)
text = text.replace('com.example.LogKeeper', 'com.example.core.LogKeeper')

with open('app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt', 'w') as f:
    f.write(text)

