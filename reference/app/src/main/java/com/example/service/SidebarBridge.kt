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
import java.io.FileNotFoundException

class SidebarBridge(private val context: Context, private val callbackId: Int = -1, private val errorCallback: ((String) -> Unit)? = null) {
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
        } catch (e: SecurityException) {
            com.example.LogKeeper.writeLog("SidebarBridge", "SecurityException - clearImportCache: ${e.message}\n${android.util.Log.getStackTraceString(e)}")
            errorCallback?.invoke("SecurityException during clearImportCache: ${e.message}")
        } catch (e: FileNotFoundException) {
            com.example.LogKeeper.writeLog("SidebarBridge", "FileNotFoundException - clearImportCache: ${e.message}\n${android.util.Log.getStackTraceString(e)}")
            errorCallback?.invoke("FileNotFoundException during clearImportCache: ${e.message}")
        } catch (e: Exception) {
            com.example.LogKeeper.writeLog("SidebarBridge", "Exception - clearImportCache: ${e.message}\n${android.util.Log.getStackTraceString(e)}")
            errorCallback?.invoke("Exception during clearImportCache: ${e.message}")
        }
    }

    @JavascriptInterface
    fun importFile() {
        try {
            val intent = android.content.Intent(context, PwaFilePickerActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("callbackId", callbackId)
            context.startActivity(intent)
        } catch (e: SecurityException) {
            com.example.LogKeeper.writeLog("SidebarBridge", "SecurityException - importFile: ${e.message}\n${android.util.Log.getStackTraceString(e)}")
            errorCallback?.invoke("SecurityException during importFile: ${e.message}")
        } catch (e: FileNotFoundException) {
            com.example.LogKeeper.writeLog("SidebarBridge", "FileNotFoundException - importFile: ${e.message}\n${android.util.Log.getStackTraceString(e)}")
            errorCallback?.invoke("FileNotFoundException during importFile: ${e.message}")
        } catch (e: Exception) {
            com.example.LogKeeper.writeLog("SidebarBridge", "Exception - importFile: ${e.message}\n${android.util.Log.getStackTraceString(e)}")
            errorCallback?.invoke("Exception during importFile: ${e.message}")
        }
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
                    } else {
                         throw FileNotFoundException("Could not create MediaStore entry for $filename")
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, filename)
                    file.writeText(data)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Exported to Downloads: $filename", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                com.example.LogKeeper.writeLog("SidebarBridge", "SecurityException - exportGeoJson ($filename): ${e.message}\n${android.util.Log.getStackTraceString(e)}")
                withContext(Dispatchers.Main) {
                    errorCallback?.invoke("SecurityException during exportGeoJson: ${e.message}")
                }
            } catch (e: FileNotFoundException) {
                com.example.LogKeeper.writeLog("SidebarBridge", "FileNotFoundException - exportGeoJson ($filename): ${e.message}\n${android.util.Log.getStackTraceString(e)}")
                withContext(Dispatchers.Main) {
                    errorCallback?.invoke("FileNotFoundException during exportGeoJson: ${e.message}")
                }
            } catch (e: Exception) {
                com.example.LogKeeper.writeLog("SidebarBridge", "Exception - exportGeoJson ($filename): ${e.message}\n${android.util.Log.getStackTraceString(e)}")
                withContext(Dispatchers.Main) {
                    errorCallback?.invoke("Exception during exportGeoJson: ${e.message}")
                }
            }
        }
    }
    
    @JavascriptInterface
    fun requestNativeSensors() {
        // Implementation to trigger Android SensorManager if Web API fails
    }
}
