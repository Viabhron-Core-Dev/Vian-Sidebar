import re

with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "r") as f:
    text = f.read()
text = text.replace("import com.example.com.example.core.LogKeeper", "import com.example.core.LogKeeper")
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()
text = re.sub(r'serviceLifecycleOwner\?\.let \{.*?\n\s*\}', '', text, flags=re.DOTALL)
with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "r") as f:
    text = f.read()
text = text.replace("//         val gravity = getEdgeFlag(edgeStr)", "val gravity = Gravity.END")
text = text.replace("layoutParams?.gravity = gravity or Gravity.TOP", "layoutParams?.gravity = gravity or Gravity.TOP")
text = text.replace("com.example.service.FloatingReaderService", "com.example.feature.miniapps.reader.FloatingReaderService")
with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "w") as f:
    f.write(text)
