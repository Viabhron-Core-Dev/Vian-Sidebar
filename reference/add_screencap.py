import re

with open('app/src/main/java/com/example/SettingsActivity.kt', 'r') as f:
    content = f.read()

old_main_call = """                "main" -> MainSettingsScreen(
                    onNavigateToReader = { currentRoute = "reader" },
                    onNavigateToGeneral = { currentRoute = "general" },
                    onNavigateToNetSpeed = { currentRoute = "netspeed" },
                    onNavigateToData = { currentRoute = "data" },
                    onNavigateToPages = { currentRoute = "pages" },
                    onNavigateToHandles = { currentRoute = "handles" },
                    onNavigateToCallRecorder = { currentRoute = "call_recorder" },
                    onBack = onFinish
                )"""

new_main_call = """                "main" -> MainSettingsScreen(
                    onNavigateToReader = { currentRoute = "reader" },
                    onNavigateToGeneral = { currentRoute = "general" },
                    onNavigateToNetSpeed = { currentRoute = "netspeed" },
                    onNavigateToData = { currentRoute = "data" },
                    onNavigateToPages = { currentRoute = "pages" },
                    onNavigateToHandles = { currentRoute = "handles" },
                    onNavigateToCallRecorder = { currentRoute = "call_recorder" },
                    onNavigateToScreenCap = { currentRoute = "screencap" },
                    onBack = onFinish
                )"""

content = content.replace(old_main_call, new_main_call)

old_routes = """                "call_recorder" -> CallRecorderSettingsScreen(
                    onBack = { currentRoute = "main" }
                )"""

new_routes = """                "call_recorder" -> CallRecorderSettingsScreen(
                    onBack = { currentRoute = "main" }
                )
                "screencap" -> ScreenCapSettingsScreen(
                    onBack = { currentRoute = "main" }
                )"""

content = content.replace(old_routes, new_routes)

old_main_fun = """fun MainSettingsScreen(onNavigateToReader: () -> Unit, onNavigateToGeneral: () -> Unit, onNavigateToNetSpeed: () -> Unit, onNavigateToData: () -> Unit, onNavigateToPages: () -> Unit, onNavigateToHandles: () -> Unit, onNavigateToCallRecorder: () -> Unit, onBack: () -> Unit) {"""

new_main_fun = """fun MainSettingsScreen(onNavigateToReader: () -> Unit, onNavigateToGeneral: () -> Unit, onNavigateToNetSpeed: () -> Unit, onNavigateToData: () -> Unit, onNavigateToPages: () -> Unit, onNavigateToHandles: () -> Unit, onNavigateToCallRecorder: () -> Unit, onNavigateToScreenCap: () -> Unit, onBack: () -> Unit) {"""

content = content.replace(old_main_fun, new_main_fun)

old_list = """                ListItem(
                    headlineContent = { Text("Call Recorder Settings") },
                    supportingContent = { Text("Automatic call recording & privacy") },
                    modifier = Modifier.clickable { onNavigateToCallRecorder() }
                )
                Divider()"""

new_list = """                ListItem(
                    headlineContent = { Text("Call Recorder Settings") },
                    supportingContent = { Text("Automatic call recording & privacy") },
                    modifier = Modifier.clickable { onNavigateToCallRecorder() }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Screen Cap") },
                    supportingContent = { Text("Screenshot and screen recording location") },
                    modifier = Modifier.clickable { onNavigateToScreenCap() }
                )
                Divider()"""

content = content.replace(old_list, new_list)

with open('app/src/main/java/com/example/SettingsActivity.kt', 'w') as f:
    f.write(content)
