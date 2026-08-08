import re
with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

log_code = 'localIds.addAll(manager.activeItems.map { it.id })\n            com.example.LogKeeper.writeLog("SidebarEdit", "localIds on attach: $localIds")'
content = content.replace('localIds.addAll(manager.activeItems.map { it.id })', log_code)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
