import re

# AppsPageView.kt
with open("app/src/main/java/com/example/service/AppsPageView.kt", "r") as f:
    content = f.read()
content = content.replace("""                    } else if (item.action == "dictionary_full") {
                        val intent = Intent(context, SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_full")
                        context.startService(intent)""", "")
with open("app/src/main/java/com/example/service/AppsPageView.kt", "w") as f:
    f.write(content)

# SidebarAppsManager.kt
with open("app/src/main/java/com/example/service/SidebarAppsManager.kt", "r") as f:
    content = f.read()
content = content.replace("""    SidebarItem.SystemAction("dictionary_full", "Dictionary (Full Screen)", android.R.drawable.ic_menu_sort_alphabetically),
""", "")
content = content.replace("""    SidebarItem.SystemAction("dictionary_full", "Dictionary (Full Screen)", android.R.drawable.ic_menu_sort_alphabetically)
""", "")
with open("app/src/main/java/com/example/service/SidebarAppsManager.kt", "w") as f:
    f.write(content)
