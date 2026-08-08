#!/bin/bash
cat << 'INNER' > app/src/main/java/com/example/service/PwaFilePickerActivity.kt
package com.example.service

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PwaFilePickerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        val callbackId = getIntent().getIntExtra("callbackId", -1)
        intent.putExtra("callbackId", callbackId)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val callbackId = intent.getIntExtra("callbackId", -1)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                copyAndSendToPwa(uri, callbackId)
            } ?: finish()
        } else {
            finish()
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result ?: "imported_file"
    }

    private fun copyAndSendToPwa(uri: Uri, callbackId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val importsDir = File(filesDir, "pwa_imports")
                if (!importsDir.exists()) {
                    importsDir.mkdirs()
                }
                val fileName = getFileName(uri)
                val destFile = File(importsDir, fileName)
                
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                val relativePath = "/pwa_imports/$fileName"
                
                withContext(Dispatchers.Main) {
                    if (callbackId != -1) {
                        PwaWindowManager.pendingImportCallbacks[callbackId]?.invoke(relativePath)
                        PwaWindowManager.pendingImportCallbacks.remove(callbackId)
                    }
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }
}
INNER
