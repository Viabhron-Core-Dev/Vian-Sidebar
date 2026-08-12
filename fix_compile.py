import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

# Add missing import
if 'import android.content.Intent' not in content:
    content = content.replace('import android.content.Context', 'import android.content.Context\nimport android.content.Intent')

# Move startingIndex definition BEFORE topbar
# Find where startingIndex is defined:
start_idx_code = """        val isLooping = pageConfigs.size > 2
        val startingIndex = if (isLooping) {
            val half = Int.MAX_VALUE / 2
            half - (half % pageConfigs.size) + max(0, defaultPageIndex)
        } else {
            max(0, defaultPageIndex)
        }"""
        
content = content.replace(start_idx_code, '')
content = content.replace('val topBar = LinearLayout(context).apply {', start_idx_code + '\n\n        val topBar = LinearLayout(context).apply {')

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
