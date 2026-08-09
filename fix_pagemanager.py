import re

with open('app/src/main/java/com/example/utils/PageManager.kt', 'r') as f:
    content = f.read()

# Seed default hybrid grid items when creating the default page
replacement = """
        val defaultPage = SidebarPage(id = defaultPageId, type = "hybrid_grid", title = "Home Grid")
        if (!prefs.contains("hybrid_grid_" + defaultPageId)) {
            val jsonStr = "[{\\"id\\": \\"system:ebook_reader\\", \\"cols\\": 1, \\"rows\\": 1, \\"x\\": 0, \\"y\\": 0}, {\\"id\\": \\"system:log_keeper\\", \\"cols\\": 1, \\"rows\\": 1, \\"x\\": 1, \\"y\\": 0}]"
            prefs.edit().putString("hybrid_grid_" + defaultPageId, jsonStr).apply()
        }
"""

if 'if (!prefs.contains("hybrid_grid_"' not in content:
    content = content.replace(
        'val defaultPage = SidebarPage(id = defaultPageId, type = "hybrid_grid", title = "Home Grid")',
        replacement.strip()
    )

with open('app/src/main/java/com/example/utils/PageManager.kt', 'w') as f:
    f.write(content)
