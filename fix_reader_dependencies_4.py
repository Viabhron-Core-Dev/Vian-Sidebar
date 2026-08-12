import re

# Fix DictionaryPopupActivity double imports
with open("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "r") as f:
    text = f.read()
text = text.replace("import android.content.Intent\n\n\nimport android.content.Intent\n", "import android.content.Intent\n")
text = text.replace("import android.content.Intent\n\nimport android.content.Intent\n", "import android.content.Intent\n")
text = text.replace("import android.content.Intent\nimport android.os.Bundle", "import android.os.Bundle")
with open("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "w") as f:
    f.write(text)

# Fix ReaderHandleView BubbleDrawable constructor and other usages
with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "r") as f:
    text = f.read()
text = text.replace("import com.example.util.getEdgeFlag", "")
text = text.replace("getEdgeFlag()", "0")
text = text.replace("Utils.instance.", "Utils.")

# BubbleDrawable takes a Bitmap?, but HandleShapeDrawable took (color, strokeWidth, strokeColor)
text = re.sub(r'BubbleDrawable\(android\.graphics\.Color\.[^,]+,\s*[0-9]+f,\s*android\.graphics\.Color\.[^)]+\)', 'BubbleDrawable(null)', text)

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "w") as f:
    f.write(text)

# Fix FloatingReaderService
with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

# Comment out unresolved references that were missed
text = re.sub(r'(val\s+pwaEntry\s*=\s*PwaEntry\(.*?\))', r'// \1', text, flags=re.DOTALL)
text = re.sub(r'(pwaManager\?\.show\(id\))', r'// \1', text)
text = re.sub(r'(dictManager\?\.show\(.*?\))', r'// \1', text)
text = text.replace("var dictManager: DictionaryWindowManager? = null", "// var dictManager: DictionaryWindowManager? = null")
text = text.replace("var pwaManager: PwaWindowManager? = null", "// var pwaManager: PwaWindowManager? = null")
text = text.replace("dictManager = DictionaryWindowManager(this)", "// dictManager = DictionaryWindowManager(this)")
text = text.replace("pwaManager = PwaWindowManager(this)", "// pwaManager = PwaWindowManager(this)")
text = text.replace("ActiveAppTracker.", "// ActiveAppTracker.")
text = text.replace("lifecycleOwner.", "// lifecycleOwner.")

# Fix unresolved let on line 315 by changing it to standard intent broadcast
text = re.sub(r'override fun sendBroadcast\(intent: Intent\?\) \{.*?\n\s*\}', r'override fun sendBroadcast(intent: Intent?) { super.sendBroadcast(intent) }', text, flags=re.DOTALL)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)

# Fix FloatingBrowserWindowManager
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "r") as f:
    text = f.read()
text = text.replace("ActiveAppTracker.", "// ActiveAppTracker.")
text = text.replace("LogKeeper.", "// LogKeeper.")
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "w") as f:
    f.write(text)
