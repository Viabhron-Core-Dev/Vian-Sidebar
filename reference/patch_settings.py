import re

with open('app/src/main/java/com/example/SettingsActivity.kt', 'r') as f:
    content = f.read()

target = """                ListItem(
                    headlineContent = { Text("PWA Loader") },
                    supportingContent = { Text("Import and manage PWAs") },
                    modifier = Modifier.clickable { 
                        val intent = Intent(context, com.example.PwaManagerActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
                Divider()"""

replacement = target + """
                ListItem(
                    headlineContent = { Text("Appywork Vibe Coding") },
                    supportingContent = { Text("Manage AI coding projects and GitHub auth") },
                    modifier = Modifier.clickable { 
                        val intent = Intent(context, com.example.AppyworkSettingsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
                Divider()"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/SettingsActivity.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
