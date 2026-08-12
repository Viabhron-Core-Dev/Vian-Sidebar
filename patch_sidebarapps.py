import re

file_path = 'app/src/main/java/com/example/feature/sidebar/SidebarAppsManager.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    '"resources_tracker" -> "Resources Tracker"',
    '"resources_tracker" -> "Resources Tracker"\n                "media_player" -> "Media Player"\n                "widget" -> "Android Widget"'
)
with open(file_path, 'w') as f:
    f.write(content)
print("SidebarAppsManager patched")
