#!/bin/bash
sed -i '/webView.apply {/,/loadUrl/c\
        webView.apply {\
            settings.javaScriptEnabled = true\
            settings.domStorageEnabled = true\
            settings.allowFileAccess = true\
            settings.allowContentAccess = true\
            settings.setGeolocationEnabled(true)\
            webChromeClient = object : WebChromeClient() {\
                override fun onGeolocationPermissionsShowPrompt(origin: String, callback: android.webkit.GeolocationPermissions.Callback) {\
                    callback.invoke(origin, true, false)\
                }\
            }\
            addJavascriptInterface(SidebarBridge(context), "SidebarNative")\
            webViewClient = object : WebViewClient() {\
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {\
                    return false\
                }\
            }\
            loadUrl("http://localhost:$port/")\
' app/src/main/java/com/example/service/PwaWindowManager.kt
