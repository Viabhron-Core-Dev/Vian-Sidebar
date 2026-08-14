filepath = 'app/src/main/java/com/example/feature/settings/IntentPickerActivity.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Remove the inline import which caused the syntax error
content = content.replace("                            import java.net.URLEncoder\n", "")

# Add the import at the top of the file
if "import java.net.URLEncoder" not in content:
    content = content.replace("import android.os.Bundle", "import java.net.URLEncoder\nimport android.os.Bundle")

with open(filepath, 'w') as f:
    f.write(content)
print("IntentPickerActivity patched 2")
