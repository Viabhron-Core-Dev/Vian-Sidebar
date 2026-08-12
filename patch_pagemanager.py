import re

file_path = 'app/src/main/java/com/example/utils/PageManager.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'val wrap = when(type) { "calculator", "compass", "notification", "scheduler", "app_tracker", "resources_tracker" -> false else -> true }',
    'val wrap = when(type) { "calculator", "compass", "notification", "scheduler", "app_tracker", "resources_tracker", "media_player" -> false else -> true }'
)
content = content.replace(
    'val h = when(type) { "calculator" -> 450; "compass" -> 500; "notification", "scheduler", "resources_tracker", "widget", "widgets_grid", "hybrid_grid" -> 500; "app_tracker" -> 600; else -> 450 }',
    'val h = when(type) { "calculator" -> 450; "compass" -> 500; "notification", "scheduler", "resources_tracker", "media_player", "widget", "widgets_grid", "hybrid_grid" -> 500; "app_tracker" -> 600; else -> 450 }'
)
with open(file_path, 'w') as f:
    f.write(content)
print("PageManager patched")
