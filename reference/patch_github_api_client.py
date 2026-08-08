import re

with open('app/src/main/java/com/example/utils/GitHubApiClient.kt', 'r') as f:
    content = f.read()

target = """    suspend fun createTree(repoUrl: String, token: String, baseTreeSha: String, files: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        val repoPath = extractRepoPath(repoUrl)
        val url = "https://api.github.com/repos/$repoPath/git/trees"
        val conn = getConnection(url, token, "POST")
        conn.doOutput = true
        
        val treeArray = JSONArray()
        for ((path, blobSha) in files) {
            val item = JSONObject().apply {
                put("path", path)
                put("mode", "100644")
                put("type", "blob")
                put("sha", blobSha)
            }
            treeArray.put(item)
        }"""

replacement = """    suspend fun createTree(repoUrl: String, token: String, baseTreeSha: String, files: List<Pair<String, String?>>): String = withContext(Dispatchers.IO) {
        val repoPath = extractRepoPath(repoUrl)
        val url = "https://api.github.com/repos/$repoPath/git/trees"
        val conn = getConnection(url, token, "POST")
        conn.doOutput = true
        
        val treeArray = JSONArray()
        for ((path, blobSha) in files) {
            val item = JSONObject().apply {
                put("path", path)
                put("mode", "100644")
                put("type", "blob")
                if (blobSha == null) {
                    put("sha", JSONObject.NULL)
                } else {
                    put("sha", blobSha)
                }
            }
            treeArray.put(item)
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/utils/GitHubApiClient.kt', 'w') as f:
        f.write(content)
    print("Patched GitHubApiClient successfully")
else:
    print("Target GitHubApiClient not found")
