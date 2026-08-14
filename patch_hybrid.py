import re

filepath = 'app/src/main/java/com/example/feature/sidebar/HybridGridPageView.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_block = """                                } else if (parsed is SidebarItem.SystemAction) {
                                    if (parsed.action == "screen_record") {
                                        val intent = Intent(context, ScreenRecordActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else {
                                        val intent = Intent(context, com.example.service.SidebarService::class.java)
                                        intent.action = "EXECUTE_ACTION"
                                        intent.putExtra("ACTION_ID", "system:" + parsed.action)
                                        context.startService(intent)
                                    }
                                }"""

new_block = """                                } else if (parsed is SidebarItem.SystemAction) {
                                    if (parsed.action == "log_keeper") {
                                        val intent = Intent(context, com.example.LogKeeperActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } else if (parsed.action == "dictionary_floating" || parsed.action == "translation_floating" || parsed.action == "hybrid_grid_floating" || parsed.action == "dictionary_full" || parsed.action == "work_notes") {
                                        val intent = Intent(context, com.example.service.SidebarService::class.java)
                                        intent.action = "EXECUTE_ACTION"
                                        intent.putExtra("ACTION_ID", "system:" + parsed.action)
                                        context.startService(intent)
                                    } else if (parsed.action == "ebook_reader") {
                                        val intent = Intent(context, com.example.feature.miniapps.reader.FloatingReaderService::class.java)
                                        intent.putExtra("UNFOLD", true)
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
                                        val service = com.example.feature.system_hub.VianSideAccessibilityService.instance
                                        if (service != null && service.performAction(parsed.action)) {
                                            com.example.core.LogKeeper.writeLog("HybridGrid", "System action trigger: ${parsed.action}")
                                        } else {
                                            android.widget.Toast.makeText(context, "Please enable VianSide Accessibility Service", android.widget.Toast.LENGTH_SHORT).show()
                                            com.example.core.LogKeeper.writeLog("HybridGrid", "Failed system action trigger: ${parsed.action}")
                                            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            try { context.startActivity(intent) } catch (e: Exception) {}
                                        }
                                    }
                                }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open(filepath, 'w') as f:
        f.write(content)
    print("HybridGridPageView patched")
else:
    print("Could not find the old block in HybridGridPageView.kt!")
