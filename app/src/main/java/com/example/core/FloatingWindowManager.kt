package com.example.core

object FloatingWindowManager {
    private val windows = mutableListOf<FloatingWindow>()
    val activeWindows: List<FloatingWindow> get() = windows.toList()

    fun addWindow(window: FloatingWindow) {
        if (!windows.contains(window)) {
            windows.add(window)
        }
        window.show()
    }

    fun removeWindow(window: FloatingWindow) {
        window.hide()
        windows.remove(window)
    }

    fun bringToFront(window: FloatingWindow) {
        if (windows.remove(window)) {
            windows.add(window)
            // Bring to front by removing and re-adding
            window.view?.let {
                window.windowManager.removeView(it)
                window.windowManager.addView(it, window.layoutParams)
            }
        }
    }

    
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
                com.example.util.AppLogger.d("FloatingWindowManager", "Collision: ${draggedWindow.title} with ${window.title}")
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

    fun onTrimMemory(level: Int) {
        windows.forEach { it.onTrimMemory(level) }
    }
}
