package com.example.feature.sidebar

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.core.LogKeeper
import com.example.service.AppNotificationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationPageView(
    context: Context,
    private val onCloseSidebar: () -> Unit,
    private val onHideApp: (String) -> Unit,
    private val onHeightChanged: ((Int) -> Unit)? = null
) : FrameLayout(context), SidebarPageControllable {

    private val recyclerView: RecyclerView
    private val tvEmpty: TextView
    private val llPermissionBanner: View
    private val btnClearAll: ImageButton

    private val adapter = NotificationAdapter()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var activeNotifications = listOf<StatusBarNotification>()

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadNotifications()
        }
    }

    init {
        LogKeeper.writeLog("Notification", "Opened Notification mirror page")
        LayoutInflater.from(context).inflate(R.layout.page_notification, this, true)

        recyclerView = findViewById(R.id.recycler_view)
        tvEmpty = findViewById(R.id.tv_empty)
        llPermissionBanner = findViewById(R.id.ll_permission_banner)
        btnClearAll = findViewById(R.id.btn_clear_all)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        findViewById<View>(R.id.btn_grant).setOnClickListener {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onCloseSidebar()
        }

        btnClearAll.setOnClickListener {
            AppNotificationListener.instance?.let { listener ->
                try {
                    listener.cancelAllNotifications()
                    LogKeeper.writeLog("Notification", "Cleared all dismissible notifications")
                    loadNotifications()
                } catch (e: Exception) {
                    LogKeeper.writeLog("Notification", "Failed to clear all notifications: ${e.message}")
                }
            }
        }

        val hasPermission = checkNotificationPermission()
        llPermissionBanner.visibility = if (hasPermission) View.GONE else View.VISIBLE
        
        val filter = IntentFilter().apply {
            addAction(AppNotificationListener.ACTION_NOTIFICATION_POSTED)
            addAction(AppNotificationListener.ACTION_NOTIFICATION_REMOVED)
        }
        context.registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

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
        if (!checkNotificationPermission()) {
            llPermissionBanner.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            adapter.submitList(emptyList())
            return
        }

        llPermissionBanner.visibility = View.GONE
        val listener = AppNotificationListener.instance

        if (listener != null) {
            try {
                val prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
                val sidebarHidden = prefs.getStringSet("sidebar_hidden_packages", emptySet()) ?: emptySet()

                // Mirror all live notifications in Android notification bar, excluding self app and user-blocked packages
                val sbns = listener.activeNotifications
                    .filter { 
                        it.packageName != context.packageName && 
                        !sidebarHidden.contains(it.packageName)
                    }
                    .sortedByDescending { it.postTime }

                activeNotifications = sbns
                adapter.submitList(activeNotifications)
                tvEmpty.visibility = if (activeNotifications.isEmpty()) View.VISIBLE else View.GONE
                btnClearAll.visibility = if (activeNotifications.any { it.isClearable }) View.VISIBLE else View.GONE

                post {
                    measure(
                        MeasureSpec.makeMeasureSpec(width.takeIf { it > 0 } ?: (330 * resources.displayMetrics.density).toInt(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    )
                    val density = resources.displayMetrics.density
                    val minH = (180 * density).toInt()
                    val maxH = (520 * density).toInt()
                    val targetH = measuredHeight.coerceIn(minH, maxH)
                    onHeightChanged?.invoke(targetH)
                }
            } catch (e: Exception) {
                LogKeeper.writeLog("Notification", "Error loading active notifications: ${e.message}")
            }
        } else {
            // Listener instance might be initializing; prompt permission / show empty
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Waiting for notification service..."
            adapter.submitList(emptyList())
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

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

            holder.tvTitle.text = if (title.isNotBlank()) title else sbn.packageName
            holder.tvText.text = if (text.isNotBlank()) text else subText
            holder.tvText.visibility = if (holder.tvText.text.isNotBlank()) View.VISIBLE else View.GONE
            
            val timeString = DateUtils.getRelativeTimeSpanString(
                sbn.postTime, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
            ).toString()
            holder.tvTime.text = timeString

            // Dismiss button (only shown if notification is clearable)
            if (sbn.isClearable) {
                holder.btnDismiss.visibility = View.VISIBLE
                holder.btnDismiss.setOnClickListener {
                    try {
                        AppNotificationListener.instance?.cancelNotification(sbn.key)
                        LogKeeper.writeLog("Notification", "Dismissed notification: ${sbn.packageName}")
                    } catch (e: Exception) {
                        try {
                            AppNotificationListener.instance?.cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                        } catch (e2: Exception) {}
                    }
                }
            } else {
                holder.btnDismiss.visibility = View.GONE
            }

            scope.launch(Dispatchers.IO) {
                try {
                    val appInfo = context.packageManager.getApplicationInfo(sbn.packageName, 0)
                    val appName = context.packageManager.getApplicationLabel(appInfo).toString()
                    val icon = context.packageManager.getApplicationIcon(appInfo)
                    
                    withContext(Dispatchers.Main) {
                        holder.tvAppName.text = appName
                        holder.ivIcon.setImageDrawable(icon)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        holder.tvAppName.text = sbn.packageName
                    }
                }
            }

            holder.itemView.setOnClickListener {
                try {
                    notification.contentIntent?.send()
                    LogKeeper.writeLog("Notification", "Opened notification for: ${sbn.packageName}")
                    onCloseSidebar()
                } catch (e: Exception) {
                    LogKeeper.writeLog("Notification", "Failed to launch notification intent: ${e.message}")
                }
            }
        }

        override fun getItemCount() = list.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
            val tvAppName: TextView = itemView.findViewById(R.id.tv_app_name)
            val tvTime: TextView = itemView.findViewById(R.id.tv_time)
            val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
            val tvText: TextView = itemView.findViewById(R.id.tv_text)
            val btnDismiss: ImageButton = itemView.findViewById(R.id.btn_dismiss)
        }
    }
}
