import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """            val filteredFiles = files.filter { it.path.contains(searchQuery, ignoreCase = true) }.sortedBy { it.path }"""

replacement = """            val filteredFiles = files.filter { it.path.contains(searchQuery, ignoreCase = true) && it.syncState != "DELETED" }.sortedBy { it.path }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched filtered files")
else:
    print("Target filtered files not found")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)

