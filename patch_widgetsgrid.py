filepath = 'app/src/main/java/com/example/feature/sidebar/WidgetsGridPageView.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Update constructor
old_constructor = """class WidgetsGridPageView(
    context: Context,
    private val pageId: String,
    private val onHeightChanged: (Int) -> Unit
) : FrameLayout(context), SidebarPageControllable {"""
new_constructor = """class WidgetsGridPageView(
    context: Context,
    private val pageId: String,
    private val scope: CoroutineScope,
    private val onHeightChanged: (Int) -> Unit
) : FrameLayout(context), SidebarPageControllable {"""
content = content.replace(old_constructor, new_constructor)

content = content.replace("CoroutineScope(Dispatchers.IO)", "scope")
content = content.replace("CoroutineScope(Dispatchers.Main)", "scope")

with open(filepath, 'w') as f:
    f.write(content)
print("WidgetsGridPageView updated")
