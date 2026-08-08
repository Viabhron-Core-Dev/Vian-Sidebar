import re

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'r') as f:
    content = f.read()

target = """                                pushStatus = "Uploading ${unsyncedFiles.size} files..."
                                val blobMap = mutableListOf<Pair<String, String>>()
                                for (file in unsyncedFiles) {
                                    val content = fileSystem.readFile(project.id, file.path) ?: continue
                                    val blobSha = com.example.utils.GitHubApiClient.createBlob(project.remoteUrl, token, content)
                                    blobMap.add(Pair(file.path, blobSha))
                                }"""

replacement = """                                pushStatus = "Uploading ${unsyncedFiles.size} files..."
                                val blobMap = mutableListOf<Pair<String, String?>>()
                                for (file in unsyncedFiles) {
                                    if (file.syncState == "DELETED") {
                                        blobMap.add(Pair(file.path, null))
                                    } else {
                                        val content = fileSystem.readFile(project.id, file.path) ?: continue
                                        val blobSha = com.example.utils.GitHubApiClient.createBlob(project.remoteUrl, token, content)
                                        blobMap.add(Pair(file.path, blobSha))
                                    }
                                }"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched push 1")
else:
    print("Target push 1 not found")

target2 = """                                pushStatus = "Syncing local DB..."
                                for (file in unsyncedFiles) {
                                    dao.updateFileNode(file.copy(syncState = "SYNCED"))
                                }"""

replacement2 = """                                pushStatus = "Syncing local DB..."
                                for (file in unsyncedFiles) {
                                    if (file.syncState == "DELETED") {
                                        dao.deleteFileNode(file)
                                    } else {
                                        dao.updateFileNode(file.copy(syncState = "SYNCED"))
                                    }
                                }"""

if target2 in content:
    content = content.replace(target2, replacement2)
    print("Patched push 2")
else:
    print("Target push 2 not found")

with open('app/src/main/java/com/example/service/AppyworkWindowManager.kt', 'w') as f:
    f.write(content)

