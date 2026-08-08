with open("app/src/main/java/com/example/service/TriggerHandleView.kt", "r") as f:
    content = f.read()

import re

# We need to replace all occurrences of:
# val isRight = prefs.getString("sidebar_position", "right") == "right"
# with:
# val isRight = prefs.getString("${prefix}edge", "right") == "right"

content = content.replace(
    'val isRight = prefs.getString("sidebar_position", "right") == "right"',
    'val isRight = prefs.getString("${prefix}edge", "right") == "right"'
)

# And also replace setting in FloatingReaderService
with open("app/src/main/java/com/example/service/TriggerHandleView.kt", "w") as f:
    f.write(content)
