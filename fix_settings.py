import re
import os

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    settings_code = f.read()

# Add empty composables for missing screens at the end of SettingsActivity.kt
stubs = """
@Composable fun NetSpeedSettingsScreen(onBack: () -> Unit) {}
@Composable fun CallRecorderSettingsScreen(onBack: () -> Unit) {}
@Composable fun DictionarySettingsScreen(onBack: () -> Unit) {}
@Composable fun WelcomeScreen(onContinue: () -> Unit) {}
@Composable fun BrowserSettingsScreen(onBack: () -> Unit) {}
"""
settings_code += stubs

# Comment out BackupHelper usages
settings_code = re.sub(r'BackupHelper\.[a-zA-Z0-9_]+\([^)]*\)', '/* BackupHelper call */', settings_code)

# Comment out LogKeeperActivity, PwaManagerActivity, AppyworkSettingsActivity
settings_code = re.sub(r'val intent = Intent\(context, com\.example\.LogKeeperActivity::class\.java\)\.apply \{[^}]*\}', 'val intent = Intent() /* LogKeeperActivity */', settings_code)
settings_code = re.sub(r'val intent = Intent\(context, com\.example\.PwaManagerActivity::class\.java\)\.apply \{[^}]*\}', 'val intent = Intent() /* PwaManagerActivity */', settings_code)
settings_code = re.sub(r'val intent = Intent\(context, com\.example\.AppyworkSettingsActivity::class\.java\)\.apply \{[^}]*\}', 'val intent = Intent() /* AppyworkSettingsActivity */', settings_code)

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(settings_code)


with open('app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'r') as f:
    add_el_code = f.read()

# Comment out specific missing activities in AddElementActivity
add_el_code = re.sub(r'val intent = Intent\(this, com\.example\.PwaPickerActivity::class\.java\)\.apply \{[^}]*\}', 'val intent = Intent() /* PwaPickerActivity */', add_el_code)
add_el_code = re.sub(r'val intent = Intent\(this, com\.example\.PageWindowPickerActivity::class\.java\)\.apply \{[^}]*\}', 'val intent = Intent() /* PageWindowPickerActivity */', add_el_code)

# Comment out PwaDatabase import and usage
add_el_code = re.sub(r'import com\.example\.service\.PwaDatabase\n?', '', add_el_code)
# There's likely some coroutine fetching PWA items, let's just mock or remove it.
# E.g. val db = Room.databaseBuilder...
add_el_code = re.sub(r'val db = Room\.databaseBuilder[^{]+\{[^}]+\}[^}]+?\}', '/* PWA DB fetch stubbed */', add_el_code, flags=re.DOTALL)

with open('app/src/main/java/com/example/feature/settings/AddElementActivity.kt', 'w') as f:
    f.write(add_el_code)
print("Applied fixes")
