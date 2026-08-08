import re

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'r') as f:
    content = f.read()

# Remove specific blocks that belong to SidebarService
content = re.sub(r'    private val triggerHandleViews = mutableListOf<TriggerHandleView>\(\)\n.*    private var widgetPickerReceiver: android\.content\.BroadcastReceiver\? = null', '', content, flags=re.DOTALL)
content = re.sub(r'            "speed_indicator_enabled" -> \{.*?            "call_recorder_enabled" -> \{.*?                \}\n            \}', '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'w') as f:
    f.write(content)
