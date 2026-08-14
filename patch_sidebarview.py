filepath = 'app/src/main/java/com/example/feature/sidebar/SidebarView.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("HybridGridPageView(context, config.id) { newHeight ->", "HybridGridPageView(context, config.id, viewScope) { newHeight ->")
content = content.replace("WidgetsGridPageView(context, config.id) { newHeight ->", "WidgetsGridPageView(context, config.id, viewScope) { newHeight ->")

with open(filepath, 'w') as f:
    f.write(content)
print("SidebarView updated")
