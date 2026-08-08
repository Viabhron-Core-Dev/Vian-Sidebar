with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'btnSpeakDef\.setOnClickListener \{.*?\}', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/service/DictionaryWindowManager.kt", "w") as f:
    f.write(content)
