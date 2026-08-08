with open('app/src/main/java/com/example/service/AppyworkFloatingService.kt', 'r') as f:
    content = f.read()

target = """            windowManager = AppyworkWindowManager(this) {"""
replacement = """            windowManager = AppyworkWindowManager(this, parsedBlocks) {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkFloatingService.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
