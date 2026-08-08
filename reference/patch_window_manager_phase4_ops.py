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
                                    )
                                    // Could add more here: Move, Download"""

replacement = """                                    androidx.compose.material3.DropdownMenuItem(
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
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Download (.txt)") },
                                        onClick = {
                                            scope.launch {
                                                val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${file.path}")
                                                if (f.exists()) {
                                                    val downloads = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                                                    val dest = java.io.File(downloads, "${file.path.replace('/', '_')}.txt")
                                                    f.copyTo(dest, overwrite = true)
                                                }
                                            }
                                            expandedMenu = false
                                        }
                                    )
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text("Copy content") },
                                        onClick = {
                                            val f = java.io.File(context.filesDir, "appywork_projects/${project.id}/${file.path}")
                                            if (f.exists()) {
                                                val text = f.readText()
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Appywork file", text))
                                            }
                                            expandedMenu = false
                                        }
                                    )"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched operations successfully")
else:
    print("Target operations not found")

