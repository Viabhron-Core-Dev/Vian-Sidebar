import re

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'r') as f:
    content = f.read()

old_folder = """    data class Folder(
        val uuid: String,
        val name: String,
        val colorHex: String,
        val items: List<String>,
        val folderStyle: Int = 0,
        val popupColumns: Int = 0,
        override var id: String = "folder:$uuid"
    ) : SidebarItem() {"""
new_folder = """    data class Folder(
        val uuid: String,
        val name: String,
        val colorHex: String,
        val items: List<String>,
        val folderStyle: Int = 0,
        val popupColumns: Int = 0,
        val popupRows: Int = 0,
        override var id: String = "folder:$uuid"
    ) : SidebarItem() {"""
content = content.replace(old_folder, new_folder)

old_parse = """                val folderStyle = obj.optInt("folderStyle", 0)
                val popupColumns = obj.optInt("popupColumns", 0)
                return SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, popupColumns, id)"""
new_parse = """                val folderStyle = obj.optInt("folderStyle", 0)
                val popupColumns = obj.optInt("popupColumns", 0)
                val popupRows = obj.optInt("popupRows", 0)
                return SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, popupColumns, popupRows, id)"""
content = content.replace(old_parse, new_parse)

old_add = """                    val folderStyle = obj.optInt("folderStyle", 0)
                    val popupColumns = obj.optInt("popupColumns", 0)
                    result.add(SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, popupColumns, id))"""
new_add = """                    val folderStyle = obj.optInt("folderStyle", 0)
                    val popupColumns = obj.optInt("popupColumns", 0)
                    val popupRows = obj.optInt("popupRows", 0)
                    result.add(SidebarItem.Folder(uuid, obj.getString("name"), obj.getString("colorHex"), itemsList, folderStyle, popupColumns, popupRows, id))"""
content = content.replace(old_add, new_add)

with open('app/src/main/java/com/example/service/SidebarAppsManager.kt', 'w') as f:
    f.write(content)
