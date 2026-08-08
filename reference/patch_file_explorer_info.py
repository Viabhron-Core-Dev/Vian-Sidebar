import os

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'r') as f:
    content = f.read()

target = """                    if (selectedFiles.size == 1) {
                        IconButton(onClick = { showRenameDialog = selectedFiles.first() }) { Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.LightGray) }
                    }"""

replacement = """                    if (selectedFiles.size == 1) {
                        IconButton(onClick = { showRenameDialog = selectedFiles.first() }) { Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.LightGray) }
                        IconButton(onClick = { showInfoDialog = selectedFiles.first() }) { Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.LightGray) }
                    }"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Target not found for info button")

target_dialog = """        if (showRenameDialog != null) {"""
replacement_dialog = """        if (showInfoDialog != null) {
            val file = showInfoDialog!!
            AlertDialog(
                onDismissRequest = { showInfoDialog = null },
                title = { Text("Properties") },
                text = {
                    Column {
                        Text("Name: ${file.name}")
                        Text("Path: ${file.absolutePath}")
                        Text("Size: ${if (file.isDirectory) "Directory" else file.length().toString() + " bytes"}")
                        Text("Modified: ${SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()).format(Date(file.lastModified()))}")
                        Text("Readable: ${file.canRead()}")
                        Text("Writable: ${file.canWrite()}")
                    }
                },
                confirmButton = { TextButton(onClick = { showInfoDialog = null }) { Text("OK") } }
            )
        }
        
        if (showRenameDialog != null) {"""

if target_dialog in content:
    content = content.replace(target_dialog, replacement_dialog)
else:
    print("Target not found for info dialog")

target_state = """    var showRenameDialog by remember { mutableStateOf<File?>(null) }"""
replacement_state = """    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var showInfoDialog by remember { mutableStateOf<File?>(null) }"""

if target_state in content:
    content = content.replace(target_state, replacement_state)
else:
    print("Target state not found")

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'w') as f:
    f.write(content)

