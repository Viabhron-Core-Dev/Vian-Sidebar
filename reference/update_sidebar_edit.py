import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

# Add rootLocalIds and currentFolderId
init_vars = """    val localIds = mutableListOf<String>()
    val rootLocalIds = mutableListOf<String>()
    var currentFolderId: String? = null
    var titleView: TextView? = null
    var btnBack: Button? = null
    var btnReset: Button? = null"""

content = re.sub(r'    val localIds = mutableListOf<String>\(\)', init_vars, content)

# Change attach()
attach_old = """    fun attach() {
        if (windowToken == null) {
            localIds.clear()
            localIds.addAll(manager.activeItems.map { it.id })
            com.example.LogKeeper.writeLog("SidebarEdit", "localIds on attach: $localIds")
            refresh()
            windowManager.addView(this, layoutParams)
        }
    }"""
attach_new = """    fun attach() {
        if (windowToken == null) {
            rootLocalIds.clear()
            rootLocalIds.addAll(manager.activeItems.map { it.id })
            currentFolderId = null
            localIds.clear()
            localIds.addAll(rootLocalIds)
            com.example.LogKeeper.writeLog("SidebarEdit", "localIds on attach: $localIds")
            updateUIState()
            windowManager.addView(this, layoutParams)
        }
    }"""
content = content.replace(attach_old, attach_new)

# Add helper methods
helpers = """    private fun commitCurrentFolder() {
        val folderId = currentFolderId ?: return
        val uuid = folderId.split(":")[1]
        val index = rootLocalIds.indexOfFirst { it.startsWith("folder:$uuid:") }
        if (index != -1) {
            val oldStr = rootLocalIds[index]
            val parts = oldStr.split(":", limit = 3)
            val folderDataStr = parts[2]
            val obj = org.json.JSONObject(folderDataStr)
            val itemsArr = JSONArray()
            localIds.forEach { itemsArr.put(it) }
            obj.put("items", itemsArr)
            rootLocalIds[index] = "folder:${parts[1]}:${obj.toString()}"
        }
    }

    fun saveCurrentState() {
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
    }

    private fun enterFolder(folder: SidebarItem.Folder) {
        rootLocalIds.clear()
        rootLocalIds.addAll(localIds) // Save current root state
        currentFolderId = folder.id
        localIds.clear()
        localIds.addAll(folder.items)
        updateUIState()
    }

    private fun exitFolder() {
        commitCurrentFolder()
        currentFolderId = null
        localIds.clear()
        localIds.addAll(rootLocalIds)
        updateUIState()
    }

    private fun updateUIState() {
        if (currentFolderId != null) {
            val f = manager.parseId(currentFolderId!!) as? SidebarItem.Folder
            titleView?.text = "Editing Folder: ${f?.name ?: ""}"
            btnBack?.visibility = View.VISIBLE
            btnReset?.visibility = View.GONE
        } else {
            titleView?.text = "Edit Sidebar"
            btnBack?.visibility = View.GONE
            btnReset?.visibility = View.VISIBLE
        }
        refresh()
    }"""

content = content.replace("""    fun saveCurrentState() {
        val jArr = JSONArray()
        localIds.forEach { jArr.put(it) }
        prefs.edit().putString("sidebar_apps", jArr.toString()).apply()
        manager.reloadActiveApps()
    }""", helpers)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
