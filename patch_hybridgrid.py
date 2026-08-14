filepath = 'app/src/main/java/com/example/feature/sidebar/HybridGridPageView.kt'
with open(filepath, 'r') as f:
    content = f.read()

import re

# Update constructor
old_constructor = """class HybridGridPageView(
    context: Context,
    private val pageId: String,
    private val onHeightChanged: (Int) -> Unit
) : FrameLayout(context), SidebarPageControllable {"""
new_constructor = """class HybridGridPageView(
    context: Context,
    private val pageId: String,
    private val scope: CoroutineScope,
    private val onHeightChanged: (Int) -> Unit
) : FrameLayout(context), SidebarPageControllable {"""
content = content.replace(old_constructor, new_constructor)

# Update appsManager initialization
content = content.replace("CoroutineScope(Dispatchers.IO)", "scope")

# Update bindIcon calls
content = content.replace("CoroutineScope(Dispatchers.Main)", "scope")

with open(filepath, 'w') as f:
    f.write(content)
print("HybridGridPageView updated")
