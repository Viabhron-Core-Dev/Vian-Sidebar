import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                        items(currentBlocks) { b ->
                            Text(b.filePath ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                        }"""

replacement = """                        items(currentBlocks) { b ->
                            val isNew = files.none { it.path == b.filePath }
                            Text("${if (isNew) "🟢 NEW" else "🔵 MODIFIED"} - ${b.filePath ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched Diff screen")
else:
    print("Target Diff screen not found")
