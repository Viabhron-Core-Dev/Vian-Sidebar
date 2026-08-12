import re

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

# I am going to nuke the unresolved references manually by matching the exact line
lines = text.split("\n")
new_lines = []
for i, line in enumerate(lines):
    if "ServiceLifecycleOwner" in line:
        line = "// " + line
    elif "LogKeeper" in line and "com.example.core.LogKeeper" not in line:
        line = line.replace("LogKeeper", "com.example.core.LogKeeper")
    elif "ActiveAppTracker" in line:
        line = "// " + line
    elif "DictionaryWindowManager" in line:
        line = "// " + line
    elif "PwaWindowManager" in line:
        line = "// " + line
    elif "PwaEntry" in line:
        line = "// " + line
    elif "id" in line and i in [237, 239]:
        line = "// " + line
    elif "show" in line and i in [252]:
        line = "// " + line
    elif "let" in line and i in [315]:
        line = "// " + line
    elif "override fun sendBroadcast(intent: Intent?) {" in line:
         pass

    if i == 237 or i == 239 or i == 252 or i == 315:
        line = "// " + line # Hardcode comment those lines

    new_lines.append(line)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write("\n".join(new_lines))

with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "r") as f:
    text = f.read()
lines = text.split("\n")
new_lines = []
for line in lines:
    if "LogKeeper" in line and "com.example.core.LogKeeper" not in line:
        line = line.replace("LogKeeper", "com.example.core.LogKeeper")
    elif "ActiveAppTracker" in line:
        line = "// " + line
    new_lines.append(line)
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "w") as f:
    f.write("\n".join(new_lines))

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "r") as f:
    text = f.read()
lines = text.split("\n")
new_lines = []
for i, line in enumerate(lines):
    if "getEdgeFlag" in line:
        line = "// " + line
    elif "BubbleDrawable()" in line:
        line = line.replace("BubbleDrawable()", "BubbleDrawable(null)")
    elif "Utils.instance." in line:
        line = line.replace("Utils.instance.", "Utils.")
    new_lines.append(line)
with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "w") as f:
    f.write("\n".join(new_lines))

