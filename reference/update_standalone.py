import re

with open("app/src/main/java/com/example/service/FloatingReaderService.kt", "r") as f:
    content = f.read()

# Add standaloneSidebarView variable
content = content.replace("    private var sidebarView: SidebarView? = null", "    private var sidebarView: SidebarView? = null\n    private var standaloneSidebarView: SidebarView? = null")

# Add showStandalonePage function
standalone_func = """
    private fun showStandalonePage(type: String) {
        if (standaloneSidebarView != null) {
            windowManager.removeView(standaloneSidebarView)
            standaloneSidebarView = null
        }
        
        val config = com.example.utils.SidebarPage(id = "standalone_$type", title = type.replaceFirstChar { it.uppercase() }, type = type)
        val tempPagesList = mutableListOf<View>()
        val pageView = when (config.type) {
            "apps" -> {
                val p = AppsPageView(this, config, appsManager, serviceScope,
                    onCloseSidebar = { standaloneSidebarView?.close() },
                    onHeightChanged = { newHeight -> standaloneSidebarView?.updatePageStyles(config, newHeight) }
                )
                p.updateData(appsManager.activeItems)
                p
            }
            "scheduler" -> SchedulerPageView(this, serviceScope)
            "calculator" -> CalculatorPageView(this)
            "compass" -> CompassPageView(this)
            "notifications" -> NotificationPageView(this, { standaloneSidebarView?.close() }) { newHeight ->
                standaloneSidebarView?.updatePageStyles(config, newHeight)
            }
            "widgets_grid" -> WidgetsGridPageView(this, config.id) { newHeight ->
                standaloneSidebarView?.updatePageStyles(config, newHeight)
            }
            else -> null
        }
        
        if (pageView != null) {
            tempPagesList.add(pageView)
            standaloneSidebarView = SidebarView(this, prefs, windowManager, tempPagesList, listOf(config), 0, onClose = { 
                standaloneSidebarView = null 
            }, onEditPageClicked = null)
            
            serviceLifecycleOwner?.let {
                standaloneSidebarView?.setViewTreeLifecycleOwner(it)
                standaloneSidebarView?.setViewTreeViewModelStoreOwner(it)
                standaloneSidebarView?.setViewTreeSavedStateRegistryOwner(it)
            }
            
            standaloneSidebarView?.attach()
            if (pageView is AppsPageView) {
                standaloneSidebarView?.updatePageStyles(config, pageView.getCurrentHeightPx())
            } else if (pageView is WidgetsGridPageView) {
                standaloneSidebarView?.updatePageStyles(config, pageView.getCurrentHeightPx())
            }
        }
    }
"""

content = content.replace("    fun openSidebarPage(type: String) {\n        showSidebar()\n        val pageConfigs = PageManager.getPages(prefs)\n        val index = pageConfigs.indexOfFirst { it.type == type }\n        if (index != -1) {\n            sidebarView?.goToPage(index)\n        }\n    }", """    fun openSidebarPage(type: String) {
        val pageConfigs = PageManager.getPages(prefs)
        val index = pageConfigs.indexOfFirst { it.type == type }
        if (index != -1) {
            showSidebar()
            sidebarView?.goToPage(index)
        } else {
            showStandalonePage(type)
        }
    }
""" + standalone_func)

# Make sure `detach()` removes it
content = content.replace("        sidebarView?.detach()\n        sidebarView = null", "        sidebarView?.detach()\n        sidebarView = null\n        standaloneSidebarView?.detach()\n        standaloneSidebarView = null")

with open("app/src/main/java/com/example/service/FloatingReaderService.kt", "w") as f:
    f.write(content)
