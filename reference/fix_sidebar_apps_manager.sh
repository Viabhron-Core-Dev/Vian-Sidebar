#!/bin/bash
sed -i '/SidebarItem.SystemAction("log_keeper", "Log Keeper", android.R.drawable.ic_menu_agenda),/a \    SidebarItem.SystemAction("cursor", "Cursor", android.R.drawable.ic_menu_directions),' app/src/main/java/com/example/service/SidebarAppsManager.kt
