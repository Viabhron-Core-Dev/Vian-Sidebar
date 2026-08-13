import re

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    text = f.read()

old_pages_route = """                "pages" -> SidebarSettingsScreen(
                    handleId = "sidebar",
                    onBack = { currentRoute = "main" }
                )"""

new_pages_route = """                "pages" -> PageManagementSettingsScreen(
                    onBack = { currentRoute = "main" }
                )"""

text = text.replace(old_pages_route, new_pages_route)

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(text)
