import re

with open('app/src/main/java/com/example/PageManagementSettingsScreen.kt', 'r') as f:
    content = f.read()

click_handler = """                        types.forEach { (type, title) ->
                            TextButton(onClick = {
                                if (type == "widget") {
                                    val intent = android.content.Intent(context, WidgetPickerActivity::class.java).apply {
                                        putExtra("ACTION_TYPE", "CREATE_PAGE")
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    showAddDialog = false
                                } else {
                                    val newPages = pages.toMutableList()
                                    newPages.add(SidebarPage.createDefault(id = UUID.randomUUID().toString(), type = type, title = title))
                                    pages = newPages
                                    savePages()
                                    showAddDialog = false
                                }
                            }, modifier = Modifier.fillMaxWidth()) {"""

content = content.replace("""                        types.forEach { (type, title) ->
                            TextButton(onClick = {
                                val newPages = pages.toMutableList()
                                newPages.add(SidebarPage.createDefault(id = UUID.randomUUID().toString(), type = type, title = title))
                                pages = newPages
                                savePages()
                                showAddDialog = false
                            }, modifier = Modifier.fillMaxWidth()) {""", click_handler)

with open('app/src/main/java/com/example/PageManagementSettingsScreen.kt', 'w') as f:
    f.write(content)

