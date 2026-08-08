with open('app/src/main/java/com/example/PageWindowPickerActivity.kt', 'r') as f:
    content = f.read()

target = """"Local Terminal" to "local_terminal","""
replacement = """"Local Terminal" to "local_terminal",
            "Termux (PRoot)" to "termux","""

if target in content:
    content = content.replace(target, replacement)
    print("Patched PageWindowPickerActivity")
else:
    print("target not found")

with open('app/src/main/java/com/example/PageWindowPickerActivity.kt', 'w') as f:
    f.write(content)
