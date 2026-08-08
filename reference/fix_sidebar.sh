#!/bin/bash
sed -i '/private fun reloadHandles() {/a\
        if (!android.provider.Settings.canDrawOverlays(this)) return\
' app/src/main/java/com/example/service/SidebarService.kt
