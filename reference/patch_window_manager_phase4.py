import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

# We need to replace the `} else {` block in AppyworkProjectSelector with something that includes the File Tree

target = """    } else {
        val project = selectedProject!!
        val files by dao.getFilesForProjectFlow(project.id).collectAsState(initial = emptyList())
        val unsyncedFiles = files.filter { it.syncState != "SYNCED" }"""

replacement = """    } else {
        val project = selectedProject!!
        val files by dao.getFilesForProjectFlow(project.id).collectAsState(initial = emptyList())
        val unsyncedFiles = files.filter { it.syncState != "SYNCED" }
        
        var searchQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
        var multiSelectMode by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        var selectedFileIds by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(setOf<Int>()) }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched variables successfully")
else:
    print("Target variables not found")

