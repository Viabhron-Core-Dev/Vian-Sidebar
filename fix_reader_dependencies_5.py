import re

# Fix DictionaryPopupActivity double imports properly
with open("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "r") as f:
    text = f.read()
if "import android.content.Intent" not in text:
    text = text.replace("import android.os.Bundle", "import android.content.Intent\nimport android.os.Bundle")
with open("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "w") as f:
    f.write(text)

# Fix FloatingBrowserWindowManager unused imports and unresolved references
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "r") as f:
    text = f.read()
text = re.sub(r'import com.example.service.ActiveAppTracker', '', text)
text = re.sub(r'import com.example.core.LogKeeper', '', text)
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "w") as f:
    f.write(text)

# Fix FloatingReaderService
with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()
text = re.sub(r'import com.example.service.ServiceLifecycleOwner', '', text)
text = re.sub(r'import com.example.service.ActiveAppTracker', '', text)
with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)

# Fix ReaderHandleView BubbleDrawable
with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "r") as f:
    text = f.read()
text = text.replace("BubbleDrawable(null)", "BubbleDrawable()")
text = text.replace("Utils.instance.", "Utils.")

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "w") as f:
    f.write(text)
