#!/bin/bash
sed -i '/SidebarItem.SystemAction("work_notes", "Work Notes", android.R.drawable.ic_menu_edit)/a\
    SidebarItem.SystemAction("hybrid_grid_floating", "Hybrid Grid (Floating)", android.R.drawable.ic_menu_gallery),
' app/src/main/java/com/example/service/SidebarAppsManager.kt
