#!/bin/bash
sed -i '/val pm = context.packageManager/,/activities = list/c\
            val pm = context.packageManager\
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as android.content.pm.LauncherApps\
            val list = mutableListOf<ResolveInfo>()\
            try {\
                val apps = launcherApps.getActivityList(null, android.os.Process.myUserHandle())\
                for (app in apps) {\
                    val intent = Intent(Intent.ACTION_MAIN)\
                    intent.setClassName(app.applicationInfo.packageName, app.componentName.className)\
                    val resolveInfos = pm.queryIntentActivities(intent, 0)\
                    list.addAll(resolveInfos)\
                }\
            } catch (e: Exception) {\
                e.printStackTrace()\
            }\
            withContext(Dispatchers.Main) {\
                activities = list\
' app/src/main/java/com/example/IntentPickerActivity.kt
