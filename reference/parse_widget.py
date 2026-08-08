import re

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'r') as f:
    content = f.read()

widget_parser = """        } else if (id.startsWith("widget:")) {
            try {
                val parts = id.split(":", limit = 3)
                if (parts.size >= 2) {
                    val widgetId = parts[1].toInt()
                    val jsonStr = parts.getOrNull(2)
                    var label = "Widget $widgetId"
                    if (jsonStr != null && jsonStr.isNotEmpty()) {
                        val json = org.json.JSONObject(jsonStr)
                        label = json.optString("label", label)
                    }
                    return SidebarItem.Widget(widgetId, label)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (id.startsWith("folder:")) {"""

content = content.replace("        } else if (id.startsWith(\"folder:\")) {", widget_parser)

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'w') as f:
    f.write(content)

