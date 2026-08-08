#!/bin/bash

# Update WebChromeClient
sed -i '/webChromeClient = object : WebChromeClient() {/a\
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {\
                    if (consoleMessage?.messageLevel() == android.webkit.ConsoleMessage.MessageLevel.ERROR) {\
                        com.example.LogKeeper.writeLog(\
                            "PwaWebView",\
                            "JS ERROR: ${consoleMessage.message()} at ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}"\
                        )\
                    }\
                    return super.onConsoleMessage(consoleMessage)\
                }\
' app/src/main/java/com/example/service/PwaWindowManager.kt

# Update SidebarBridge instantiation to include error callback
sed -i 's/sidebarBridge = SidebarBridge(context, callbackId)/sidebarBridge = SidebarBridge(context, callbackId) { errorMsg ->\n                android.os.Handler(android.os.Looper.getMainLooper()).post {\n                    webView.evaluateJavascript("if(window.onNativeExportError) { window.onNativeExportError(\\\"$errorMsg\\\"); } else { console.error(\\\"Native Error: $errorMsg\\\"); }", null)\n                }\n            }/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# Update WebViewClient
sed -i '/webViewClient = object : WebViewClient() {/a\
                override fun onReceivedError(\
                    view: WebView?,\
                    request: android.webkit.WebResourceRequest?,\
                    error: android.webkit.WebResourceError?\
                ) {\
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {\
                        com.example.LogKeeper.writeLog(\
                            "PwaWebView",\
                            "Network Error: ${error?.errorCode} - ${error?.description} for URL: ${request?.url}"\
                        )\
                    }\
                    super.onReceivedError(view, request, error)\
                }\
\
                override fun onReceivedHttpError(\
                    view: WebView?,\
                    request: android.webkit.WebResourceRequest?,\
                    errorResponse: android.webkit.WebResourceResponse?\
                ) {\
                    com.example.LogKeeper.writeLog(\
                        "PwaWebView",\
                        "HTTP Error: ${errorResponse?.statusCode} - ${errorResponse?.reasonPhrase} for URL: ${request?.url}"\
                    )\
                    super.onReceivedHttpError(view, request, errorResponse)\
                }\
\
                override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {\
                    com.example.LogKeeper.writeLog(\
                        "PwaWebView",\
                        "RENDER_PROCESS_GONE: WebGL crash detected. Did crash? ${detail?.didCrash()}"\
                    )\
                    android.os.Handler(android.os.Looper.getMainLooper()).post {\
                        android.widget.Toast.makeText(context, "Map Engine Recovering...", android.widget.Toast.LENGTH_LONG).show()\
                        view?.reload()\
                    }\
                    return true\
                }\
' app/src/main/java/com/example/service/PwaWindowManager.kt

