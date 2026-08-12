import re

with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'r') as f:
    text = f.read()

text = text.replace("instance = this", "// instance = this")
text = text.replace("instance = null", "// instance = null")
text = re.sub(r'companion object \{\s*var instance: FloatingReaderService\? = null\s*\}', '', text)

with open('app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt', 'w') as f:
    f.write(text)
