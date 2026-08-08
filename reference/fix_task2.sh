#!/bin/bash
sed -i '/fun close() {/a\
        pendingImportCallback = null\
' app/src/main/java/com/example/service/PwaWindowManager.kt
