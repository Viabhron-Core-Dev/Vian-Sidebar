#!/bin/bash
sed -i '/} catch (e: Exception) {}/a\
        val isFloating = intent.getBooleanExtra("IS_FLOATING", false)\
        if (isFloating) {\
            val sIntent = Intent(this, com.example.service.SidebarService::class.java)\
            sIntent.action = "EXECUTE_ACTION"\
            sIntent.putExtra("ACTION_ID", "system:hybrid_grid_floating_exit_edit")\
            startService(sIntent)\
        }\
' app/src/main/java/com/example/HybridGridEditActivity.kt
