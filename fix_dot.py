with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    text = f.read()

text = text.replace('context.startActivity(Intent.createChooser(i, "Export Logs").)', 'context.startActivity(Intent.createChooser(i, "Export Logs").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })')

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(text)
