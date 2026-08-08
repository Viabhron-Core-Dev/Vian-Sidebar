import re

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'r') as f:
    content = f.read()

target = """                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))"""
replacement = """"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched onClose")
else:
    print("onClose target not found")

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'w') as f:
    f.write(content)
