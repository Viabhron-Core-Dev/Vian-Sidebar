import re

# Fix BrowserReceiverActivity
with open("app/src/main/java/com/example/feature/miniapps/browser/BrowserReceiverActivity.kt", "r") as f:
    text = f.read()
text = text.replace("import com.example.feature.miniapps.FloatingBrowserService", "import com.example.feature.miniapps.browser.FloatingBrowserService")
with open("app/src/main/java/com/example/feature/miniapps/browser/BrowserReceiverActivity.kt", "w") as f:
    f.write(text)

