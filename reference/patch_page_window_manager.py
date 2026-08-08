with open('app/src/main/java/com/example/service/PageWindowManager.kt', 'r') as f:
    content = f.read()

target1 = '"local_terminal" -> "Local Terminal"'
replacement1 = '"local_terminal" -> "Local Terminal"\n            "termux" -> "Termux (PRoot)"'

target2 = '"local_terminal" -> 5'
replacement2 = '"local_terminal" -> 5\n            "termux" -> 25'

target3 = '"local_terminal" -> LocalTerminalPageView(context)'
replacement3 = '"local_terminal" -> LocalTerminalPageView(context)\n            "termux" -> TermuxPageView(context)'

if target1 in content:
    content = content.replace(target1, replacement1)
    print("Patched target1")

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Patched target2")

if target3 in content:
    content = content.replace(target3, replacement3)
    print("Patched target3")

with open('app/src/main/java/com/example/service/PageWindowManager.kt', 'w') as f:
    f.write(content)
