filepath = 'app/src/main/java/com/example/feature/sidebar/NotificationPageView.kt'
with open(filepath, 'r') as f:
    content = f.read()

import re

# Add listener for btn_history
hook = """
        findViewById<View>(R.id.btn_history).setOnClickListener {
            val historyIntent = Intent(context, com.example.NotificationHistoryActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(historyIntent)
            onCloseSidebar()
        }
        
        findViewById<View>(R.id.btn_grant).setOnClickListener {"""

content = content.replace("        findViewById<View>(R.id.btn_grant).setOnClickListener {", hook)

with open(filepath, 'w') as f:
    f.write(content)
print("NotificationPageView updated")
