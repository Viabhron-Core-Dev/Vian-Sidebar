import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target_ui = """        val composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    AppyworkProjectSelector(
                        onProjectSelected = { project ->
                            // TODO: Phase 5 - Execute Git operations and apply blocks
                        }
                    )
                }
            }
        }"""

replacement_ui = """        val composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme {
                    AppyworkProjectSelector(
                        parsedBlocks = parsedBlocks,
                        onProjectSelected = { project ->
                            applyBlocksToProject(project)
                        }
                    )
                }
            }
        }"""

target_fun = """    fun close() {"""

replacement_fun = """    private fun applyBlocksToProject(project: AppyworkProject) {
        val fileSystem = com.example.data.AppyworkFileSystem(context)
        val dao = com.example.data.AppDatabase.getDatabase(context).appyworkDao()
        
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            for (block in parsedBlocks) {
                if (block.filePath != null && !block.isQuarantined) {
                    // Write to virtual file system
                    fileSystem.writeFile(project.id, block.filePath!!, block.code)
                    
                    // Update database node
                    var node = dao.getFileNode(project.id, block.filePath!!)
                    if (node == null) {
                        node = com.example.data.AppyworkFileNode(
                            projectId = project.id,
                            path = block.filePath!!,
                            localHash = block.code.hashCode().toString(),
                            syncState = "NEW"
                        )
                        dao.insertFileNode(node)
                    } else {
                        val updatedNode = node.copy(
                            localHash = block.code.hashCode().toString(),
                            syncState = "MODIFIED"
                        )
                        dao.updateFileNode(updatedNode)
                    }
                }
            }
            
            kotlinx.coroutines.Dispatchers.Main.dispatch(context.mainLooper) {
                android.widget.Toast.makeText(context, "Applied ${parsedBlocks.size} blocks to ${project.name}", android.widget.Toast.LENGTH_SHORT).show()
                close()
            }
        }
    }

    fun close() {"""


target_compose = """@Composable
fun AppyworkProjectSelector(onProjectSelected: (AppyworkProject) -> Unit) {"""

replacement_compose = """@Composable
fun AppyworkProjectSelector(parsedBlocks: List<ParsedCodeBlock>, onProjectSelected: (AppyworkProject) -> Unit) {"""

if target_ui in content:
    content = content.replace(target_ui, replacement_ui)
    content = content.replace(target_fun, replacement_fun)
    content = content.replace(target_compose, replacement_compose)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
