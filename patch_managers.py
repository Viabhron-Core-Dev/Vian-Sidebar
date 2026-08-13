import re

for filename in ['LongScreenshotManager.kt', 'AutoScrollManager.kt', 'CursorManager.kt']:
    path = f'app/src/main/java/com/example/feature/system_hub/{filename}'
    with open(path, 'r') as f:
        text = f.read()
    
    text = text.replace('private fun stop() {', 'fun stop() {')
    text = text.replace('private fun removeFloatingControls() {', 'fun removeFloatingControls() {')
    text = text.replace('fun destroy() {', 'fun stop() {')
    
    with open(path, 'w') as f:
        f.write(text)

