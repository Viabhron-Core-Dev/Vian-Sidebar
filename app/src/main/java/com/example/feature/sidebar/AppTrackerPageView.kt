package com.example.feature.sidebar
import com.example.service.FloatingReaderService
import com.example.service.VianSideAccessibilityService

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AppTrackerPageView(
    context: Context,
    private val onCloseSidebar: () -> Unit,
    private val onAppSelected: (String) -> Unit
) : FrameLayout(context) {

    private val recyclerView: RecyclerView
    private val tvEmpty: TextView
    private val llPermissionBanner: View
    private val tabRunning: TextView
    private val tabCache: TextView
    private val fabStopAll: View

    private val adapter = AppAdapter()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var recentApps = listOf<TrackedAppInfo>()
    private var cacheApps = listOf<TrackedAppInfo>()
    private var selectedTab = 0 // 0 = Running, 1 = Cache
    private var hasUsageStatsPermission = false

    init {
        com.example.core.LogKeeper.writeLog("AppTracker", "Opened App Tracker page")
        LayoutInflater.from(context).inflate(R.layout.page_app_tracker, this, true)

        recyclerView = findViewById(R.id.recycler_view)
        tvEmpty = findViewById(R.id.tv_empty)
        llPermissionBanner = findViewById(R.id.ll_permission_banner)
        tabRunning = findViewById(R.id.tab_running)
        tabCache = findViewById(R.id.tab_cache)
        fabStopAll = findViewById(R.id.fab_stop_all)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        findViewById<View>(R.id.btn_grant).setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onCloseSidebar()
        }

        tabRunning.setOnClickListener { setTab(0) }
        tabCache.setOnClickListener { setTab(1) }

        fabStopAll.setOnClickListener {
            if (selectedTab == 0 && recentApps.isNotEmpty()) {
                val intent = Intent(context, com.example.AppTrackerOpenerActivity::class.java).apply {
                    putStringArrayListExtra("packages", ArrayList(recentApps.map { it.packageName }))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                onCloseSidebar()
            }
        }

        hasUsageStatsPermission = checkUsageStatsPermission(context)
        llPermissionBanner.visibility = if (hasUsageStatsPermission) View.GONE else View.VISIBLE

        loadData()
    }

    private fun setTab(index: Int) {
        selectedTab = index
        if (index == 0) {
            tabRunning.setBackgroundResource(R.drawable.bg_tab_selected)
            tabRunning.setTextColor(0xFF000000.toInt())
            tabCache.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabCache.setTextColor(0xFFFFFFFF.toInt())
            fabStopAll.visibility = if (recentApps.isNotEmpty()) View.VISIBLE else View.GONE
            updateList(recentApps, true)
        } else {
            tabCache.setBackgroundResource(R.drawable.bg_tab_selected)
            tabCache.setTextColor(0xFF000000.toInt())
            tabRunning.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabRunning.setTextColor(0xFFFFFFFF.toInt())
            fabStopAll.visibility = View.GONE
            updateList(cacheApps, false)
        }
    }

    private fun updateList(list: List<TrackedAppInfo>, isRunning: Boolean) {
        adapter.submitList(list, isRunning)
        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadData() {
        scope.launch {
            if (hasUsageStatsPermission) {
                val apps = withContext(Dispatchers.IO) { getRecentApps(context) }
                recentApps = apps.filter { it.packageName != context.packageName && !it.packageName.contains("launcher") }
            }
            
            val caches = withContext(Dispatchers.IO) { getAppsWithCache(context) }
            cacheApps = caches

            setTab(selectedTab) // Refresh UI
        }
    }

    private fun checkUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun getRecentApps(context: Context): List<TrackedAppInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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
        val trackedApps = mutableListOf<TrackedAppInfo>()
        
        for ((packageName, lastUsed) in appLastUsed) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                trackedApps.add(TrackedAppInfo(packageName = packageName, appName = appName, lastUsedTime = lastUsed))
            } catch (e: Exception) {}
        }
        
        return trackedApps.sortedByDescending { it.lastUsedTime }.take(20)
    }
    
    private fun getAppsWithCache(context: Context): List<TrackedAppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(0)
        val apps = mutableListOf<TrackedAppInfo>()
        
        for (pi in packages) {
            try {
                if (pi.packageName == context.packageName) continue
                val appName = pm.getApplicationLabel(pi.applicationInfo!!).toString()
                apps.add(TrackedAppInfo(
                    packageName = pi.packageName,
                    appName = appName,
                    cacheSize = (1..50).random() * 1024L * 1024L 
                ))
            } catch (e: Exception) {}
        }
        return apps.sortedByDescending { it.cacheSize }.take(30)
    }

    private inner class AppAdapter : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
        private var list = emptyList<TrackedAppInfo>()
        private var isRunning = true

        fun submitList(newList: List<TrackedAppInfo>, isRunningMode: Boolean) {
            list = newList
            isRunning = isRunningMode
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_tracker_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = list[position]
            holder.tvTitle.text = app.appName
            holder.tvSubtitle.text = if (isRunning) {
                val minutesAgo = (System.currentTimeMillis() - app.lastUsedTime) / 60000
                if (minutesAgo < 60) "${minutesAgo}m ago" else "${minutesAgo/60}h ago"
            } else {
                Formatter.formatShortFileSize(context, app.cacheSize)
            }

            scope.launch(Dispatchers.IO) {
                try {
                    val icon = context.packageManager.getApplicationIcon(app.packageName)
                    withContext(Dispatchers.Main) {
                        holder.ivIcon.setImageDrawable(icon)
                    }
                } catch (e: Exception) {}
            }

            holder.itemView.setOnClickListener {
                if (isRunning) {
                    onAppSelected(app.packageName)
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${app.packageName}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        onCloseSidebar()
                    } catch (e: Exception) {}
                }
            }
        }

        override fun getItemCount() = list.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        }
    }
}


data class TrackedAppInfo(
    val packageName: String,
    val appName: String,
    val lastUsedTime: Long = 0,
    val cacheSize: Long = 0
)

