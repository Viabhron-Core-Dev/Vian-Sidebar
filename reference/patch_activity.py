import re

with open('app/src/main/java/com/example/AppyworkSettingsActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.example.ui.theme.ThemeManager", "import androidx.compose.material3.MaterialTheme")
content = content.replace("ThemeManager.CurrentTheme", "MaterialTheme")

with open('app/src/main/java/com/example/AppyworkSettingsActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/AppyworkSettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("@Composable\nfun AppyworkProjectDialog(", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun AppyworkProjectDialog(")

with open('app/src/main/java/com/example/AppyworkSettingsScreen.kt', 'w') as f:
    f.write(content)
