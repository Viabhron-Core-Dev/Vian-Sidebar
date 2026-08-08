import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                androidx.compose.material3.IconButton(onClick = { multiSelectMode = !multiSelectMode }) {
                    androidx.compose.material3.Icon(if (multiSelectMode) androidx.compose.material.icons.Icons.Default.Checklist else androidx.compose.material.icons.Icons.AutoMirrored.Filled.List, contentDescription = "Multi-select")
                }
            }"""

replacement = """                androidx.compose.material3.IconButton(onClick = { multiSelectMode = !multiSelectMode }) {
                    androidx.compose.material3.Icon(if (multiSelectMode) androidx.compose.material.icons.Icons.Default.Checklist else androidx.compose.material.icons.Icons.AutoMirrored.Filled.List, contentDescription = "Multi-select")
                }
                var showCreateDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                androidx.compose.material3.IconButton(onClick = { showCreateDialog = true }) {
                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Create file")
                }
                
                if (showCreateDialog) {
                    var newFileName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showCreateDialog = false },
                        title = { Text("Create File") },
                        text = {
                            OutlinedTextField(
                                value = newFileName,
                                onValueChange = { newFileName = it },
                                label = { Text("File path (e.g. src/main.js)") }
                            )
                        },
                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                scope.launch {
                                    val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/$newFileName")
                                    f.parentFile?.mkdirs()
                                    if (!f.exists()) {
                                        f.createNewFile()
                                        val node = com.example.data.AppyworkFileNode(
                                            projectId = project.id,
                                            path = newFileName,
                                            localHash = "", // empty hash initially
                                            syncState = "NEW"
                                        )
                                        dao.insertFileNode(node)
                                    }
                                }
                                showCreateDialog = false
                            }) { Text("Create") }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched create successfully")
else:
    print("Target create not found")

