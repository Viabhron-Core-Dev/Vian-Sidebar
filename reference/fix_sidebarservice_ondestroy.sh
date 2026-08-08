#!/bin/bash
sed -i '/override fun onDestroy() {/a\
        try {\
            val importsDir = java.io.File(filesDir, "pwa_imports")\
            if (importsDir.exists() && importsDir.isDirectory) {\
                importsDir.listFiles()?.forEach { it.delete() }\
            }\
        } catch (e: Exception) {\
            e.printStackTrace()\
        }\
' app/src/main/java/com/example/service/SidebarService.kt
