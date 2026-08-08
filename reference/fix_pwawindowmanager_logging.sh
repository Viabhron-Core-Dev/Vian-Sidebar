#!/bin/bash
sed -i '/if (pwaServer == null) {/a \            com.example.LogKeeper.writeLog("PwaLoader", "Initializing PWA: ${pwa.name}. VirtualHost: ${pwa.useVirtualHost}, Port: ${if (pwa.persistentPort > 0) pwa.persistentPort else "Ephemeral"}, Incognito: ${pwa.incognitoMode}")' app/src/main/java/com/example/service/PwaWindowManager.kt
