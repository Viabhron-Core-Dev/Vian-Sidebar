with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    text = f.read()

old_when = """                "scheduler" -> com.example.feature.sidebar.SchedulerPageView(context)
                "notifications" -> com.example.feature.sidebar.NotificationPageView(context, viewScope)
                "resources_tracker" -> com.example.feature.sidebar.ResourcesTrackerPageView(context, viewScope)"""

new_when = """                "scheduler" -> com.example.feature.sidebar.SchedulerPageView(context, viewScope)
                "notifications" -> com.example.feature.sidebar.NotificationPageView(context, { onClose() }, { /* TODO: onHideApp */ })
                "resources_tracker" -> com.example.feature.sidebar.ResourcesTrackerPageView(context, viewScope)"""

text = text.replace(old_when, new_when)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(text)
