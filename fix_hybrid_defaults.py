import re

with open('app/src/main/java/com/example/utils/PageManager.kt', 'r') as f:
    content = f.read()

setup_old = """        if (!prefs.contains("hybrid_grid_" + defaultPageId)) {
            val jsonStr = "[{\\"id\\": \\"system:ebook_reader\\", \\"cols\\": 1, \\"rows\\": 1, \\"x\\": 0, \\"y\\": 0}, {\\"id\\": \\"system:log_keeper\\", \\"cols\\": 1, \\"rows\\": 1, \\"x\\": 1, \\"y\\": 0}]"
            prefs.edit().putString("hybrid_grid_" + defaultPageId, jsonStr).apply()
        }"""

setup_new = """        if (!prefs.contains("hybrid_grid_" + defaultPageId)) {
            val jsonStr = "[{\\"id\\": \\"system:ebook_reader\\", \\"cols\\": 1, \\"rows\\": 1, \\"x\\": 0, \\"y\\": 0}, {\\"id\\": \\"system:log_keeper\\", \\"cols\\": 1, \\"rows\\": 1, \\"x\\": 1, \\"y\\": 0}]"
            prefs.edit().putString("hybrid_grid_" + defaultPageId, jsonStr).apply()
            prefs.edit().putInt("hybrid_grid_cols_$defaultPageId", 3).apply()
            prefs.edit().putBoolean("handle_${handleId}_sidebar_wrap_content", true).apply()
        }"""

content = content.replace(setup_old, setup_new)

with open('app/src/main/java/com/example/utils/PageManager.kt', 'w') as f:
    f.write(content)
