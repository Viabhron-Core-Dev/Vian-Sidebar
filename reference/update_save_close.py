import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

save_close_old = """    private fun saveAndClose() {
        val jArr = JSONArray()
        localIds.forEach { jArr.put(it) }
        prefs.edit().putString("sidebar_apps", jArr.toString()).apply()
        manager.reloadActiveApps()
        close()
    }"""

save_close_new = """    private fun saveAndClose() {
        saveCurrentState()
        close()
    }"""

content = content.replace(save_close_old, save_close_new)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
