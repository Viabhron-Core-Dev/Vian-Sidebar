#!/bin/bash
sed -i '/settings.setGeolocationEnabled(true)/a\
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT\
            try { settings.setAppCacheEnabled(true) } catch(e: Exception) {}\
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {\
                android.webkit.ServiceWorkerController.getInstance().serviceWorkerWebSettings.allowContentAccess = true\
                android.webkit.ServiceWorkerController.getInstance().serviceWorkerWebSettings.allowFileAccess = true\
            }\
' app/src/main/java/com/example/service/PwaWindowManager.kt
