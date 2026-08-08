import re

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'r') as f:
    content = f.read()

widget_click = """                } else if (item is SidebarItem.Widget) {
                    WidgetOverlayView(context, item.widgetId)
                    currentFolderPopup?.dismiss()
                    onCloseSidebar()
                } else if (item is SidebarItem.QuickTile) {"""

content = content.replace("                } else if (item is SidebarItem.QuickTile) {", widget_click)

with open('app/src/main/java/com/example/service/AppsPageView.kt', 'w') as f:
    f.write(content)

