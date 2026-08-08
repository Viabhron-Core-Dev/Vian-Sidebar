#!/bin/bash
sed -i 's/var pendingImportCallback: ((String) -> Unit)? = null/val pendingImportCallbacks = java.util.concurrent.ConcurrentHashMap<Int, (String) -> Unit>()\n        private var nextCallbackId = 0\n        @Synchronized fun generateCallbackId(): Int = nextCallbackId++/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# Replace SidebarBridge creation and callback
sed -i 's/sidebarBridge = SidebarBridge(context)/val callbackId = generateCallbackId()\n            this.callbackId = callbackId\n            sidebarBridge = SidebarBridge(context, callbackId)/g' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i 's/pendingImportCallback = { content ->/pendingImportCallbacks\[callbackId\] = { content ->/g' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i 's/pendingImportCallback = null/pendingImportCallbacks.remove(callbackId)/g' app/src/main/java/com/example/service/PwaWindowManager.kt

# Add callbackId to PwaWindowManager class properties
sed -i '/private var floatingView/i \    private var callbackId: Int = -1' app/src/main/java/com/example/service/PwaWindowManager.kt
