#!/bin/bash
sed -i '/private suspend fun loadAllAppsFromPackageManager/,/}/c\
    private suspend fun loadAllAppsFromPackageManager() = withContext(Dispatchers.IO) {\
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps\
        val userHandle = android.os.Process.myUserHandle()\
        val apps = launcherApps.getActivityList(null, userHandle)\
        val result = mutableListOf<AppInfo>()\
        for (activityInfo in apps) {\
            val packageName = activityInfo.applicationInfo.packageName\
            val label = activityInfo.label.toString()\
            result.add(AppInfo(packageName, label))\
        }\
        val distinctResult = result.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }\
        allInstalledApps = distinctResult\
    }\
' app/src/main/java/com/example/service/SidebarAppsManager.kt
