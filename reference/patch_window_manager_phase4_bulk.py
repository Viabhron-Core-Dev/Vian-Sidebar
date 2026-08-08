import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                androidx.compose.material3.IconButton(onClick = { multiSelectMode = !multiSelectMode }) {
                    androidx.compose.material3.Icon(if (multiSelectMode) androidx.compose.material.icons.Icons.Default.Checklist else androidx.compose.material.icons.Icons.AutoMirrored.Filled.List, contentDescription = "Multi-select")
                }"""

replacement = """                androidx.compose.material3.IconButton(onClick = { multiSelectMode = !multiSelectMode }) {
                    androidx.compose.material3.Icon(if (multiSelectMode) androidx.compose.material.icons.Icons.Default.Checklist else androidx.compose.material.icons.Icons.AutoMirrored.Filled.List, contentDescription = "Multi-select")
                }
                if (multiSelectMode && selectedFileIds.isNotEmpty()) {
                    androidx.compose.material3.IconButton(onClick = { 
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
                    }) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete selected")
                    }
                }"""

if target in content:
    content = content.replace(target, replacement)
    
    if "import androidx.compose.material.icons.filled.Delete" not in content:
        content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Delete")
        
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched bulk delete successfully")
else:
    print("Target bulk delete not found")
