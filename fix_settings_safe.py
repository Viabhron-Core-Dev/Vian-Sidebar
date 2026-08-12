import re

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    text = f.read()

text = text.replace("package com.example", "package com.example.feature.settings")
text = text.replace("import com.example.LogKeeper", "import com.example.core.LogKeeper")
text = text.replace("com.example.LogKeeper", "com.example.core.LogKeeper")
text = text.replace("import com.example.BackupHelper", "")
text = text.replace("isFirstLaunch = true,", "")

# Stub out unmigrated intents
text = text.replace("val intent = Intent(context, com.example.LogKeeperActivity::class.java).apply {", "val intent = Intent().apply {")
text = text.replace("val intent = Intent(context, com.example.PwaManagerActivity::class.java).apply {", "val intent = Intent().apply {")
text = text.replace("val intent = Intent(context, com.example.AppyworkSettingsActivity::class.java).apply {", "val intent = Intent().apply {")
text = text.replace("addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)", "")

# And we shouldn't even startActivity with an empty Intent, let's just comment out `startActivity(intent)` where it's next to these blocks
# Well, if the intent is empty it might crash. Let's just remove the intents entirely.
def remove_intent_block(content, class_name):
    # Find block starting with val intent = Intent(context, class_name)
    pattern = r"val intent = Intent\(context,\s*" + class_name + r"\.class\.java\)\.apply\s*\{[^}]*\}\s*context\.startActivity\(intent\)"
    # The actual string is `com.example.LogKeeperActivity::class.java`
    pattern2 = r"val intent = Intent\(context,\s*" + class_name + r"::class\.java\)\.apply\s*\{[^}]*\}\s*context\.startActivity\(intent\)"
    return re.sub(pattern2, 'android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', content, flags=re.DOTALL)

text = remove_intent_block(text, "com\.example\.LogKeeperActivity")
text = remove_intent_block(text, "com\.example\.PwaManagerActivity")
text = remove_intent_block(text, "com\.example\.AppyworkSettingsActivity")


# Handle BackupHelper runCatching blocks
# The block is usually:
# val result = runCatching { BackupHelper... }
# if (result.isSuccess) ...
# Let's just replace BackupHelper.* with `kotlin.Result.success(Unit)` ? No, runCatching returns Result<T>.
# Let's replace `BackupHelper.[^\(]+\([^\)]*\)` with `kotlin.Unit`
text = re.sub(r'BackupHelper\.[a-zA-Z0-9_]+\([^)]*\)', 'kotlin.Unit', text)
text = re.sub(r'BackupHelper\.[a-zA-Z0-9_]+\([^)]*\,\s*[^)]*\)', 'kotlin.Unit', text)
text = re.sub(r'BackupHelper\.[a-zA-Z0-9_]+\([^\)]+\)', 'kotlin.Unit', text)

# Add missing composables at the end
stubs = """
@Composable fun NetSpeedSettingsScreen(onBack: () -> Unit) {}
@Composable fun CallRecorderSettingsScreen(onBack: () -> Unit) {}
@Composable fun DictionarySettingsScreen(onBack: () -> Unit) {}
@Composable fun WelcomeScreen(onContinue: () -> Unit) {}
@Composable fun BrowserSettingsScreen(onBack: () -> Unit) {}
"""
text += stubs

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(text)
