package com.example.utils

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object GitHubApiClient {

    private fun getConnection(urlStr: String, token: String, method: String): HttpURLConnection {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("Authorization", "token $token")
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
        conn.setRequestProperty("Content-Type", "application/json")
        return conn
    }

    private fun readResponse(conn: HttpURLConnection): JSONObject {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use { it.readText() }
        if (conn.responseCode !in 200..299) {
            throw Exception("GitHub API Error (${conn.responseCode}): $response")
        }
        return JSONObject(response)
    }

    suspend fun getLatestCommitSha(repoUrl: String, token: String, branch: String = "main"): String = withContext(Dispatchers.IO) {
        val repoPath = extractRepoPath(repoUrl)
        val url = "https://api.github.com/repos/$repoPath/git/ref/heads/$branch"
        val conn = getConnection(url, token, "GET")
        val json = readResponse(conn)
        json.getJSONObject("object").getString("sha")
    }

    suspend fun createBlob(repoUrl: String, token: String, content: String): String = withContext(Dispatchers.IO) {
        val repoPath = extractRepoPath(repoUrl)
        val url = "https://api.github.com/repos/$repoPath/git/blobs"
        val conn = getConnection(url, token, "POST")
        conn.doOutput = true
        
        val payload = JSONObject().apply {
            put("content", Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
            put("encoding", "base64")
        }
        
        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        val json = readResponse(conn)
        json.getString("sha")
    }

    suspend fun createTree(repoUrl: String, token: String, baseTreeSha: String, files: List<Pair<String, String?>>): String = withContext(Dispatchers.IO) {
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
        }
        
        val payload = JSONObject().apply {
            put("base_tree", baseTreeSha)
            put("tree", treeArray)
        }
        
        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        val json = readResponse(conn)
        json.getString("sha")
    }

    suspend fun createCommit(repoUrl: String, token: String, message: String, treeSha: String, parentSha: String): String = withContext(Dispatchers.IO) {
        val repoPath = extractRepoPath(repoUrl)
        val url = "https://api.github.com/repos/$repoPath/git/commits"
        val conn = getConnection(url, token, "POST")
        conn.doOutput = true
        
        val payload = JSONObject().apply {
            put("message", message)
            put("tree", treeSha)
            put("parents", JSONArray().apply { put(parentSha) })
        }
        
        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        val json = readResponse(conn)
        json.getString("sha")
    }

    suspend fun updateRef(repoUrl: String, token: String, branch: String, newCommitSha: String): String = withContext(Dispatchers.IO) {
        val repoPath = extractRepoPath(repoUrl)
        val url = "https://api.github.com/repos/$repoPath/git/refs/heads/$branch"
        val conn = getConnection(url, token, "PATCH")
        conn.doOutput = true
        
        val payload = JSONObject().apply {
            put("sha", newCommitSha)
            put("force", false)
        }
        
        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        val json = readResponse(conn)
        json.getString("sha")
    }

    private fun extractRepoPath(url: String): String {
        var cleanUrl = url.replace("https://github.com/", "")
                          .replace("http://github.com/", "")
                          .replace("github.com/", "")
                          .removeSuffix("/")
                          .removeSuffix(".git")
        val parts = cleanUrl.split("/")
        if (parts.size >= 2) {
            return "${parts[0]}/${parts[1]}"
        }
        return cleanUrl
    }
}
