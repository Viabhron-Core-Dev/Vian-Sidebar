import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """    if (selectedProject == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (parsedBlocks.isNotEmpty()) {
                Text("Parsed Blocks: ${parsedBlocks.size}", style = MaterialTheme.typography.titleMedium)
                val validBlocks = parsedBlocks.count { !it.isQuarantined && it.filePath != null }
                Text("Valid: $validBlocks | Quarantined: ${parsedBlocks.size - validBlocks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            }"""

replacement = """    var currentBlocks by androidx.compose.runtime.remember(parsedBlocks) { androidx.compose.runtime.mutableStateOf(parsedBlocks) }
    
    if (selectedProject == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (currentBlocks.isNotEmpty()) {
                Text("Parsed Blocks: ${currentBlocks.size}", style = MaterialTheme.typography.titleMedium)
                val validBlocks = currentBlocks.count { !it.isQuarantined && it.filePath != null }
                Text("Valid: $validBlocks | Quarantined: ${currentBlocks.size - validBlocks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched part 1")
else:
    print("Part 1 not found")

target2 = """            if (parsedBlocks.isNotEmpty()) {
                androidx.compose.material3.Button(
                    onClick = { onProjectSelected(project) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Apply ${parsedBlocks.size} Blocks to Local FS")
                }
            }"""

replacement2 = """            if (currentBlocks.isNotEmpty()) {
                val quarantined = currentBlocks.filter { it.isQuarantined }
                if (quarantined.isNotEmpty()) {
                    Text("Quarantine Resolution (${quarantined.size} remaining)", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
                    val block = quarantined.first()
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Missing file path for code block:", style = MaterialTheme.typography.bodySmall)
                            Text(block.code.take(100) + if (block.code.length > 100) "..." else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            var manualPath by androidx.compose.runtime.remember(block.id) { androidx.compose.runtime.mutableStateOf("") }
                            OutlinedTextField(
                                value = manualPath,
                                onValueChange = { manualPath = it },
                                label = { Text("Enter file path") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                            
                            Row(modifier = Modifier.padding(top = 8.dp)) {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        currentBlocks = currentBlocks.filter { it.id != block.id }
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Discard")
                                }
                                androidx.compose.material3.Button(
                                    onClick = {
                                        if (manualPath.isNotBlank()) {
                                            val updatedBlock = block.copy(filePath = manualPath, isQuarantined = false)
                                            currentBlocks = currentBlocks.map { if (it.id == block.id) updatedBlock else it }
                                        }
                                    },
                                    modifier = Modifier.padding(start = 8.dp),
                                    enabled = manualPath.isNotBlank()
                                ) {
                                    Text("Assign & Keep")
                                }
                            }
                        }
                    }
                } else {
                    Text("Ready to Apply: ${currentBlocks.size} Blocks", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp).fillMaxWidth().padding(horizontal = 12.dp)) {
                        items(currentBlocks) { b ->
                            Text(b.filePath ?: "Unknown", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    androidx.compose.material3.Button(
                        onClick = { 
                            scope.launch {
                                val fileSystem = com.example.data.AppyworkFileSystem(context)
                                for (block in currentBlocks) {
                                    if (block.filePath == null) continue
                                    
                                    val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${block.filePath}")
                                    f.parentFile?.mkdirs()
                                    if (!f.exists()) f.createNewFile()
                                    
                                    fileSystem.writeFile(project.id, block.filePath!!, block.code)
                                    
                                    var node = dao.getFileNode(project.id, block.filePath!!)
                                    if (node == null) {
                                        node = com.example.data.AppyworkFileNode(
                                            projectId = project.id,
                                            path = block.filePath!!,
                                            localHash = "",
                                            syncState = "NEW"
                                        )
                                        dao.insertFileNode(node)
                                    } else {
                                        val updatedNode = node.copy(syncState = "MODIFIED")
                                        dao.updateFileNode(updatedNode)
                                    }
                                }
                                currentBlocks = emptyList() // clear after applying
                                // also trigger refresh of files...
                                pushStatus = "Applied to local filesystem!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Apply ${currentBlocks.size} Blocks to Local FS")
                    }
                }
            }"""

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Patched part 2")
else:
    print("Part 2 not found")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)

