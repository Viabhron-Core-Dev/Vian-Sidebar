import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                                    androidx.compose.material3.DropdownMenuItem(
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
                                    )"""

replacement = """                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            scope.launch {
                                                if (file.syncState == "NEW") {
                                                    dao.deleteFileNode(file)
                                                } else {
                                                    dao.updateFileNode(file.copy(syncState = "DELETED"))
                                                }
                                                val fileSystem = com.example.data.AppyworkFileSystem(context)
                                                val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${file.path}")
                                                f.delete()
                                            }
                                            expandedMenu = false
                                        }
                                    )"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched single delete")
else:
    print("Target single delete not found")
    
target2 = """                    androidx.compose.material3.IconButton(onClick = { 
                        scope.launch {
                            val toDelete = files.filter { selectedFileIds.contains(it.id) }
                            for (file in toDelete) {
                                dao.deleteFileNode(file)
                                val fileSystem = com.example.data.AppyworkFileSystem(context)
                                val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${file.path}")
                                f.delete()
                            }
                            selectedFileIds = setOf()
                            multiSelectMode = false
                        }
                    })"""

replacement2 = """                    androidx.compose.material3.IconButton(onClick = { 
                        scope.launch {
                            val toDelete = files.filter { selectedFileIds.contains(it.id) }
                            for (file in toDelete) {
                                if (file.syncState == "NEW") {
                                    dao.deleteFileNode(file)
                                } else {
                                    dao.updateFileNode(file.copy(syncState = "DELETED"))
                                }
                                val fileSystem = com.example.data.AppyworkFileSystem(context)
                                val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${file.path}")
                                f.delete()
                            }
                            selectedFileIds = setOf()
                            multiSelectMode = false
                        }
                    })"""

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Patched bulk delete")
else:
    print("Target bulk delete not found")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)

