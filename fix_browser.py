import re

with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "r") as f:
    text = f.read()

text = text.replace("com.example.utils.// ActiveAppTracker.", "// com.example.utils.ActiveAppTracker.")
text = text.replace("com.example.core.// LogKeeper.", "// com.example.core.LogKeeper.")

with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "w") as f:
    f.write(text)

