import re

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "r") as f:
    text = f.read()

if "import com.example.utils.HandleShapeDrawable" not in text:
    text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.utils.HandleShapeDrawable\nimport com.example.utils.Utils\n")

text = text.replace("Utils.instance", "Utils")

with open("app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt", "w") as f:
    f.write(text)

