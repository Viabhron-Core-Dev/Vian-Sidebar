import re

with open("app/src/main/java/com/example/WelcomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun WelcomeScreen(onContinue: () -> Unit, isFirstLaunch: Boolean = true) {", "fun WelcomeScreen(onContinue: () -> Unit) {")
content = content.replace('text = if (isFirstLaunch) "Welcome to LiteReader" else "Permissions Manager",', 'text = "Welcome to LiteReader",')
content = content.replace('Text(if (isFirstLaunch) "Continue to Settings" else "Back", style = MaterialTheme.typography.titleMedium)', 'Text("Continue to Settings", style = MaterialTheme.typography.titleMedium)')

with open("app/src/main/java/com/example/WelcomeScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/SettingsActivity.kt", "r") as f:
    content = f.read()

target = """                "screencap" -> ScreenCapSettingsScreen(
                    onBack = { currentRoute = "main" }
                )
                "permissions" -> WelcomeScreen(
                    onContinue = { currentRoute = "main" },
                    isFirstLaunch = false
                )
            }"""

replacement = """                "screencap" -> ScreenCapSettingsScreen(
                    onBack = { currentRoute = "main" }
                )
            }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/SettingsActivity.kt", "w") as f:
    f.write(content)
