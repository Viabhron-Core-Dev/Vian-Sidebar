filepath = 'app/src/main/java/com/example/feature/sidebar/AppsPageView.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """                    } else if (item.action == "dictionary_full") {
                        val intent = android.content.Intent(context, com.example.service.SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_full")
                        context.startService(intent)
                    } else if (item.action == "ebook_reader") {"""

new_block = """                    } else if (item.action == "dictionary_full") {
                        val intent = android.content.Intent(context, com.example.service.SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_full")
                        context.startService(intent)
                    } else if (item.action == "work_notes") {
                        val intent = android.content.Intent(context, com.example.service.SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:work_notes")
                        context.startService(intent)
                    } else if (item.action == "ebook_reader") {"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("AppsPageView patched")
else:
    print("Could not find the old block in AppsPageView.kt!")
