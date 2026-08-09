import re

with open('app/src/main/java/com/example/feature/settings/HandlesListSettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace intent calls with commented out intent calls, but carefully.
content = content.replace("val intent = android.content.Intent(context, com.example.AddElementActivity::class.java).apply {", "// val intent")
content = content.replace("action = \"SELECT_ELEMENT_FOR_HANDLE\"", "//")
content = content.replace("putExtra(\"handle_prefix\", prefix)", "//")
content = content.replace("putExtra(\"gesture\", gestureToChange)", "//")
content = content.replace("putExtra(\"gesture\", selectedGesture)", "//")
content = content.replace("addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)", "//")
content = content.replace("context.startActivity(intent)", "//")

with open('app/src/main/java/com/example/feature/settings/HandlesListSettingsScreen.kt', 'w') as f:
    f.write(content)
