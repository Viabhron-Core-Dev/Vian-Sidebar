#!/bin/bash
# 1. Fix Geolocation
sed -i 's/callback.invoke(origin, true, false)/callback.invoke(origin, true, true)/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# 2. Fix findFreePort
sed -i '/private fun findFreePort(): Int {/,/^    }/c\
    private fun findFreePort(): Int {\
        return try {\
            val socket = ServerSocket(0)\
            val freePort = socket.localPort\
            socket.close()\
            freePort\
        } catch (e: Exception) {\
            throw RuntimeException("Network Stack Error: No free ports available")\
        }\
    }\
' app/src/main/java/com/example/service/PwaWindowManager.kt

# 3. Fix PwaServer binding
sed -i 's/class PwaServer(port: Int, private val zipFilePath: String) : NanoHTTPD(port) {/class PwaServer(port: Int, private val zipFilePath: String) : NanoHTTPD("127.0.0.1", port) {/g' app/src/main/java/com/example/service/PwaServer.kt

# 4. Fix PwaServer OPTIONS
sed -i '/override fun serve(session: IHTTPSession): Response {/a\
        if (session.method == Method.OPTIONS) {\
            val response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "")\
            response.addHeader("Access-Control-Allow-Origin", "*")\
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")\
            response.addHeader("Access-Control-Allow-Headers", "*")\
            return response\
        }\
' app/src/main/java/com/example/service/PwaServer.kt

# 5. Fix SidebarBridge CoroutineScope
cat << 'INNER' > app/src/main/java/com/example/service/SidebarBridge.kt
package com.example.service

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class SidebarBridge(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun destroy() {
        scope.cancel()
    }

    @JavascriptInterface
    fun exportGeoJson(filename: String, data: String) {
        scope.launch {
            try {
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, filename)
                file.writeText(data)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported to Downloads: $filename", Toast.LENGTH_SHORT).show()
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

# 6. Destroy SidebarBridge in PwaWindowManager
sed -i '/private var port: Int = 0/a\
    private var sidebarBridge: SidebarBridge? = null\
' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i 's/addJavascriptInterface(SidebarBridge(context), "SidebarNative")/sidebarBridge = SidebarBridge(context)\n            addJavascriptInterface(sidebarBridge!!, "SidebarNative")/g' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i '/pwaServer?.stop()/i\
        sidebarBridge?.destroy()\
        sidebarBridge = null\
' app/src/main/java/com/example/service/PwaWindowManager.kt

