import re

def process_file(file_path):
    with open(file_path, "r") as f:
        text = f.read()

    # Fix ActiveAppTracker imports
    if "ActiveAppTracker" in text and "import com.example.service.ActiveAppTracker" not in text:
        text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.service.ActiveAppTracker\n")
    if "ActiveAppTracker" in text and "import com.example.service.ActiveAppTracker" not in text and "package com.example.feature.miniapps.browser" in text:
        text = text.replace("package com.example.feature.miniapps.browser\n", "package com.example.feature.miniapps.browser\n\nimport com.example.service.ActiveAppTracker\n")

    # Fix LogKeeper imports in Browser
    if "LogKeeper" in text and "import com.example.core.LogKeeper" not in text and "package com.example.feature.miniapps.browser" in text:
        text = text.replace("package com.example.feature.miniapps.browser\n", "package com.example.feature.miniapps.browser\n\nimport com.example.core.LogKeeper\n")

    # Fix ServiceLifecycleOwner
    if "ServiceLifecycleOwner" in text and "import com.example.service.ServiceLifecycleOwner" not in text:
        text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.service.ServiceLifecycleOwner\n")
        
    # Fix HandleShapeDrawable in ReaderHandleView
    if file_path.endswith("ReaderHandleView.kt"):
        text = text.replace("import com.example.utils.Utils", "import com.example.util.Utils")
        text = text.replace("import com.example.utils.HandleShapeDrawable", "import com.example.feature.sidebar.HandleShapeDrawable")
        if "getEdgeFlag" in text and "import com.example.util.getEdgeFlag" not in text:
            text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.util.getEdgeFlag\n")
        text = text.replace("Utils.instance", "Utils") # Remove .instance if it's an object

    # Comment out DictionaryWindowManager and PwaWindowManager usages in reader (not migrated yet)
    text = re.sub(r'var dictManager: DictionaryWindowManager\? = null', r'// var dictManager: DictionaryWindowManager? = null', text)
    text = re.sub(r'var pwaManager: PwaWindowManager\? = null', r'// var pwaManager: PwaWindowManager? = null', text)
    text = re.sub(r'dictManager = DictionaryWindowManager\(this\)', r'// dictManager = DictionaryWindowManager(this)', text)
    text = re.sub(r'pwaManager = PwaWindowManager\(this\)', r'// pwaManager = PwaWindowManager(this)', text)
    text = re.sub(r'val pwaEntry = PwaEntry.*?\}', r'// val pwaEntry = PwaEntry...', text, flags=re.DOTALL)
    text = re.sub(r'pwaManager\?\.show\(pwaEntry\.id\)', r'// pwaManager?.show(pwaEntry.id)', text)
    
    text = re.sub(r'(floatingView\.findViewById<View>\(R\.id\.btn_dictionary\)\?\.setOnClickListener \{.*?^\s*\})', r'// \1', text, flags=re.DOTALL|re.MULTILINE)
    text = re.sub(r'(floatingView\.findViewById<View>\(R\.id\.btn_browser\)\?\.setOnClickListener \{.*?^\s*\})', r'// \1', text, flags=re.DOTALL|re.MULTILINE)

    with open(file_path, "w") as f:
        f.write(text)

import glob
for f in glob.glob("app/src/main/java/com/example/feature/miniapps/reader/*.kt"):
    process_file(f)
for f in glob.glob("app/src/main/java/com/example/feature/miniapps/browser/*.kt"):
    process_file(f)

