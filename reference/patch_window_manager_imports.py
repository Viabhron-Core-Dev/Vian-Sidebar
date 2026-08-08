import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = "import androidx.compose.runtime.getValue"
replacement = "import androidx.compose.runtime.getValue\nimport androidx.compose.runtime.setValue"

if target in content and "import androidx.compose.runtime.setValue" not in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched imports successfully")
else:
    print("Target imports not found or already patched")
