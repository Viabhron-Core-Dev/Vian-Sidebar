import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

imports_to_add = """
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CardDefaults
"""

if "import androidx.compose.foundation.layout.Box" not in content:
    content = content.replace("import androidx.compose.material3.Text", "import androidx.compose.material3.Text" + imports_to_add)

content = content.replace("androidx.compose.material.icons.Icons.Default.List", "androidx.compose.material.icons.Icons.AutoMirrored.Filled.List")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)
print("Patched imports again")
