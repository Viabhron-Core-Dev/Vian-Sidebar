with open('app/src/main/java/com/example/feature/sidebar/NotificationPageView.kt', 'r') as f:
    text = f.read()

text = text.replace('class NotificationPageView(\n    context: Context,\n    private val onCloseSidebar: () -> Unit,\n    private val onHideApp: (String) -> Unit\n) : FrameLayout(context) {',
'''class NotificationPageView(
    context: Context,
    private val onCloseSidebar: () -> Unit,
    private val onHideApp: (String) -> Unit
) : FrameLayout(context), SidebarPageControllable {''')

old_init = """        val hasPermission = checkNotificationPermission()
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
    }"""

new_init = """        val hasPermission = checkNotificationPermission()
        llPermissionBanner.visibility = if (hasPermission) View.GONE else View.VISIBLE
    }

    private var isReceiverRegistered = false

    override fun onPageSelected() {
        if (!isReceiverRegistered) {
            context.registerReceiver(notificationReceiver, IntentFilter().apply {
                addAction(AppNotificationListener.Companion.ACTION_NOTIFICATION_POSTED)
                addAction(AppNotificationListener.Companion.ACTION_NOTIFICATION_REMOVED)
            }, Context.RECEIVER_NOT_EXPORTED)
            isReceiverRegistered = true
        }
        loadNotifications()
    }

    override fun onPageUnselected() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(notificationReceiver)
            } catch (e: Exception) {}
            isReceiverRegistered = false
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onPageUnselected()
    }"""

text = text.replace(old_init, new_init)

with open('app/src/main/java/com/example/feature/sidebar/NotificationPageView.kt', 'w') as f:
    f.write(text)
