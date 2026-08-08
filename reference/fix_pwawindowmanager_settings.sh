#!/bin/bash
sed -i 's/settings.domStorageEnabled = true/settings.domStorageEnabled = !pwa.incognitoMode\n            settings.databaseEnabled = !pwa.incognitoMode/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# Replace findFreePort usage with persistent port logic
sed -i 's/port = findFreePort()/if (pwa.persistentPort > 0) port = pwa.persistentPort else port = findFreePort()/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# Replace loadUrl logic
sed -i 's/loadUrl("http:\/\/localhost:$port\/")/if (pwa.useVirtualHost) loadUrl("https:\/\/pwa-${pwa.id}.app\/") else loadUrl("http:\/\/localhost:$port\/")/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# Add shouldInterceptRequest
sed -i '/override fun onReceivedError(/i \
                override fun shouldInterceptRequest(view: WebView?, request: android.webkit.WebResourceRequest?): android.webkit.WebResourceResponse? {\
                    if (pwa.useVirtualHost && request?.url?.host == "pwa-${pwa.id}.app") {\
                        try {\
                            val urlString = "http://127.0.0.1:$port${request.url.path ?: "/"}${if (request.url.query != null) "?" + request.url.query else ""}"\
                            val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection\
                            connection.requestMethod = request.method\
                            request.requestHeaders?.forEach { (key, value) ->\
                                connection.setRequestProperty(key, value)\
                            }\
                            val statusCode = connection.responseCode\
                            val message = connection.responseMessage\
                            val headers = connection.headerFields?.mapValues { it.value.joinToString(", ") }?.filterKeys { it != null }?.toMutableMap() ?: mutableMapOf()\
                            val contentTypeHeader = connection.contentType ?: "application/octet-stream"\
                            val mimeType = contentTypeHeader.substringBefore(";")\
                            val encoding = if (contentTypeHeader.contains("charset=")) contentTypeHeader.substringAfter("charset=") else "UTF-8"\
                            val inputStream = if (statusCode >= 400) connection.errorStream else connection.inputStream\
                            val response = android.webkit.WebResourceResponse(mimeType, encoding, inputStream)\
                            response.setStatusCodeAndReasonPhrase(statusCode, message)\
                            response.responseHeaders = headers\
                            return response\
                        } catch(e: Exception) {\
                            e.printStackTrace()\
                        }\
                    }\
                    return super.shouldInterceptRequest(view, request)\
                }\
' app/src/main/java/com/example/service/PwaWindowManager.kt

