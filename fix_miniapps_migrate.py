import os
import shutil

os.makedirs("app/src/main/java/com/example/feature/miniapps/browser", exist_ok=True)
os.makedirs("app/src/main/java/com/example/feature/miniapps/translation", exist_ok=True)
os.makedirs("app/src/main/java/com/example/feature/miniapps/reader", exist_ok=True)

# Copy Translation
shutil.copy("reference/app/src/main/java/com/example/service/TranslationPopupActivity.kt", "app/src/main/java/com/example/feature/miniapps/translation/")
shutil.copy("reference/app/src/main/java/com/example/service/TranslationManagementActivity.kt", "app/src/main/java/com/example/feature/miniapps/translation/")
shutil.copy("reference/app/src/main/java/com/example/service/TranslationWindowManager.kt", "app/src/main/java/com/example/feature/miniapps/translation/")

# Copy Dictionary
shutil.copy("reference/app/src/main/java/com/example/service/DictionaryPopupActivity.kt", "app/src/main/java/com/example/feature/miniapps/")

# Copy Read Aloud & Reader
shutil.copy("reference/app/src/main/java/com/example/service/ReadAloudActivity.kt", "app/src/main/java/com/example/feature/miniapps/reader/")
shutil.copy("reference/app/src/main/java/com/example/service/FloatingReaderService.kt", "app/src/main/java/com/example/feature/miniapps/reader/")
shutil.copy("reference/app/src/main/java/com/example/service/FloatingReaderAdapters.kt", "app/src/main/java/com/example/feature/miniapps/reader/")
shutil.copy("reference/app/src/main/java/com/example/service/ReaderHandleView.kt", "app/src/main/java/com/example/feature/miniapps/reader/")

# Copy Browser
shutil.copy("reference/app/src/main/java/com/example/BrowserReceiverActivity.kt", "app/src/main/java/com/example/feature/miniapps/browser/")
shutil.copy("reference/app/src/main/java/com/example/service/FloatingBrowserService.kt", "app/src/main/java/com/example/feature/miniapps/browser/")
shutil.copy("reference/app/src/main/java/com/example/service/FloatingBrowserWindowManager.kt", "app/src/main/java/com/example/feature/miniapps/browser/")
