#!/bin/bash
sed -i '/val intent = android.content.Intent/,/}.distinctBy { it.first }/c\
                            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps\
                            val apps = try { launcherApps.getActivityList(null, android.os.Process.myUserHandle()) } catch(e: Exception) { emptyList() }\
                            apps.map { \
                                it.applicationInfo.packageName to it.label.toString()\
                            }.distinctBy { it.first }\
' app/src/main/java/com/example/NotificationHistoryActivity.kt
