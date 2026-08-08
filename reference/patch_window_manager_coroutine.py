import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """            kotlinx.coroutines.Dispatchers.Main.dispatch(context.mainLooper) {
                android.widget.Toast.makeText(context, "Applied ${parsedBlocks.size} blocks to ${project.name}", android.widget.Toast.LENGTH_SHORT).show()
                close()
            }"""

replacement = """            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Applied ${parsedBlocks.size} blocks to ${project.name}", android.widget.Toast.LENGTH_SHORT).show()
                close()
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched Coroutine successfully")
else:
    print("Target Coroutine not found")
