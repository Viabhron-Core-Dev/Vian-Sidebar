#!/bin/bash
sed -i '/settings.setAppCacheEnabled(true)/d' app/src/main/java/com/example/service/PwaWindowManager.kt
