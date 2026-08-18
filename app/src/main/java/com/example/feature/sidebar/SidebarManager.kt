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
                com.example.core.LogKeeper.writeLog("SidebarManager", "Execute action: $actionId")
                when (actionId) {
                    "system:dictionary_floating", "system:dictionary_full" -> com.example.feature.miniapps.MiniAppManager.toggleApp(context, "dictionary")
                    "system:translation_floating" -> com.example.feature.miniapps.MiniAppManager.toggleApp(context, "translation")
                    "system:hybrid_grid_floating" -> com.example.feature.miniapps.MiniAppManager.toggleApp(context, "hybrid_grid")
                    else -> {
                        val isSystemAction = actionId.startsWith("system:")
                        val systemActionKey = actionId.removePrefix("system:")
                        val accessibilityService = com.example.feature.system_hub.VianSideAccessibilityService.instance
                        if (isSystemAction && accessibilityService != null && accessibilityService.performAction(systemActionKey)) {
                            com.example.core.LogKeeper.writeLog("SidebarManager", "Handled system action via Accessibility: $systemActionKey")
                        } else if (actionId.endsWith("_floating") || actionId.startsWith("page_window:")) {
                            val pageType = actionId.removePrefix("page_window:").removePrefix("system:").removeSuffix("_floating")
                            com.example.feature.miniapps.MiniAppManager.toggleApp(context, pageType)
                        } else {
                            com.example.core.LogKeeper.writeLog("SidebarManager", "Ignored unrecognized action: $actionId")
                        }
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
        
        val pages = PageManager.getPages(prefs, physicalHandleId)
        val cleanTargetId = targetPageId?.removePrefix("open_page:")
        
        // Find target index based on targetPageId or fallback to default
        val targetIndex = if (cleanTargetId != null) {
            val idx = pages.indexOfFirst { it.id == cleanTargetId || it.type == cleanTargetId }
            if (idx != -1) idx else PageManager.getDefaultPageIndex(prefs, physicalHandleId)
        } else {
            PageManager.getDefaultPageIndex(prefs, physicalHandleId)
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
