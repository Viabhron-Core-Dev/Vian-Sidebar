#!/bin/bash
cat << 'INNER' > app/src/main/java/com/example/service/SidebarBridge.kt
package com.example.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

class SidebarBridge(private val context: Context, private val callbackId: Int = -1) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun destroy() {
        scope.cancel()
    }

    @JavascriptInterface
    fun clearImportCache() {
        try {
            val importsDir = File(context.filesDir, "pwa_imports")
            if (importsDir.exists() && importsDir.isDirectory) {
                importsDir.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun importFile() {
        val intent = android.content.Intent(context, PwaFilePickerActivity::class.java)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("callbackId", callbackId)
        context.startActivity(intent)
    }

    @JavascriptInterface
    fun exportGeoJson(filename: String, data: String) {
        scope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, filename)
                        put(MediaStore.Downloads.MIME_TYPE, "application/geo+json")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(data.toByteArray())
                        }
                        
                        contentValues.clear()
                        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Exported to Downloads: $filename", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, filename)
                    file.writeText(data)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Exported to Downloads: $filename", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    @JavascriptInterface
    fun requestNativeSensors() {
        // Implementation to trigger Android SensorManager if Web API fails
    }
}
INNER
