package com.example.utils

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import com.example.AppTrackerOpenerActivity
import java.util.concurrent.TimeUnit

object AppTrackerHelper {

    fun isAppTrackerConfigured(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return PageManager.isPageTypePresent(prefs, "app_tracker")
    }

    fun checkUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getRunningPackagesToStop(context: Context): List<String> {
        if (!checkUsageStatsPermission(context)) return emptyList()

        val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        val whitelist = prefs.getStringSet("app_tracker_whitelist_current", emptySet()) ?: emptySet()
        val showSystem = prefs.getBoolean("app_tracker_show_system_running", false)

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return emptyList()
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(24)

        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        val appLastUsed = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                appLastUsed[event.packageName] = event.timeStamp
            }
        }

        val pm = context.packageManager
        val resultPackages = mutableListOf<Pair<String, Long>>()

        for ((packageName, lastUsed) in appLastUsed) {
            if (packageName == context.packageName) continue
            if (packageName.contains("launcher", ignoreCase = true)) continue
            if (whitelist.contains(packageName)) continue

            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (isSystem && !showSystem) continue
                resultPackages.add(packageName to lastUsed)
            } catch (e: Exception) {}
        }

        return resultPackages.sortedByDescending { it.second }.map { it.first }
    }

    fun startForceStopSequence(context: Context) {
        if (!isAppTrackerConfigured(context)) {
            Toast.makeText(context, "Requires App Tracker page added to sidebar", Toast.LENGTH_SHORT).show()
            return
        }

        if (!checkUsageStatsPermission(context)) {
            Toast.makeText(context, "Grant Usage Access to track active apps", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {}
            return
        }

        val packagesToStop = getRunningPackagesToStop(context)
        if (packagesToStop.isEmpty()) {
            Toast.makeText(context, "No running apps to stop", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(context, AppTrackerOpenerActivity::class.java).apply {
            putStringArrayListExtra("packages", ArrayList(packagesToStop))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
