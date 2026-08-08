import re

with open("app/src/main/java/com/example/service/AppsPageView.kt", "r") as f:
    content = f.read()

# Replace the whole SystemAction block
old_block = """                } else if (item is SidebarItem.SystemAction) {
                    if (item.action == "log_keeper") {
                        val intent = android.content.Intent(context, com.example.LogKeeperActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else if (item.action == "dictionary_floating") {
                        val intent = android.content.Intent(context, SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_floating")
                        context.startService(intent)
                    } else if (item.action == "dictionary_full") {
                        val intent = android.content.Intent(context, SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:dictionary_full")
                        context.startService(intent)
                    } else if (item.action == "ebook_reader") {
                        val intent = android.content.Intent(context, FloatingReaderService::class.java)
                        intent.putExtra("UNFOLD", true)
                        context.startService(intent)
                    } else if (item.action == "screen_record") {
                        val intent = android.content.Intent(context, ScreenRecordActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else if (item.action == "settings") {
                        val intent = android.content.Intent(context, com.example.SettingsActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        val service = VianSideAccessibilityService.instance
                        if (service != null && service.performAction(item.action)) {
                            // success
                            com.example.LogKeeper.writeLog("Sidebar", "System action trigger: ${item.action}")
                        } else {
                            android.widget.Toast.makeText(context, "Please enable VianSide Accessibility Service", android.widget.Toast.LENGTH_SHORT).show()
                            com.example.LogKeeper.writeLog("Sidebar", "Failed system action trigger: ${item.action}")
                            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    currentFolderPopup?.dismiss()
                    onCloseSidebar()"""

new_block = """                } else if (item is SidebarItem.SystemAction) {
                    if (item.action == "screen_record") {
                        val intent = android.content.Intent(context, ScreenRecordActivity::class.java)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        val intent = android.content.Intent(context, SidebarService::class.java)
                        intent.action = "EXECUTE_ACTION"
                        intent.putExtra("ACTION_ID", "system:" + item.action)
                        context.startService(intent)
                    }
                    currentFolderPopup?.dismiss()
                    onCloseSidebar()"""

content = content.replace(old_block, new_block)

with open("app/src/main/java/com/example/service/AppsPageView.kt", "w") as f:
    f.write(content)

