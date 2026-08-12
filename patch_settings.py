import re

files_to_patch = [
    'app/src/main/java/com/example/feature/settings/SidebarSettingsScreen.kt',
    'app/src/main/java/com/example/feature/settings/HandlesListSettingsScreen.kt'
]

for file_path in files_to_patch:
    with open(file_path, 'r') as f:
        content = f.read()

    # Replace display names
    content = content.replace(
        '"resources_tracker" -> "Resources Tracker"',
        '"resources_tracker" -> "Resources Tracker"\n                    "media_player" -> "Media Player"\n                    "widget" -> "Android Widget"'
    )
    content = content.replace(
        '"resources_tracker" to "Resources Tracker",',
        '"resources_tracker" to "Resources Tracker",\n                            "media_player" to "Media Player",\n                            "widget" to "Android Widget",'
    )

    with open(file_path, 'w') as f:
        f.write(content)
        
print("Settings patched")
