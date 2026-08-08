import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

imports_to_add = "import androidx.compose.material.icons.filled.Add\n"

if "import androidx.compose.material.icons.filled.Add" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", imports_to_add + "import androidx.compose.ui.Alignment")
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched imports again")
