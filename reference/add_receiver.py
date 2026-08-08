import re

with open('app/src/main/java/com/example/PageManagementSettingsScreen.kt', 'r') as f:
    content = f.read()

receiver_code = """    var showAddDialog by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: android.content.Intent?) {
                if (intent?.action == "WIDGET_PAGE_CREATED") {
                    val widgetId = intent.getIntExtra("WIDGET_ID", -1)
                    if (widgetId != -1) {
                        val newPages = pages.toMutableList()
                        val title = "App Widget"
                        val page = SidebarPage.createDefault(id = UUID.randomUUID().toString(), type = "widget", title = title)
                        page.id = "widget:$widgetId" // use id to store widget ID
                        newPages.add(page)
                        pages = newPages
                        PageManager.savePages(prefs, pages)
                    }
                }
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, android.content.IntentFilter("WIDGET_PAGE_CREATED"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, android.content.IntentFilter("WIDGET_PAGE_CREATED"))
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }"""

content = content.replace("    var showAddDialog by remember { mutableStateOf(false) }", receiver_code)

with open('app/src/main/java/com/example/PageManagementSettingsScreen.kt', 'w') as f:
    f.write(content)

