import re

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'r') as f:
    content = f.read()

widget_class = """    data class SettingsShortcut(
        val action: String,
        override val label: String,
        val iconResId: Int
    ) : SidebarItem() {
        override var id = "settings_shortcut:$action"
    }
    
    data class Widget(
        val widgetId: Int,
        override val label: String,
        val iconBitmap: Bitmap? = null
    ) : SidebarItem() {
        override var id = "widget:$widgetId"
    }"""

content = content.replace("""    data class SettingsShortcut(
        val action: String,
        override val label: String,
        val iconResId: Int
    ) : SidebarItem() {
        override var id = "settings_shortcut:$action"
    }""", widget_class)

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'w') as f:
    f.write(content)

