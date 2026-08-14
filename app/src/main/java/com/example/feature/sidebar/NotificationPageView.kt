package com.example.feature.sidebar

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.core.LogKeeper
import com.example.feature.settings.TagManagementActivity
import com.example.service.AppNotificationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationPageView(
    context: Context,
    private val onCloseSidebar: () -> Unit,
    private val onHideApp: (String) -> Unit
) : FrameLayout(context), SidebarPageControllable {

    private val recyclerView: RecyclerView
    private val tvEmpty: TextView
    private val llPermissionBanner: View
    private val fabClearAll: View

    private val adapter = NotificationAdapter()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var activeNotifications = listOf<StatusBarNotification>()

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AppNotificationListener.Companion.ACTION_NOTIFICATION_POSTED || 
                intent?.action == AppNotificationListener.Companion.ACTION_NOTIFICATION_REMOVED) {
                loadNotifications()
            }
        }
    }

    init {
        com.example.core.LogKeeper.writeLog("Notification", "Opened Notification page")
        LayoutInflater.from(context).inflate(R.layout.page_notification, this, true)

        recyclerView = findViewById(R.id.recycler_view)
        tvEmpty = findViewById(R.id.tv_empty)
        llPermissionBanner = findViewById(R.id.ll_permission_banner)
        fabClearAll = findViewById(R.id.fab_clear_all)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter


        findViewById<View>(R.id.btn_history).setOnClickListener {
            val historyIntent = Intent(context, com.example.NotificationHistoryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(historyIntent)
            onCloseSidebar()
        }
        
        findViewById<View>(R.id.btn_grant).setOnClickListener {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onCloseSidebar()
        }

        fabClearAll.setOnClickListener {
            val intent = Intent(context, AppNotificationListener::class.java).apply {
                action = AppNotificationListener.Companion.ACTION_CLEAR_ALL
            }
            context.startService(intent)
        }

        val hasPermission = checkNotificationPermission()
        llPermissionBanner.visibility = if (hasPermission) View.GONE else View.VISIBLE
        
        context.registerReceiver(notificationReceiver, IntentFilter().apply {
            addAction(AppNotificationListener.Companion.ACTION_NOTIFICATION_POSTED)
            addAction(AppNotificationListener.Companion.ACTION_NOTIFICATION_REMOVED)
        }, Context.RECEIVER_NOT_EXPORTED)

        loadNotifications()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            context.unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {}
    }

    private fun checkNotificationPermission(): Boolean {
        val listeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return listeners != null && listeners.contains(context.packageName)
    }

    private fun loadNotifications() {
        if (checkNotificationPermission()) {
            AppNotificationListener.instance?.let { listener ->
                try {
                    val sbns = listener.activeNotifications
                        .filter { it.isClearable }
                        .sortedByDescending { it.postTime }
                    
                    activeNotifications = sbns
                    adapter.submitList(activeNotifications)
                    tvEmpty.visibility = if (activeNotifications.isEmpty()) View.VISIBLE else View.GONE
                } catch (e: Exception) {}
            }
        }
    }

    private inner class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
        private var list = emptyList<StatusBarNotification>()

        fun submitList(newList: List<StatusBarNotification>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sbn = list[position]
            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            holder.tvTitle.text = title
            holder.tvText.text = text
            
            val timeString = DateUtils.getRelativeTimeSpanString(
                sbn.postTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            ).toString()
            holder.tvTime.text = timeString

            scope.launch(Dispatchers.IO) {
                try {
                    val appInfo = context.packageManager.getApplicationInfo(sbn.packageName, 0)
                    val appName = context.packageManager.getApplicationLabel(appInfo).toString()
                    val icon = context.packageManager.getApplicationIcon(appInfo)
                    
                    withContext(Dispatchers.Main) {
                        holder.tvAppName.text = appName
                        holder.ivIcon.setImageDrawable(icon)
                    }
                } catch (e: Exception) {}
            }

            holder.itemView.setOnClickListener {
                try {
                    notification.contentIntent?.send()
                    onCloseSidebar()
                } catch (e: Exception) {}
            }
        }

        override fun getItemCount() = list.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
            val tvAppName: TextView = itemView.findViewById(R.id.tv_app_name)
            val tvTime: TextView = itemView.findViewById(R.id.tv_time)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            val tvText: TextView = itemView.findViewById(R.id.tv_text)
        }
    }
}
