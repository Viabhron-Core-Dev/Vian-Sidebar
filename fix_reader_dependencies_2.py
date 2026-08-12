import re

def process_file(file_path):
    with open(file_path, "r") as f:
        text = f.read()

    # DictionaryPopupActivity
    text = text.replace("import android.content.Intent\nimport android.content.Intent\n", "import android.content.Intent\n")

    # FloatingBrowserWindowManager
    if file_path.endswith("FloatingBrowserWindowManager.kt"):
        text = text.replace("package com.example.feature.miniapps.browser\n\nimport com.example.service.ActiveAppTracker\n", "package com.example.feature.miniapps.browser\n")
        text = text.replace("ActiveAppTracker.onStart(context, \"browser\")", "// ActiveAppTracker.onStart(context, \"browser\")")
        text = text.replace("ActiveAppTracker.onStop(context, \"browser\")", "// ActiveAppTracker.onStop(context, \"browser\")")
        
        text = text.replace("package com.example.feature.miniapps.browser\n\nimport com.example.core.LogKeeper\n", "package com.example.feature.miniapps.browser\n")
        text = text.replace("LogKeeper.writeLog(", "com.example.core.LogKeeper.writeLog(")

    # FloatingReaderService
    if file_path.endswith("FloatingReaderService.kt"):
        text = text.replace("package com.example.feature.miniapps.reader\n\nimport com.example.service.ServiceLifecycleOwner\n", "package com.example.feature.miniapps.reader\n")
        text = text.replace("package com.example.feature.miniapps.reader\n\nimport com.example.service.ActiveAppTracker\n", "package com.example.feature.miniapps.reader\n")
        text = text.replace("private val lifecycleOwner = ServiceLifecycleOwner()", "// private val lifecycleOwner = ServiceLifecycleOwner()")
        
        text = text.replace("ActiveAppTracker.onStart(this, \"reader\")", "// ActiveAppTracker.onStart(this, \"reader\")")
        text = text.replace("ActiveAppTracker.onStop(this, \"reader\")", "// ActiveAppTracker.onStop(this, \"reader\")")
        
        text = text.replace("lifecycleOwner.onCreate()", "// lifecycleOwner.onCreate()")
        text = text.replace("lifecycleOwner.onStart()", "// lifecycleOwner.onStart()")
        text = text.replace("lifecycleOwner.onResume()", "// lifecycleOwner.onResume()")
        text = text.replace("lifecycleOwner.onPause()", "// lifecycleOwner.onPause()")
        text = text.replace("lifecycleOwner.onStop()", "// lifecycleOwner.onStop()")
        text = text.replace("lifecycleOwner.onDestroy()", "// lifecycleOwner.onDestroy()")

    # ReaderHandleView
    if file_path.endswith("ReaderHandleView.kt"):
        text = text.replace("import com.example.feature.sidebar.HandleShapeDrawable", "import com.example.feature.sidebar.BubbleDrawable")
        text = text.replace("HandleShapeDrawable", "BubbleDrawable")
        
        if "fun getEdgeFlag" not in text:
            text = text.replace("package com.example.feature.miniapps.reader\n\nimport com.example.util.getEdgeFlag\n", "package com.example.feature.miniapps.reader\n")
            text = text.replace("getEdgeFlag()", "0") # Replace getEdgeFlag with stub

    with open(file_path, "w") as f:
        f.write(text)

import glob
for f in glob.glob("app/src/main/java/com/example/feature/miniapps/*.kt"):
    process_file(f)
for f in glob.glob("app/src/main/java/com/example/feature/miniapps/reader/*.kt"):
    process_file(f)
for f in glob.glob("app/src/main/java/com/example/feature/miniapps/browser/*.kt"):
    process_file(f)
