import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """            Text("Unsynced Files: ${unsyncedFiles.size}", modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp))"""

replacement = """            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search files") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    singleLine = true
                )
                androidx.compose.material3.IconButton(onClick = { multiSelectMode = !multiSelectMode }) {
                    androidx.compose.material3.Icon(if (multiSelectMode) androidx.compose.material.icons.Icons.Default.Checklist else androidx.compose.material.icons.Icons.Default.List, contentDescription = "Multi-select")
                }
            }
            
            Text("Unsynced Files: ${unsyncedFiles.size}", modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp))
            
            val filteredFiles = files.filter { it.path.contains(searchQuery, ignoreCase = true) }.sortedBy { it.path }
            
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(filteredFiles) { file ->
                    var expandedMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clickable {
                            if (multiSelectMode) {
                                selectedFileIds = if (selectedFileIds.contains(file.id)) {
                                    selectedFileIds - file.id
                                } else {
                                    selectedFileIds + file.id
                                }
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedFileIds.contains(file.id)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (multiSelectMode) {
                                androidx.compose.material3.Checkbox(
                                    checked = selectedFileIds.contains(file.id),
                                    onCheckedChange = { checked ->
                                        selectedFileIds = if (checked) selectedFileIds + file.id else selectedFileIds - file.id
                                    }
                                )
                            }
                            Text(file.path, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            
                            Box {
                                androidx.compose.material3.IconButton(onClick = { expandedMenu = true }) {
                                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.MoreVert, contentDescription = "Options")
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = expandedMenu,
                                    onDismissRequest = { expandedMenu = false }
                                ) {
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            scope.launch {
                                                dao.deleteFileNode(file)
                                                val fileSystem = com.example.data.AppyworkFileSystem(context)
                                                val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${file.path}")
                                                f.delete()
                                            }
                                            expandedMenu = false
                                        }
                                    )
                                    // Could add more here: Move, Download
                                }
                            }
                        }
                    }
                }
            }"""

if target in content:
    content = content.replace(target, replacement)
    
    # Add imports if missing
    if "import androidx.compose.material.icons.filled.List" not in content:
        content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.List\nimport androidx.compose.material.icons.filled.Checklist\nimport androidx.compose.material.icons.filled.MoreVert\nimport androidx.compose.material3.CardDefaults")
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched file list successfully")
else:
    print("Target file list not found")
