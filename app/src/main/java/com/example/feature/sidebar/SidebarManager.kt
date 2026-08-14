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
        val gesture = intent.getStringExtra("gesture") ?: "tap"
        val containerId = "${handleId}_${gesture}"
        
        
        AppLogger.d("SidebarManager", "handleIntent: action=$action handleId=$handleId")
        
        when (action) {
            "com.example.ACTION_TOGGLE_SIDEBAR" -> {
                toggleSidebar(handleId, containerId)
            }
            "com.example.ACTION_OPEN_PAGE" -> {
                val pageId = intent.getStringExtra("pageType")
                toggleSidebar(handleId, containerId, pageId)
            }
            "com.example.ACTION_EXECUTE_ELEMENT", "EXECUTE_ACTION" -> {
                val actionId = intent.getStringExtra("ACTION_ID") ?: intent.getStringExtra("elementId") ?: return
                AppLogger.d("SidebarManager", "Execute action/element: $actionId")
                when (actionId) {
                    "system:dictionary_floating" -> com.example.feature.miniapps.MiniAppManager.toggleApp(context, "dictionary")
                    "system:translation_floating" -> com.example.feature.miniapps.MiniAppManager.toggleApp(context, "translation")
                    "system:hybrid_grid_floating" -> com.example.feature.miniapps.MiniAppManager.toggleApp(context, "hybrid_grid")
                    "system:dictionary_full" -> {
                        // TODO: Implement full dictionary if needed, for now just use floating
                        com.example.feature.miniapps.MiniAppManager.toggleApp(context, "dictionary")
                    }
                    else -> {
                        // Forward to MiniAppManager as generic
                        val pageType = actionId.removePrefix("system:").removeSuffix("_floating")
                        com.example.feature.miniapps.MiniAppManager.toggleApp(context, pageType)
                    }
                }
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
