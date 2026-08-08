import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                        confirmButton = {
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
                        },"""

replacement = """                        confirmButton = {
                            androidx.compose.material3.TextButton(onClick = {
                                scope.launch {
                                    if (newFileName.isNotBlank()) {
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
                                }
                                showCreateDialog = false
                            }) { Text("Create") }
                        },"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched create button")
else:
    print("Target create button not found")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)

