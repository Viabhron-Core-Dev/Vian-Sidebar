import re

with open('app/src/main/java/com/example/core/HandleManager.kt', 'r') as f:
    content = f.read()

# Fix 1: When jsonStr is null, add swipe_left
if 'prefs.edit().putString("handle_sidebar_swipe_left", "open_page:default_hybrid").apply()' not in content:
    content = content.replace(
        'prefs.edit().putString("handle_sidebar_tap", "toggle_sidebar").apply()',
        'prefs.edit().putString("handle_sidebar_tap", "toggle_sidebar").apply()\n            prefs.edit().putString("handle_sidebar_swipe_left", "open_page:default_hybrid").apply()'
    )

# Fix 2: When adding handles in loop, add swipe_left
if 'prefs.edit().putString("handle_${id}_swipe_left"' not in content:
    content = content.replace(
        'if (!prefs.contains("handle_${id}_tap")) {\n                    prefs.edit().putString("handle_${id}_tap", "toggle_sidebar").apply()\n                }',
        'if (!prefs.contains("handle_${id}_tap")) {\n                    prefs.edit().putString("handle_${id}_tap", "toggle_sidebar").apply()\n                }\n                if (!prefs.contains("handle_${id}_swipe_left")) {\n                    val defaultPageId = if (id == "sidebar") "default_hybrid" else "default_hybrid_$id"\n                    prefs.edit().putString("handle_${id}_swipe_left", "open_page:$defaultPageId").apply()\n                }'
    )

with open('app/src/main/java/com/example/core/HandleManager.kt', 'w') as f:
    f.write(content)
