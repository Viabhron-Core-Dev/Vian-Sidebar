import re

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'r') as f:
    content = f.read()

old_sig = "fun showFolderStyleDialog(context: Context, item: SidebarItem.Folder, manager: SidebarAppsManager, onStyleSelected: ((Int) -> Unit)? = null) {"
new_sig = "fun showFolderStyleDialog(context: Context, item: SidebarItem.Folder, manager: SidebarAppsManager, onStyleSelected: ((Int, Int) -> Unit)? = null) {"
content = content.replace(old_sig, new_sig)

with open('app/src/main/java/com/example/service/FolderStyleDialog.kt', 'w') as f:
    f.write(content)
