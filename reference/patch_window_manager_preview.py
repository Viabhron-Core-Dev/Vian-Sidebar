import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                androidx.compose.material3.Button(
                    onClick = {
                        isPushing = true"""

replacement = """                androidx.compose.material3.Button(
                    onClick = {
                        scope.launch {
                            try {
                                val pwaDb = com.example.service.PwaDatabase.getDatabase(context)
                                var pwas = pwaDb.pwaDao().getAllPwasSync()
                                var existing = pwas.find { it.name == "Appywork: ${project.name}" }
                                
                                val projectDir = java.io.File(context.filesDir, "appywork_projects/${project.id}")
                                val zipFile = java.io.File(context.filesDir, "appywork_${project.id}.zip")
                                
                                if (zipFile.exists()) zipFile.delete()
                                
                                java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zos ->
                                    projectDir.walkTopDown().forEach { file ->
                                        if (file.isFile) {
                                            val entryName = file.absolutePath.removePrefix(projectDir.absolutePath + "/")
                                            zos.putNextEntry(java.util.zip.ZipEntry(entryName))
                                            file.inputStream().use { it.copyTo(zos) }
                                            zos.closeEntry()
                                        }
                                    }
                                }
                                
                                val newEntry = existing?.copy(zipPath = zipFile.absolutePath) ?: com.example.service.PwaEntry(
                                    name = "Appywork: ${project.name}",
                                    zipPath = zipFile.absolutePath,
                                    isLightweight = true,
                                    useVirtualHost = true
                                )
                                
                                if (existing == null) {
                                    pwaDb.pwaDao().insertPwa(newEntry)
                                    existing = pwaDb.pwaDao().getAllPwasSync().find { it.name == "Appywork: ${project.name}" }
                                } else {
                                    pwaDb.pwaDao().updatePwa(newEntry)
                                }
                                
                                if (existing != null) {
                                    val intent = android.content.Intent(context, com.example.service.SidebarService::class.java).apply {
                                        action = "EXECUTE_ACTION"
                                        putExtra("ACTION_ID", "pwa:${existing.id}")
                                    }
                                    context.startService(intent)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Live Preview (PWA)")
                }
                
                androidx.compose.material3.Button(
                    onClick = {
                        isPushing = true"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
        f.write(content)
    print("Patched Preview successfully")
else:
    print("Target Preview not found")
