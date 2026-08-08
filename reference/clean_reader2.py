import re

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'r') as f:
    content = f.read()

content = re.sub(r'    private var wasSidebarEditOpen.*    private var upSpeed: Long = 0', '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'w') as f:
    f.write(content)
