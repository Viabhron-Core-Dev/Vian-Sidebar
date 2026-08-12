import re

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    text = f.read()

# Replace any occurrence of the LogKeeperActivity intent block
p1 = r"val intent = Intent\(context,\s*com\.example[^:]*LogKeeperActivity::class\.java\)\.apply\s*\{[^\}]*\}\s*(//\s*)?context\.startActivity\(intent\)"
text = re.sub(p1, 'android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', text, flags=re.DOTALL)

p2 = r"val intent = Intent\(context,\s*com\.example[^:]*PwaManagerActivity::class\.java\)\.apply\s*\{[^\}]*\}\s*(//\s*)?context\.startActivity\(intent\)"
text = re.sub(p2, 'android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', text, flags=re.DOTALL)

p3 = r"val intent = Intent\(context,\s*com\.example[^:]*AppyworkSettingsActivity::class\.java\)\.apply\s*\{[^\}]*\}\s*(//\s*)?context\.startActivity\(intent\)"
text = re.sub(p3, 'android.widget.Toast.makeText(context, "Not Migrated", android.widget.Toast.LENGTH_SHORT).show()', text, flags=re.DOTALL)

# But wait, earlier I replaced `context.startActivity(intent)` with `Toast...`!
# Let me just replace the `val intent = Intent(..., ...Activity::class.java).apply { ... }` part.
p4 = r"val intent = Intent\(context,\s*com\.example[^:]*(LogKeeper|PwaManager|AppyworkSettings)Activity::class\.java\)\.apply\s*\{[^\}]*\}"
text = re.sub(p4, 'val intent = Intent()', text)

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(text)
