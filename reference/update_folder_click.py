import re

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'r') as f:
    content = f.read()

click_old = """            holder.view.setOnClickListener {
                val actionList = mutableListOf("Change Icon", "Remove")
                var popupWindow: android.widget.PopupWindow? = null"""

click_new = """            holder.view.setOnClickListener {
                val actionList = mutableListOf("Change Icon", "Remove")
                if (item is SidebarItem.Folder) {
                    actionList.add(0, "Edit Contents")
                }
                var popupWindow: android.widget.PopupWindow? = null"""

content = content.replace(click_old, click_new)

action_old = """                            when (action) {
                                "Remove" -> {
                                    localIds.removeAt(holder.adapterPosition)
                                    refresh()
                                }
                                "Change Icon" -> {"""

action_new = """                            when (action) {
                                "Edit Contents" -> {
                                    if (item is SidebarItem.Folder) {
                                        enterFolder(item)
                                    }
                                }
                                "Remove" -> {
                                    localIds.removeAt(holder.adapterPosition)
                                    refresh()
                                }
                                "Change Icon" -> {"""

content = content.replace(action_old, action_new)

with open('app/src/main/java/com/example/service/SidebarEditOverlayView.kt', 'w') as f:
    f.write(content)
