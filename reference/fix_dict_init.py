with open("app/src/main/java/com/example/service/SidebarService.kt", "r") as f:
    content = f.read()

content = content.replace(
    """            } else if (action == "dictionary_floating") {
                dictWindowManager?.show(false)
            } else if (action == "dictionary_full") {
                dictWindowManager?.show(true)""",
    """            } else if (action == "dictionary_floating") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(false)
            } else if (action == "dictionary_full") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(true)"""
)

with open("app/src/main/java/com/example/service/SidebarService.kt", "w") as f:
    f.write(content)
