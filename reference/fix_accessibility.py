import re
with open("app/src/main/java/com/example/service/VianSideAccessibilityService.kt", "r") as f:
    content = f.read()

old_action = """        if (action == "auto_scroll") {
            if (autoScrollManager?.isRunning == true) autoScrollManager?.stop() else autoScrollManager?.start()
            return true
        }"""

new_action = """        if (action == "auto_scroll") {
            if (autoScrollManager?.isRunning == true) autoScrollManager?.stop() else autoScrollManager?.start()
            return true
        }
        if (action == "long_screenshot") {
            longScreenshotManager?.start()
            return true
        }"""

content = content.replace(old_action, new_action)

with open("app/src/main/java/com/example/service/VianSideAccessibilityService.kt", "w") as f:
    f.write(content)
