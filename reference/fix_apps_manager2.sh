#!/bin/bash
sed -i '/val apps = pm.queryIntentActivities(intent, 0)/,/allInstalledApps = distinctResult/d' app/src/main/java/com/example/service/SidebarAppsManager.kt
