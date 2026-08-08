import sys
import re

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'r') as f:
    content = f.read()

# Add properties
prop_target = """    private var widgetPickerReceiver: android.content.BroadcastReceiver? = null"""
prop_replacement = """    private var widgetPickerReceiver: android.content.BroadcastReceiver? = null
    private var wasSidebarEditOpen = false
    private var wasWidgetsGridEditOpen = false
    private var lastWidgetsGridPageId = "" """

if "wasSidebarEditOpen" not in content[:content.find("widgetPickerReceiver") + 500]:
    content = content.replace(prop_target, prop_replacement)

# Fix NotificationPageView instantiation
content = content.replace("""p = NotificationPageView(this) { newHeight ->""", """p = NotificationPageView(this, { closeSidebar() }) { newHeight ->""")
content = content.replace("""p = NotificationPageView(this, config.id) { newHeight ->""", """p = NotificationPageView(this, { closeSidebar() }) { newHeight ->""")

# Fix other missing onClose parameters
# For SidebarView?
content = content.replace("""sidebarView = SidebarView(this, prefs, windowManager, sidebarPagesList, PageManager.getPages(prefs), sidebarDefaultIndex,""",
                          """sidebarView = SidebarView(this, prefs, windowManager, sidebarPagesList, PageManager.getPages(prefs), sidebarDefaultIndex, onClose = { closeSidebar() },""")

# Wait, the error is at 620:17 No value passed for parameter 'onClose'.
# Let's check what is at 620: it's SidebarView instantiation!
# Also 667:13 No value passed for parameter 'onClose'. That is WidgetsGridEditOverlayView!
# Let's see: `widgetsGridEditOverlayView = WidgetsGridEditOverlayView(`
content = content.replace("""widgetsGridEditOverlayView = WidgetsGridEditOverlayView(
            this, pageId, windowManager,
            onAddClicked = {""",
            """widgetsGridEditOverlayView = WidgetsGridEditOverlayView(
            this, pageId, windowManager,
            onAddClicked = {""")
# I should just use regex to fix WidgetsGridEditOverlayView instantiation
content = re.sub(r'(widgetsGridEditOverlayView = WidgetsGridEditOverlayView\(\s*this,\s*pageId,\s*windowManager,\s*onAddClicked = \{[\s\S]*?\}\n\s*)\)', r'\1, onClose = { widgetsGridEditOverlayView?.detach() })', content)

# Also fix the 551 missing onCloseSidebar parameter. It's for an earlier NotificationPageView?
# Wait! "notifications" -> { var p: NotificationPageView? = null ... }
# What about 551?
# Let's replace any NotificationPageView(this) or NotificationPageView(this, config.id) 
content = re.sub(r'NotificationPageView\(this\)\s*\{', r'NotificationPageView(this, { closeSidebar() }) {', content)
content = re.sub(r'NotificationPageView\(this,\s*config\.id\)\s*\{', r'NotificationPageView(this, { closeSidebar() }) {', content)
content = re.sub(r'NotificationPageView\(this\)', r'NotificationPageView(this, { closeSidebar() }, { })', content)

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'w') as f:
    f.write(content)
