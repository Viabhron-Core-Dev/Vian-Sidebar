package com.example.feature.sidebar

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.WindowManager
import com.example.core.FloatingWindowManager
import com.example.util.AppLogger
import com.example.utils.PageManager

class SidebarManager(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val windowManager: WindowManager
) {
    private var sidebarView: SidebarView? = null
    
    fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val handleId = intent.getStringExtra("handleId") ?: "sidebar"
        
        AppLogger.d("SidebarManager", "handleIntent: action=$action handleId=$handleId")
        
        when (action) {
            "com.example.ACTION_TOGGLE_SIDEBAR" -> {
                toggleSidebar(handleId, handleId)
            }
            "com.example.ACTION_OPEN_PAGE" -> {
                val pageId = intent.getStringExtra("pageType")
                toggleSidebar(handleId, handleId, pageId)
            }
            "com.example.ACTION_EXECUTE_ELEMENT" -> {
                val elementId = intent.getStringExtra("elementId")
                AppLogger.d("SidebarManager", "Execute element: $elementId")
                // TODO: Execute action element
            }
        }
    }
    
    private fun toggleSidebar(physicalHandleId: String, containerId: String, targetPageId: String? = null) {
        if (sidebarView?.windowToken != null && sidebarView?.containerId == containerId) {
            closeSidebar()
        } else {
            showSidebar(physicalHandleId, containerId, targetPageId)
        }
    }
    
    private fun showSidebar(physicalHandleId: String, containerId: String, targetPageId: String? = null) {
        if (sidebarView != null) {
            closeSidebar()
        }
        
        val pages = PageManager.getPages(prefs, containerId)
        
        // Find target index based on targetPageId or fallback to default
        val targetIndex = if (targetPageId != null) {
            val idx = pages.indexOfFirst { it.id == targetPageId }
            if (idx != -1) idx else PageManager.getDefaultPageIndex(prefs, containerId)
        } else {
            PageManager.getDefaultPageIndex(prefs, containerId)
        }
        
        sidebarView = SidebarView(context, prefs, windowManager, physicalHandleId, containerId, pages, targetIndex) {
            closeSidebar()
        }
        sidebarView?.attach()
    }
    
    fun closeSidebar() {
        sidebarView?.detach()
        sidebarView = null
    }
    
    fun onTrimMemory(level: Int) {
        // Freeze/Thaw logic: unload if not Handle 1
        if (sidebarView?.containerId != "sidebar") {
            closeSidebar()
        }
    }
}
