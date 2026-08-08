import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

old_remove = """                                "Remove" -> {
                                    localIds.removeAt(holder.adapterPosition)
                                    refresh()
                                }"""
new_remove = """                                "Remove" -> {
                                    com.example.LogKeeper.writeLog("SidebarEdit", "Removed item: ${item.label}")
                                    localIds.removeAt(holder.adapterPosition)
                                    refresh()
                                }"""
content = content.replace(old_remove, new_remove)

old_folder_style = """                                "Folder Style" -> {
                                    if (item is SidebarItem.Folder) {
                                        showFolderStyleDialog(context, item, manager) { styleIndex, popupCols ->"""
new_folder_style = """                                "Folder Style" -> {
                                    com.example.LogKeeper.writeLog("SidebarEdit", "Editing folder style for ${item.label}")
                                    if (item is SidebarItem.Folder) {
                                        showFolderStyleDialog(context, item, manager) { styleIndex, popupCols ->"""
content = content.replace(old_folder_style, new_folder_style)

old_grid_size = """                                "Grid Size" -> {
                                    if (item is SidebarItem.Folder) {
                                        showFolderGridDialog(context, item, manager) { styleIndex, popupCols ->"""
new_grid_size = """                                "Grid Size" -> {
                                    com.example.LogKeeper.writeLog("SidebarEdit", "Editing grid size for ${item.label}")
                                    if (item is SidebarItem.Folder) {
                                        showFolderGridDialog(context, item, manager) { styleIndex, popupCols ->"""
content = content.replace(old_grid_size, new_grid_size)

old_change_icon = """                                "Change Icon" -> {
                                    showIconPickerDialog(context, item, manager) {
                                        refresh()
                                    }
                                }"""
new_change_icon = """                                "Change Icon" -> {
                                    com.example.LogKeeper.writeLog("SidebarEdit", "Changing icon for ${item.label}")
                                    showIconPickerDialog(context, item, manager) {
                                        refresh()
                                    }
                                }"""
content = content.replace(old_change_icon, new_change_icon)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
