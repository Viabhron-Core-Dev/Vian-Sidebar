import re

# Fix CallRecorderManager (it might have `com.example.com.example.core.LogKeeper`)
with open('app/src/main/java/com/example/feature/system_hub/CallRecorderManager.kt', 'r') as f:
    crm_text = f.read()
crm_text = crm_text.replace('com.example.com.example.core.LogKeeper', 'com.example.core.LogKeeper')
with open('app/src/main/java/com/example/feature/system_hub/CallRecorderManager.kt', 'w') as f:
    f.write(crm_text)

# Fix LongScreenshotManager (comment out SidebarService and FloatingTriggerService for now)
with open('app/src/main/java/com/example/feature/system_hub/LongScreenshotManager.kt', 'r') as f:
    lsm_text = f.read()
lsm_text = re.sub(r'(FloatingTriggerService.*)', r'// \1', lsm_text)
lsm_text = re.sub(r'(SidebarService.*)', r'// \1', lsm_text)
with open('app/src/main/java/com/example/feature/system_hub/LongScreenshotManager.kt', 'w') as f:
    f.write(lsm_text)

# Fix QRCropActivity
with open('app/src/main/java/com/example/feature/system_hub/QRCropActivity.kt', 'r') as f:
    qrc_text = f.read()
qrc_text = qrc_text.replace('com.example.LogKeeper', 'com.example.core.LogKeeper')
qrc_text = qrc_text.replace('LogKeeper.', 'com.example.core.LogKeeper.')
with open('app/src/main/java/com/example/feature/system_hub/QRCropActivity.kt', 'w') as f:
    f.write(qrc_text)

# Fix RecordingsActivity
with open('app/src/main/java/com/example/feature/system_hub/RecordingsActivity.kt', 'r') as f:
    ra_text = f.read()
ra_text = ra_text.replace('PinUtils', 'com.example.core.PinUtils')
with open('app/src/main/java/com/example/feature/system_hub/RecordingsActivity.kt', 'w') as f:
    f.write(ra_text)
