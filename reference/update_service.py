import re

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'r') as f:
    content = f.read()

old_add = """            onAddClicked = { 
                showAddElementOverlayForSelection { id ->
                    sidebarEditOverlayView?.localIds?.add(id)
                    sidebarEditOverlayView?.refresh()
                }
            },"""
new_add = """            onAddClicked = { 
                showAddElementOverlayForSelection { id ->
                    com.example.LogKeeper.writeLog("SidebarEdit", "Added new element: $id")
                    sidebarEditOverlayView?.localIds?.add(id)
                    sidebarEditOverlayView?.refresh()
                }
            },"""
content = content.replace(old_add, new_add)

with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'w') as f:
    f.write(content)
