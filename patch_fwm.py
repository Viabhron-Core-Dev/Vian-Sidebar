with open('app/src/main/java/com/example/core/FloatingWindowManager.kt', 'r') as f:
    text = f.read()

collision_code = """
    fun checkCollisions(draggedWindow: FloatingWindow) {
        val draggedView = draggedWindow.view ?: return
        val draggedRect = android.graphics.Rect()
        draggedView.getGlobalVisibleRect(draggedRect)

        for (window in windows.toList()) {
            if (window == draggedWindow) continue
            val view = window.view ?: continue
            val rect = android.graphics.Rect()
            view.getGlobalVisibleRect(rect)
            
            if (android.graphics.Rect.intersects(draggedRect, rect)) {
                com.example.util.AppLogger.d("FloatingWindowManager", "Collision: ${draggedWindow.windowId} with ${window.windowId}")
                // Implement stacking: For now, snap dragged window to the target window
                val params = window.layoutParams
                val draggedParams = draggedWindow.layoutParams
                if (params != null && draggedParams != null) {
                    draggedParams.x = params.x + 10
                    draggedParams.y = params.y + 10
                    draggedWindow.windowManager.updateViewLayout(draggedView, draggedParams)
                }
                
                // TODO: Actual grouping UI logic here
            }
        }
    }
"""

text = text.replace('fun onTrimMemory', collision_code + '\n    fun onTrimMemory')

with open('app/src/main/java/com/example/core/FloatingWindowManager.kt', 'w') as f:
    f.write(text)
