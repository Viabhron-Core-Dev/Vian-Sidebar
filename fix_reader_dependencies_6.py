import re
import glob

# 1. FloatingBrowserWindowManager (LogKeeper)
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "r") as f:
    text = f.read()
text = re.sub(r'LogKeeper\.writeLog\(', 'com.example.core.LogKeeper.writeLog(', text)
text = text.replace("com.example.core.com.example.core.", "com.example.core.")
with open("app/src/main/java/com/example/feature/miniapps/browser/FloatingBrowserWindowManager.kt", "w") as f:
    f.write(text)

# 2. FloatingReaderService (ServiceLifecycleOwner, PwaManager, DictionaryManager, let)
with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

# Make sure LogKeeper is fully qualified everywhere just in case
text = text.replace("LogKeeper.writeLog(", "com.example.core.LogKeeper.writeLog(")
text = text.replace("com.example.core.com.example.core.", "com.example.core.")

# Just comment out all lifecycleOwner lines since we don't have it imported
text = re.sub(r'(lifecycleOwner\.[a-zA-Z]+\(\))', r'// \1', text)
text = re.sub(r'private val lifecycleOwner = ServiceLifecycleOwner\(\)', r'// private val lifecycleOwner = ServiceLifecycleOwner()', text)

# Pwa/Dict Managers
text = text.replace("var dictManager: DictionaryWindowManager? = null", "// var dictManager = null")
text = text.replace("var pwaManager: PwaWindowManager? = null", "// var pwaManager = null")
text = text.replace("dictManager = DictionaryWindowManager(this)", "// dictManager = null")
text = text.replace("pwaManager = PwaWindowManager(this)", "// pwaManager = null")
text = re.sub(r'val pwaEntry = PwaEntry.*?\}', '// PwaEntry', text, flags=re.DOTALL)
text = re.sub(r'pwaManager\?\.show\(id\)', '// show', text)
text = re.sub(r'dictManager\?\.show\(.*?\)', '// show', text)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)

# 3. ReaderHandleView (getEdgeFlag, BubbleDrawable)
with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "r") as f:
    text = f.read()

text = text.replace("getEdgeFlag()", "0")
text = re.sub(r'BubbleDrawable\(.*?\)', 'BubbleDrawable()', text)
text = text.replace("Utils.instance.", "Utils.")

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "w") as f:
    f.write(text)
