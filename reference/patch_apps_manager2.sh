#!/bin/bash
sed -i 's/SidebarItem.SystemAction("work_notes", "Work Notes", android.R.drawable.ic_menu_edit)/SidebarItem.SystemAction("work_notes", "Work Notes", android.R.drawable.ic_menu_edit),/' app/src/main/java/com/example/service/SidebarAppsManager.kt
