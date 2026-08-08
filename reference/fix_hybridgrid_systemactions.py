import re

with open("app/src/main/java/com/example/service/HybridGridPageView.kt", "r") as f:
    content = f.read()

old_block_1 = """                                } else if (parsed is SidebarItem.SystemAction) {
                                    if (parsed.action == "log_keeper") {
                                        val intent = Intent(context, com.example.LogKeeperActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else if (parsed.action == "ebook_reader") {
                                        val intent = Intent(context, FloatingReaderService::class.java)
                                        intent.putExtra("UNFOLD", true)
                                        context.startService(intent)
                                    } else if (parsed.action == "work_notes") {
                                        val intent = Intent(context, WorkNotesService::class.java)
                                        intent.action = "TOGGLE"
                                        context.startService(intent)
                                    } else if (parsed.action == "screen_record") {
                                        val intent = Intent(context, ScreenRecordActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else if (parsed.action == "settings") {
                                        val intent = Intent(context, com.example.SettingsActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else {
                                        val service = VianSideAccessibilityService.instance
                                        if (service != null && service.performAction(parsed.action)) {
                                            com.example.LogKeeper.writeLog("Sidebar", "System action trigger: ${parsed.action}")
                                        } else {
                                            android.widget.Toast.makeText(context, "Please enable VianSide Accessibility Service", android.widget.Toast.LENGTH_SHORT).show()
                                            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        }
                                    }"""

new_block_1 = """                                } else if (parsed is SidebarItem.SystemAction) {
                                    if (parsed.action == "screen_record") {
                                        val intent = Intent(context, ScreenRecordActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else {
                                        val intent = Intent(context, SidebarService::class.java)
                                        intent.action = "EXECUTE_ACTION"
                                        intent.putExtra("ACTION_ID", "system:" + parsed.action)
                                        context.startService(intent)
                                    }"""

content = content.replace(old_block_1, new_block_1)

old_block_2 = """                    } else if (parsed is SidebarItem.SystemAction) {
                        if (parsed.action == "log_keeper") {
                            val intent = Intent(context, com.example.LogKeeperActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else if (parsed.action == "ebook_reader") {
                            val intent = Intent(context, FloatingReaderService::class.java)
                            intent.putExtra("UNFOLD", true)
                            context.startService(intent)
                        } else if (parsed.action == "work_notes") {
                            val intent = Intent(context, WorkNotesService::class.java)
                            intent.action = "TOGGLE"
                            context.startService(intent)
                        } else if (parsed.action == "screen_record") {
                            val intent = Intent(context, ScreenRecordActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else if (parsed.action == "settings") {
                            val intent = Intent(context, com.example.SettingsActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            val service = VianSideAccessibilityService.instance
                            if (service != null && service.performAction(parsed.action)) {
                                com.example.LogKeeper.writeLog("Sidebar", "System action trigger: ${parsed.action}")
                            } else {
                                android.widget.Toast.makeText(context, "Please enable VianSide Accessibility Service", android.widget.Toast.LENGTH_SHORT).show()
                                val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try { context.startActivity(intent) } catch (e: Exception) {}
                            }
                        }
                        popupWindow?.dismiss()"""

new_block_2 = """                    } else if (parsed is SidebarItem.SystemAction) {
                        if (parsed.action == "screen_record") {
                            val intent = Intent(context, ScreenRecordActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(context, SidebarService::class.java)
                            intent.action = "EXECUTE_ACTION"
                            intent.putExtra("ACTION_ID", "system:" + parsed.action)
                            context.startService(intent)
                        }
                        popupWindow?.dismiss()"""

content = content.replace(old_block_2, new_block_2)

with open("app/src/main/java/com/example/service/HybridGridPageView.kt", "w") as f:
    f.write(content)
