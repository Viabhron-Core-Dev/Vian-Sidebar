import re

# 1. Update WelcomeScreen.kt
with open("app/src/main/java/com/example/WelcomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun WelcomeScreen(onContinue: () -> Unit) {", "fun WelcomeScreen(onContinue: () -> Unit, isFirstLaunch: Boolean = true) {")
content = content.replace('text = "Welcome to LiteReader",', 'text = if (isFirstLaunch) "Welcome to LiteReader" else "Permissions Manager",')
content = content.replace('Text("Continue to Settings", style = MaterialTheme.typography.titleMedium)', 'Text(if (isFirstLaunch) "Continue to Settings" else "Back", style = MaterialTheme.typography.titleMedium)')

with open("app/src/main/java/com/example/WelcomeScreen.kt", "w") as f:
    f.write(content)


# 2. Update SettingsActivity.kt
with open("app/src/main/java/com/example/SettingsActivity.kt", "r") as f:
    content = f.read()

target = """                "screencap" -> ScreenCapSettingsScreen(
                    onBack = { currentRoute = "main" }
                )
            }"""

replacement = """                "screencap" -> ScreenCapSettingsScreen(
                    onBack = { currentRoute = "main" }
                )
                "permissions" -> WelcomeScreen(
                    onContinue = { currentRoute = "main" },
                    isFirstLaunch = false
                )
            }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/SettingsActivity.kt", "w") as f:
    f.write(content)

