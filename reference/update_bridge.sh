#!/bin/bash
sed -i '/@JavascriptInterface/i\
    @JavascriptInterface\
    fun importFile() {\
        val intent = android.content.Intent(context, PwaFilePickerActivity::class.java)\
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)\
        context.startActivity(intent)\
    }\
' app/src/main/java/com/example/service/SidebarBridge.kt

sed -i '/class PwaWindowManager/i\
    companion object {\
        var pendingImportCallback: ((String) -> Unit)? = null\
    }\
' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i '/sidebarBridge = SidebarBridge(context)/a\
            pendingImportCallback = { content ->\
                android.os.Handler(android.os.Looper.getMainLooper()).post {\
                    webView.evaluateJavascript("if(window.onNativeFileImport) { window.onNativeFileImport(\\"$content\\"); }", null)\
                }\
            }\
' app/src/main/java/com/example/service/PwaWindowManager.kt
