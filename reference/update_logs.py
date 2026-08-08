import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

# Add log to close
old_close = """    fun close() {
        detach()
        onClose()
    }"""
new_close = """    fun close() {
        com.example.LogKeeper.writeLog("SidebarEdit", "Closed sidebar edit overlay")
        detach()
        onClose()
    }"""
content = content.replace(old_close, new_close)

# Add log to saveCurrentState
old_save = """    fun saveCurrentState() {
        if (currentFolderId != null) {
            commitCurrentFolder()
        } else {
            rootLocalIds.clear()
            rootLocalIds.addAll(localIds)
        }
        val jArr = JSONArray()
        rootLocalIds.forEach { jArr.put(it) }
        prefs.edit().putString("sidebar_apps", jArr.toString()).apply()
        manager.reloadActiveApps()
    }"""
new_save = """    fun saveCurrentState() {
        com.example.LogKeeper.writeLog("SidebarEdit", "Saving current state. isFolder=${currentFolderId != null}")
        if (currentFolderId != null) {
            commitCurrentFolder()
        } else {
            rootLocalIds.clear()
            rootLocalIds.addAll(localIds)
        }
        val jArr = JSONArray()
        rootLocalIds.forEach { jArr.put(it) }
        prefs.edit().putString("sidebar_apps", jArr.toString()).apply()
        manager.reloadActiveApps()
    }"""
content = content.replace(old_save, new_save)

# Add log to enterFolder
old_enter = """    private fun enterFolder(folder: SidebarItem.Folder) {
        rootLocalIds.clear()
        rootLocalIds.addAll(localIds) // Save current root state
        currentFolderId = folder.id
        localIds.clear()
        localIds.addAll(folder.items)
        updateUIState()
    }"""
new_enter = """    private fun enterFolder(folder: SidebarItem.Folder) {
        com.example.LogKeeper.writeLog("SidebarEdit", "Entering folder: ${folder.name}")
        rootLocalIds.clear()
        rootLocalIds.addAll(localIds) // Save current root state
        currentFolderId = folder.id
        localIds.clear()
        localIds.addAll(folder.items)
        updateUIState()
    }"""
content = content.replace(old_enter, new_enter)

# Add log to exitFolder
old_exit = """    private fun exitFolder() {
        commitCurrentFolder()
        currentFolderId = null
        localIds.clear()
        localIds.addAll(rootLocalIds)
        updateUIState()
    }"""
new_exit = """    private fun exitFolder() {
        com.example.LogKeeper.writeLog("SidebarEdit", "Exiting folder")
        commitCurrentFolder()
        currentFolderId = null
        localIds.clear()
        localIds.addAll(rootLocalIds)
        updateUIState()
    }"""
content = content.replace(old_exit, new_exit)

# Add log to onAddClicked
old_add = """            setOnClickListener { onAddClicked() }"""
new_add = """            setOnClickListener { 
                com.example.LogKeeper.writeLog("SidebarEdit", "Add button clicked")
                onAddClicked() 
            }"""
content = content.replace(old_add, new_add, 1)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
