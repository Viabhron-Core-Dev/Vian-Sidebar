import re

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'r') as f:
    content = f.read()

widget_icon = """            is SidebarItem.SettingsShortcut -> parsed.iconResId
            is SidebarItem.Widget -> {
                try {
                    val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
                    val info = appWidgetManager.getAppWidgetInfo(parsed.widgetId)
                    if (info != null) {
                        val dr = info.loadIcon(context, context.resources.displayMetrics.densityDpi)
                        if (dr != null) {
                            return getBitmapFromDrawable(dr)
                        }
                    }
                } catch (e: Exception) {}
                android.R.drawable.ic_menu_gallery
            }
            is SidebarItem.Link -> android.R.drawable.ic_menu_set_as"""

# I need to be careful with replace because I already replaced it once.
content = re.sub(r'is SidebarItem\.SettingsShortcut -> parsed\.iconResId.*?is SidebarItem\.Link -> android\.R\.drawable\.ic_menu_set_as', widget_icon, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'w') as f:
    f.write(content)

