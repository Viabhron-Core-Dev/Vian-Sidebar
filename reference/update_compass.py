import re

with open('app/src/main/java/com/example/service/SidebarView.kt', 'r') as f:
    content = f.read()

content = content.replace('"compass" -> 400', '"compass" -> 500')

with open('app/src/main/java/com/example/service/SidebarView.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/utils/PageManager.kt', 'r') as f:
    content = f.read()

content = content.replace('"compass" -> 400', '"compass" -> 500')

with open('app/src/main/java/com/example/utils/PageManager.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/service/CompassPageView.kt', 'r') as f:
    content = f.read()

content = content.replace('padding(16.dp)', 'padding(4.dp)')

with open('app/src/main/java/com/example/service/CompassPageView.kt', 'w') as f:
    f.write(content)
