import re

with open("app/src/main/java/com/example/service/SidebarService.kt", "r") as f:
    content = f.read()

old_start_cmd = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "EXECUTE_ACTION") {"""

new_start_cmd = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "OPEN_DICTIONARY") {
            val query = intent.getStringExtra("QUERY")
            if (dictWindowManager == null) {
                dictWindowManager = DictionaryWindowManager(this)
            }
            if (query != null) {
                dictWindowManager?.searchWord(query)
            } else {
                dictWindowManager?.show(false)
            }
            return START_NOT_STICKY
        }
        if (intent?.action == "EXECUTE_ACTION") {"""

content = content.replace(old_start_cmd, new_start_cmd)

old_dict_floating = """            } else if (action == "dictionary_floating") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(false)
            } else if (action == "dictionary_full") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(true)
            } else if (action == "ebook_reader") {"""

new_dict_floating = """            } else if (action == "dictionary_floating") {
                if (dictWindowManager == null) {
                    dictWindowManager = DictionaryWindowManager(this@SidebarService)
                }
                dictWindowManager?.show(false)
            } else if (action == "ebook_reader") {"""

content = content.replace(old_dict_floating, new_dict_floating)

with open("app/src/main/java/com/example/service/SidebarService.kt", "w") as f:
    f.write(content)
