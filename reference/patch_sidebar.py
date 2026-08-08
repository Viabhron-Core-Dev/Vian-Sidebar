import re

with open('app/src/main/java/com/example/service/SidebarService.kt', 'r') as f:
    content = f.read()

content = content.replace("""            } else if (action == "translation_floating") {
                if (translationWindowManager == null) {
                    translationWindowManager = TranslationWindowManager(this@SidebarService)
                }
                translationWindowManager?.show()""",
"""            } else if (action == "translation_floating") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(false, true)""")

with open('app/src/main/java/com/example/service/SidebarService.kt', 'w') as f:
    f.write(content)
