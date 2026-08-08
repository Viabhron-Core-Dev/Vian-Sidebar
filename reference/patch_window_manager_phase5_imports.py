import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

imports_to_add = "import androidx.compose.foundation.layout.heightIn\n"

if "import androidx.compose.foundation.layout.heightIn" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Box", imports_to_add + "import androidx.compose.foundation.layout.Box")
    
if "androidx.compose.material3.ButtonDefaults" not in content:
    content = content.replace("import androidx.compose.material3.CardDefaults", "import androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.ButtonDefaults")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)
print("Patched imports again")
