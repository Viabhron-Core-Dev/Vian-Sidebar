package com.example.feature.sidebar

import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import android.provider.Settings
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.utils.AppTrackerHelper
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
) : FrameLayout(context), SidebarPageControllable {

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
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        com.example.core.LogKeeper.writeLog("AppTracker", "Opened App Tracker page")
        LayoutInflater.from(context).inflate(R.layout.page_app_tracker, this, true)

        recyclerView = findViewById(R.id.recycler_view)
        tvEmpty = findViewById(R.id.tv_empty)
        llPermissionBanner = findViewById(R.id.ll_permission_banner)
        tabRunning = findViewById(R.id.tab_running)
        tabCache = findViewById(R.id.tab_cache)
        fabStopAll = findViewById(R.id.fab_stop_all)

        recyclerView.adapter = adapter

        findViewById<View>(R.id.btn_grant).setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {}
            onCloseSidebar()
        }

        tabRunning.setOnClickListener { setTab(0) }
        tabCache.setOnClickListener { setTab(1) }

        fabStopAll.setOnClickListener {
            AppTrackerHelper.startForceStopSequence(context)
            onCloseSidebar()
        }

        hasUsageStatsPermission = AppTrackerHelper.checkUsageStatsPermission(context)
        llPermissionBanner.visibility = if (hasUsageStatsPermission) View.GONE else View.VISIBLE

        loadData()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val perm = AppTrackerHelper.checkUsageStatsPermission(context)
        if (perm != hasUsageStatsPermission) {
            hasUsageStatsPermission = perm
            llPermissionBanner.visibility = if (hasUsageStatsPermission) View.GONE else View.VISIBLE
            loadData()
        }
    }

    private fun setTab(index: Int) {
        selectedTab = index
        if (index == 0) {
            tabRunning.setBackgroundResource(R.drawable.bg_tab_selected)
            tabRunning.setTextColor(0xFF000000.toInt())
            tabCache.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabCache.setTextColor(0xFFFFFFFF.toInt())
            fabStopAll.visibility = if (recentApps.isNotEmpty()) View.VISIBLE else View.GONE
            recyclerView.layoutManager = GridLayoutManager(context, 4)
            updateList(recentApps, true)
        } else {
            tabCache.setBackgroundResource(R.drawable.bg_tab_selected)
            tabCache.setTextColor(0xFF000000.toInt())
            tabRunning.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabRunning.setTextColor(0xFFFFFFFF.toInt())
            fabStopAll.visibility = View.GONE
            recyclerView.layoutManager = LinearLayoutManager(context)
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
                recentApps = apps
            } else {
                recentApps = emptyList()
            }
            
            val caches = withContext(Dispatchers.IO) { getAppsWithCache(context) }
            cacheApps = caches

            setTab(selectedTab) // Refresh UI
        }
    }

    private fun getRecentApps(context: Context): List<TrackedAppInfo> {
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
        
        val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        val whitelist = prefs.getStringSet("app_tracker_whitelist_current", emptySet()) ?: emptySet()
        val showSystem = prefs.getBoolean("app_tracker_show_system_running", false)

        val pm = context.packageManager
        val trackedApps = mutableListOf<TrackedAppInfo>()
        
        for ((packageName, lastUsed) in appLastUsed) {
            if (packageName == context.packageName) continue
            if (packageName.contains("launcher", ignoreCase = true)) continue
            if (whitelist.contains(packageName)) continue

            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (isSystem && !showSystem) continue

                val appName = pm.getApplicationLabel(appInfo).toString()
                trackedApps.add(TrackedAppInfo(packageName = packageName, appName = appName, lastUsedTime = lastUsed))
            } catch (e: Exception) {}
        }
        
        return trackedApps.sortedByDescending { it.lastUsedTime }.take(28)
    }
    
    private fun getAppsWithCache(context: Context): List<TrackedAppInfo> {
        val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
        val whitelist = prefs.getStringSet("app_tracker_whitelist_cache", emptySet()) ?: emptySet()
        val showSystem = prefs.getBoolean("app_tracker_show_system_cache", false)

        val pm = context.packageManager
        val packages = pm.getInstalledPackages(0)
        val apps = mutableListOf<TrackedAppInfo>()

        val storageStatsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else null
        val userHandle = Process.myUserHandle()
        val uuid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) StorageManager.UUID_DEFAULT else null
        
        for (pi in packages) {
            try {
                if (pi.packageName == context.packageName) continue
                if (pi.packageName.contains("launcher", ignoreCase = true)) continue
                if (whitelist.contains(pi.packageName)) continue

                val appInfo = pi.applicationInfo ?: continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (isSystem && !showSystem) continue

                var cacheSize = 0L
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && storageStatsManager != null && uuid != null) {
                    try {
                        val stats = storageStatsManager.queryStatsForPackage(uuid, pi.packageName, userHandle)
                        cacheSize = stats.cacheBytes
                    } catch (e: Exception) {}
                }

                val appName = pm.getApplicationLabel(appInfo).toString()
                apps.add(TrackedAppInfo(
                    packageName = pi.packageName,
                    appName = appName,
                    cacheSize = cacheSize
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

        override fun getItemViewType(position: Int): Int {
            return if (isRunning) 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutRes = if (viewType == 0) R.layout.item_app_tracker_grid else R.layout.item_app_tracker_row
            val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
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
                            data = Uri.parse("package:${app.packageName}")
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

    override fun onEditClicked() {
        val intent = Intent(context, com.example.AppTrackerSettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        onCloseSidebar()
    }
}

data class TrackedAppInfo(
    val packageName: String,
    val appName: String,
    val lastUsedTime: Long = 0,
    val cacheSize: Long = 0
)

