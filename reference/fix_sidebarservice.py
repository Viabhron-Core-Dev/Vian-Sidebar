import re

with open("app/src/main/java/com/example/service/SidebarService.kt", "r") as f:
    content = f.read()

replacement = """    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "EXECUTE_ACTION") {
            val actionId = intent.getStringExtra("ACTION_ID")
            if (actionId != null) {
                executeElementAction(actionId)
            }
            return START_NOT_STICKY
        }
        if (intent?.action == "UPDATE_CONFIG") {"""

content = content.replace("    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {\n        if (intent?.action == \"UPDATE_CONFIG\") {", replacement)

with open("app/src/main/java/com/example/service/SidebarService.kt", "w") as f:
    f.write(content)
