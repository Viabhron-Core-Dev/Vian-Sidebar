#!/bin/bash
sed -i '27,29d' app/src/main/java/com/example/service/PwaWindowManager.kt
sed -i '/class PwaWindowManager/a\
    companion object {\
        var pendingImportCallback: ((String) -> Unit)? = null\
    }\
' app/src/main/java/com/example/service/PwaWindowManager.kt
