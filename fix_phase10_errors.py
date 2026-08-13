import re
import os

# Fix 1: Remove stubs from SettingsActivity
with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    settings_text = f.read()
settings_text = re.sub(r'@androidx\.compose\.runtime\.Composable fun CallRecorderSettingsScreen[^{]*{[^}]*}', '', settings_text)
settings_text = re.sub(r'@androidx\.compose\.runtime\.Composable fun NetSpeedSettingsScreen[^{]*{[^}]*}', '', settings_text)
with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(settings_text)

# Fix 3 & 4: LogKeeper references
for file_path in [
    'app/src/main/java/com/example/feature/system_hub/CallRecorderManager.kt',
    'app/src/main/java/com/example/feature/system_hub/DisplayHandler.kt'
]:
    with open(file_path, 'r') as f:
        text = f.read()
    text = text.replace('com.example.LogKeeper', 'com.example.core.LogKeeper')
    text = text.replace('LogKeeper.', 'com.example.core.LogKeeper.')
    with open(file_path, 'w') as f:
        f.write(text)

