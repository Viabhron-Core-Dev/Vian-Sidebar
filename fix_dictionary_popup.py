import re

with open("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "r") as f:
    text = f.read()

# Fix Intent imports
if "SidebarService::class.java" in text and "import com.example.service.SidebarService" not in text:
    text = text.replace("package com.example.feature.miniapps\n", "package com.example.feature.miniapps\n\nimport com.example.service.SidebarService\nimport android.content.Intent\n")

with open("app/src/main/java/com/example/feature/miniapps/DictionaryPopupActivity.kt", "w") as f:
    f.write(text)

