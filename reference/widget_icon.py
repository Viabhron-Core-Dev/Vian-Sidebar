import re

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'r') as f:
    content = f.read()

widget_icon = """            is SidebarItem.SettingsShortcut -> parsed.iconResId
            is SidebarItem.Widget -> {
                try {
                    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                    val info = appWidgetManager.getAppWidgetInfo(parsed.widgetId)
                    if (info != null) {
                        val dr = context.packageManager.getDrawable(info.provider.packageName, info.icon, info.providerInfo?.applicationInfo)
                        if (dr != null) {
                            return getBitmapFromDrawable(dr)
                        }
                    }
                } catch (e: Exception) {}
                android.R.drawable.ic_menu_gallery
            }
            is SidebarItem.Link -> android.R.drawable.ic_menu_set_as"""

content = content.replace("""            is SidebarItem.SettingsShortcut -> parsed.iconResId
            is SidebarItem.Link -> android.R.drawable.ic_menu_set_as""", widget_icon)

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'w') as f:
    f.write(content)

