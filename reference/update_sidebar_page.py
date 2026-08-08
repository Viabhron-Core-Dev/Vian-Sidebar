import re

with open('app/src/main/java/com/example/utils/PageManager.kt', 'r') as f:
    content = f.read()

content = content.replace('"calculator", "compass", "notification", "scheduler", "reader" -> false', '"calculator", "compass", "notification", "scheduler", "reader", "widget" -> false')
content = content.replace('"notification", "scheduler", "reader" -> 500', '"notification", "scheduler", "reader", "widget" -> 500')

with open('app/src/main/java/com/example/utils/PageManager.kt', 'w') as f:
    f.write(content)

