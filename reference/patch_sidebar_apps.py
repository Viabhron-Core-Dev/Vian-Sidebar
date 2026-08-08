import re

with open("app/src/main/java/com/example/service/SidebarAppsManager.kt", "r") as f:
    content = f.read()

old_list = """val ALL_SCREEN_CAPTURE_ACTIONS = listOf(
    SidebarItem.SystemAction("screenshot", "Screenshot", android.R.drawable.ic_menu_camera),
    SidebarItem.SystemAction("screen_record", "Screen Record", android.R.drawable.ic_media_play),
    SidebarItem.SystemAction("qr_scan", "Screen Crop / QR", android.R.drawable.ic_menu_search)
)"""

new_list = """val ALL_SCREEN_CAPTURE_ACTIONS = listOf(
    SidebarItem.SystemAction("screenshot", "Screenshot", android.R.drawable.ic_menu_camera),
    SidebarItem.SystemAction("long_screenshot", "Long Screenshot", android.R.drawable.ic_menu_crop),
    SidebarItem.SystemAction("screen_record", "Screen Record", android.R.drawable.ic_media_play),
    SidebarItem.SystemAction("qr_scan", "Screen Crop / QR", android.R.drawable.ic_menu_search)
)"""

content = content.replace(old_list, new_list)

with open("app/src/main/java/com/example/service/SidebarAppsManager.kt", "w") as f:
    f.write(content)

