import re
import glob

def fix_imports(file_path):
    with open(file_path, "r") as f:
        content = f.read()
    
    # Fix LogKeeper imports
    content = content.replace("import com.example.service.LogKeeper", "import com.example.core.LogKeeper")
    content = content.replace("import com.example.LogKeeper", "import com.example.core.LogKeeper")
    content = content.replace("import com.example.util.LogKeeper", "import com.example.core.LogKeeper")
    if "LogKeeper." in content and "import com.example.core.LogKeeper" not in content:
        content = content.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.core.LogKeeper\n")
    
    # Fix missing SidebarService imports
    if "SidebarService::class.java" in content and "import com.example.service.SidebarService" not in content:
        content = content.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.service.SidebarService\n")
    
    # Fix intent extra unresolved references (caused by missing import android.content.Intent)
    if "putExtra" in content and "import android.content.Intent" not in content:
        content = content.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport android.content.Intent\n")

    # Fix Utils imports (from com.example.utils.Utils)
    content = content.replace("import com.example.utils.Utils", "import com.example.utils.Utils")
    if "Utils." in content and "import com.example.utils.Utils" not in content:
        content = content.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.utils.Utils\n")
        
    # Fix HandleShapeDrawable imports (from com.example.utils.HandleShapeDrawable)
    content = content.replace("import com.example.utils.HandleShapeDrawable", "import com.example.utils.HandleShapeDrawable")
    if "HandleShapeDrawable(" in content and "import com.example.utils.HandleShapeDrawable" not in content:
        content = content.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.utils.HandleShapeDrawable\n")
    
    with open(file_path, "w") as f:
        f.write(content)

for f in glob.glob("app/src/main/java/com/example/feature/miniapps/reader/*.kt"):
    fix_imports(f)
