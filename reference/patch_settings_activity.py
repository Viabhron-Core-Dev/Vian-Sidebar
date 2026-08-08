import re

with open('app/src/main/java/com/example/SettingsActivity.kt', 'r') as f:
    content = f.read()

# Add to MainSettingsScreen parameters
content = content.replace(
    'onNavigateToPermissions: () -> Unit',
    'onNavigateToPermissions: () -> Unit,\n    onNavigateToBrowser: () -> Unit'
)

# Add route in SettingsApp
content = content.replace(
    'onNavigateToPermissions = { currentRoute = "permissions" },',
    'onNavigateToPermissions = { currentRoute = "permissions" },\n                    onNavigateToBrowser = { currentRoute = "browser" },'
)

content = content.replace(
    '                "permissions" -> WelcomeScreen(\n                    onContinue = { currentRoute = "main" },\n                    isFirstLaunch = false\n                )',
    '                "permissions" -> WelcomeScreen(\n                    onContinue = { currentRoute = "main" },\n                    isFirstLaunch = false\n                )\n                "browser" -> BrowserSettingsScreen(\n                    onBack = { currentRoute = "main" }\n                )'
)

# Add ListItem in MainSettingsScreen
list_item_code = """
                Divider()
                ListItem(
                    headlineContent = { Text("Browser Settings") },
                    supportingContent = { Text("Global settings for Floating Browser") },
                    modifier = Modifier.clickable { onNavigateToBrowser() }
                )"""

content = content.replace('                Divider()\n                ListItem(\n                    headlineContent = { Text("Log Keeper") },', list_item_code + '\n                Divider()\n                ListItem(\n                    headlineContent = { Text("Log Keeper") },')

with open('app/src/main/java/com/example/SettingsActivity.kt', 'w') as f:
    f.write(content)
