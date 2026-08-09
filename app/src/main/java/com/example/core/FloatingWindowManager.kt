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

    fun onTrimMemory(level: Int) {
        windows.forEach { it.onTrimMemory(level) }
    }
}
