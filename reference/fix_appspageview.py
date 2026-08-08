import re

with open("app/src/main/java/com/example/service/AppsPageView.kt", "r") as f:
    content = f.read()

replacement = """                    } else if (item.action == "dictionary_floating") {
                        val intent = android.content.Intent(context, SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_floating")
                        context.startService(intent)
                    } else if (item.action == "dictionary_full") {
                        val intent = android.content.Intent(context, SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_full")
                        context.startService(intent)
                    } else if (item.action == "ebook_reader") {"""

content = content.replace("                    } else if (item.action == \"ebook_reader\") {", replacement)

with open("app/src/main/java/com/example/service/AppsPageView.kt", "w") as f:
    f.write(content)
