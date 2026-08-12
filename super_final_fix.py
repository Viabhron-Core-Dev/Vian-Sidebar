import re

with open('reference/app/src/main/java/com/example/SettingsActivity.kt', 'r') as f:
    text = f.read()

text = text.replace("package com.example", "package com.example.feature.settings")
text = text.replace("import com.example.LogKeeper", "import com.example.core.LogKeeper")
text = text.replace("com.example.LogKeeper", "com.example.core.LogKeeper")
text = text.replace("import com.example.utils.BackupHelper", "")

# Remove `isFirstLaunch = true,`
text = text.replace("isFirstLaunch = true,", "")

# Replace the intent blocks for the three unmigrated activities:
def remove_intent_block(content, class_name):
    # Match: val intent = Intent(context, class_name) \n ... \n context.startActivity(intent)
    pattern = r"val intent = Intent\(context,\s*" + class_name + r"::class\.java\)\.apply\s*\{[^\}]*\}\s*context\.startActivity\(intent\)"
    return re.sub(pattern, 'android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', content, flags=re.DOTALL)

text = remove_intent_block(text, "com\.example\.LogKeeperActivity")
text = remove_intent_block(text, "com\.example\.PwaManagerActivity")
text = remove_intent_block(text, "com\.example\.AppyworkSettingsActivity")

# Replace BackupHelper logic entirely
# Find blocks like:
# val result = runCatching { ... BackupHelper ... }
# if (result.isSuccess) { ... } else { ... }
# We can just use a regex to strip `BackupHelper` method calls and assume we just Toast "Not Migrated"
pattern_backup = r"val res(ult)? = runCatching\s*\{\s*BackupHelper\.[^\}]*\}\s*if\s*\(res(ult)?\.isSuccess\)\s*\{[^\}]*\}\s*else\s*\{[^\}]*\}"
text = re.sub(pattern_backup, 'android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', text, flags=re.DOTALL)

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
