import re

with open("app/src/main/java/com/example/HandlesListSettingsScreen.kt", "r") as f:
    content = f.read()

# 1. Update FloatingActionButton to assign toggle_sidebar to new handles
old_fab = """                val newId = UUID.randomUUID().toString()
                handles = handles + HandleConfig(id = newId, name = "Handle ${handles.size + 1}", enabled = true)"""
new_fab = """                val newId = UUID.randomUUID().toString()
                prefs.edit().putString("handle_${newId}_tap", "toggle_sidebar").apply()
                handles = handles + HandleConfig(id = newId, name = "Handle ${handles.size + 1}", enabled = true)"""
content = content.replace(old_fab, new_fab)

# 2. Update Button text "ADD SIDEBAR ELEMENT" -> "ADD GESTURE"
content = content.replace('Text("ADD SIDEBAR ELEMENT")', 'Text("ADD GESTURE")')

# 3. Update the gesture list mapping to wrap in a Card
old_row = """                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                    if (action == "toggle_sidebar") {
                                        onNavigateToSidebarSettings()
                                    }
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {"""

new_row = """                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = {
                                    if (action == "toggle_sidebar") {
                                        onNavigateToSidebarSettings()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {"""

content = content.replace(old_row, new_row)
content = content.replace('Text(actionName)\n                                }\n                            }', 'Text(actionName)\n                                }\n                            }\n                            }') # close the Card


# 4. Update the pageOptions in the add gesture dialog
old_pages = """                                        if (pageConfigs.isEmpty()) {
                                            Text("No pages available.", color = Color.Red)
                                        } else {
                                            val pageOptions = pageConfigs.map { it.type to it.title }
                                            ActionDropdown("Select Page", selectedPageType, pageOptions) { selectedPageType = it }
                                        }"""

new_pages = """                                        val pageOptions = listOf(
                                            "apps" to "Apps Grid",
                                            "widgets_grid" to "Widgets Grid",
                                            "calculator" to "Calculator",
                                            "scheduler" to "Scheduler",
                                            "compass" to "Compass",
                                            "notifications" to "Notifications"
                                        )
                                        if (selectedPageType.isEmpty()) selectedPageType = "apps"
                                        ActionDropdown("Select Page", selectedPageType, pageOptions) { selectedPageType = it }"""

content = content.replace(old_pages, new_pages)

with open("app/src/main/java/com/example/HandlesListSettingsScreen.kt", "w") as f:
    f.write(content)
