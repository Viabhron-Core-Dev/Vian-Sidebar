import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target_compose = """@Composable
fun AppyworkProjectSelector(parsedBlocks: List<ParsedCodeBlock>, onProjectSelected: (AppyworkProject) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).appyworkDao() }
    val projects by dao.getAllProjectsFlow().collectAsState(initial = emptyList())
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Parsed Blocks: ${parsedBlocks.size}", style = MaterialTheme.typography.titleMedium)
        val validBlocks = parsedBlocks.count { !it.isQuarantined && it.filePath != null }
        Text("Valid: $validBlocks | Quarantined: ${parsedBlocks.size - validBlocks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))

        Text("Select Project to Apply Changes:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        
        if (projects.isEmpty()) {
            Text("No projects available. Please configure one in Settings.", color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(projects) { project ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onProjectSelected(project) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(project.name, style = MaterialTheme.typography.bodyLarge)
                            Text(project.remoteUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}"""

replacement_compose = """@Composable
fun AppyworkProjectSelector(parsedBlocks: List<ParsedCodeBlock>, onProjectSelected: (AppyworkProject) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).appyworkDao() }
    val projects by dao.getAllProjectsFlow().collectAsState(initial = emptyList())
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    
    var selectedProject by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<AppyworkProject?>(null) }
    var pushStatus by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var isPushing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (selectedProject == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (parsedBlocks.isNotEmpty()) {
                Text("Parsed Blocks: ${parsedBlocks.size}", style = MaterialTheme.typography.titleMedium)
                val validBlocks = parsedBlocks.count { !it.isQuarantined && it.filePath != null }
                Text("Valid: $validBlocks | Quarantined: ${parsedBlocks.size - validBlocks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            }

            Text("Select Project:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
            
            if (projects.isEmpty()) {
                Text("No projects available. Please configure one in Settings.", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(projects) { project ->
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedProject = project }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(project.name, style = MaterialTheme.typography.bodyLarge)
                                Text(project.remoteUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    } else {
        val project = selectedProject!!
        val files by dao.getFilesForProjectFlow(project.id).collectAsState(initial = emptyList())
        val unsyncedFiles = files.filter { it.syncState != "SYNCED" }
        
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                androidx.compose.material3.IconButton(onClick = { selectedProject = null; pushStatus = "" }) {
                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(project.name, style = MaterialTheme.typography.titleMedium)
            }
            
            Text(project.remoteUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp, bottom = 16.dp))
            
            if (parsedBlocks.isNotEmpty()) {
                androidx.compose.material3.Button(
                    onClick = { onProjectSelected(project) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Apply ${parsedBlocks.size} Blocks to Local FS")
                }
            }
            
            Text("Unsynced Files: ${unsyncedFiles.size}", modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp))
            
            if (unsyncedFiles.isNotEmpty()) {
                androidx.compose.material3.Button(
                    onClick = {
                        isPushing = true
                        pushStatus = "Starting push..."
                        scope.launch {
                            try {
                                val fileSystem = com.example.data.AppyworkFileSystem(context)
                                val token = project.authToken
                                
                                pushStatus = "Fetching latest commit..."
                                val latestCommitSha = com.example.utils.GitHubApiClient.getLatestCommitSha(project.remoteUrl, token)
                                
                                pushStatus = "Uploading ${unsyncedFiles.size} files..."
                                val blobMap = mutableListOf<Pair<String, String>>()
                                for (file in unsyncedFiles) {
                                    val content = fileSystem.readFile(project.id, file.path) ?: continue
                                    val blobSha = com.example.utils.GitHubApiClient.createBlob(project.remoteUrl, token, content)
                                    blobMap.add(Pair(file.path, blobSha))
                                }
                                
                                pushStatus = "Creating tree..."
                                val newTreeSha = com.example.utils.GitHubApiClient.createTree(project.remoteUrl, token, latestCommitSha, blobMap)
                                
                                pushStatus = "Creating commit..."
                                val commitMsg = "Appywork Vibe Coding - Auto commit"
                                val newCommitSha = com.example.utils.GitHubApiClient.createCommit(project.remoteUrl, token, commitMsg, newTreeSha, latestCommitSha)
                                
                                pushStatus = "Updating ref..."
                                com.example.utils.GitHubApiClient.updateRef(project.remoteUrl, token, "main", newCommitSha) // default to main
                                
                                pushStatus = "Syncing local DB..."
                                for (file in unsyncedFiles) {
                                    dao.updateFileNode(file.copy(syncState = "SYNCED"))
                                }
                                
                                pushStatus = "Push Successful!"
                            } catch (e: Exception) {
                                pushStatus = "Error: ${e.message}"
                            } finally {
                                isPushing = false
                            }
                        }
                    },
                    enabled = !isPushing,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(if (isPushing) "Pushing..." else "Push to GitHub")
                }
            }
            
            if (pushStatus.isNotBlank()) {
                Text(pushStatus, color = if (pushStatus.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
        }
    }
}"""

if target_compose in content:
    content = content.replace(target_compose, replacement_compose)
    if "import androidx.compose.material.icons.filled.ArrowBack" not in content:
        content = content.replace("import androidx.compose.material3.Text", "import androidx.compose.material3.Text\nimport androidx.compose.material.icons.filled.ArrowBack")
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched UI successfully")
else:
    print("Target UI not found")
