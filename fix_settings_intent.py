import re

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'r') as f:
    content = f.read()

# Replace the initial state with intent handling
init_route = """                    var currentRoute by remember { mutableStateOf("handles_list") }
                    var currentHandleId by remember { mutableStateOf("") }
                    var currentGestureIdStr by remember { mutableStateOf("") }
                    var editPageId by remember { mutableStateOf<String?>(null) }
                    
                    LaunchedEffect(Unit) {
                        val editPage = intent.getStringExtra("sidebar_edit_page_id")
                        val container = intent.getStringExtra("sidebar_container_id")
                        if (editPage != null && container != null) {
                            currentGestureIdStr = container
                            editPageId = editPage
                            currentRoute = "sidebar_settings"
                        }
                    }"""
content = content.replace('                    var currentRoute by remember { mutableStateOf("handles_list") }\n                    var currentHandleId by remember { mutableStateOf("") }\n                    var currentGestureIdStr by remember { mutableStateOf("") }', init_route)

# In SidebarSettingsScreen, pass editPageId
sidebar_settings_call = """                            SidebarSettingsScreen(
                                handleId = handleIdAndGesture,
                                initAction = initAction,
                                initialEditPageId = editPageId,
                                onBack = { 
                                    editPageId = null
                                    currentRoute = "handles_list" 
                                }
                            )"""
content = re.sub(
    r'SidebarSettingsScreen\(\s*handleId = handleIdAndGesture,\s*initAction = initAction,\s*onBack = { currentRoute = "handles_list" }\s*\)',
    sidebar_settings_call,
    content
)

with open('app/src/main/java/com/example/feature/settings/SettingsActivity.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/feature/settings/SidebarSettingsScreen.kt', 'r') as f:
    content = f.read()

# Update SidebarSettingsScreen signature
content = content.replace(
    'fun SidebarSettingsScreen(handleId: String, initAction: String? = null, onBack: () -> Unit) {',
    'fun SidebarSettingsScreen(handleId: String, initAction: String? = null, initialEditPageId: String? = null, onBack: () -> Unit) {'
)

# Handle initialEditPageId
init_effect = """    LaunchedEffect(initialEditPageId) {
        if (initialEditPageId != null) {
            val pageToEdit = pages.find { it.id == initialEditPageId }
            if (pageToEdit != null) {
                customisingPage = pageToEdit
            }
        }
    }
"""
content = content.replace('    LaunchedEffect(initAction) {', init_effect + '    LaunchedEffect(initAction) {')

with open('app/src/main/java/com/example/feature/settings/SidebarSettingsScreen.kt', 'w') as f:
    f.write(content)
