import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    text = f.read()

old_when = """                else -> {
                    TextView(context).apply {
                        text = "Page: ${config.title}\\nType: ${config.type}\\n(Not Implemented)"
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER"""

new_when = """                "scheduler" -> com.example.feature.sidebar.SchedulerPageView(context)
                "notifications" -> com.example.feature.sidebar.NotificationPageView(context, viewScope)
                "resources_tracker" -> com.example.feature.sidebar.ResourcesTrackerPageView(context, viewScope)
                else -> {
                    TextView(context).apply {
                        text = "Page: ${config.title}\\nType: ${config.type}\\n(Not Implemented)"
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER"""

text = text.replace(old_when, new_when)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(text)
