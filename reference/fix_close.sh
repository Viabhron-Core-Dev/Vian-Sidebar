#!/bin/bash
sed -i '/fun close() {/a\
        floatingView?.findViewById<WebView>(R.id.webview)?.removeJavascriptInterface("SidebarNative")\
' app/src/main/java/com/example/service/PwaWindowManager.kt
