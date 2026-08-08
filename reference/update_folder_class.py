import re

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'r') as f:
    content = f.read()

old_class = """    data class Folder(
        val uuid: String,
        val name: String,
        val colorHex: String,
        val items: List<String>,
        val folderStyle: Int = 0,
        override var id: String = "folder:$uuid"
    ) : SidebarItem() {"""

new_class = """    data class Folder(
        val uuid: String,
        val name: String,
        val colorHex: String,
        val items: List<String>,
        val folderStyle: Int = 0,
        val popupColumns: Int = 0,
        override var id: String = "folder:$uuid"
    ) : SidebarItem() {"""

content = content.replace(old_class, new_class)

old_parse1 = """                val folderStyle = obj.optInt("folderStyle", 0)
                return SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, id)"""

new_parse1 = """                val folderStyle = obj.optInt("folderStyle", 0)
                val popupColumns = obj.optInt("popupColumns", 0)
                return SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, popupColumns, id)"""

content = content.replace(old_parse1, new_parse1)

old_parse2 = """                    val folderStyle = obj.optInt("folderStyle", 0)
                    result.add(SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, id))"""

new_parse2 = """                    val folderStyle = obj.optInt("folderStyle", 0)
                    val popupColumns = obj.optInt("popupColumns", 0)
                    result.add(SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, popupColumns, id))"""

content = content.replace(old_parse2, new_parse2)

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'w') as f:
    f.write(content)
