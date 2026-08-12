import re

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

# Comment out missing TrackerActivity button
text = re.sub(
    r'(floatingView\.findViewById<android\.view\.View>\(R\.id\.btn_library_tracker\)\?\.setOnClickListener \{.*?\})',
    r'// \1',
    text,
    flags=re.DOTALL
)

# Comment out missing FloatingTrackerEditActivity button
text = re.sub(
    r'(floatingView\.findViewById<android\.view\.View>\(R\.id\.btn_add_tracker\)\?\.setOnClickListener \{.*?\})',
    r'// \1',
    text,
    flags=re.DOTALL
)

# Fix AppsPageView import if present
if "AppsPageView" in text and "import com.example.feature.sidebar.AppsPageView" not in text:
    text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.feature.sidebar.AppsPageView\n")

# Fix missing run loop imports and 'let' 
if "let" in text and "import kotlin.let" not in text:
     pass # let is inline, the error was because intent receiver was wrong.
     
# Fix ActiveAppTracker import 
if "ActiveAppTracker" in text and "import com.example.service.ActiveAppTracker" not in text:
    text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.service.ActiveAppTracker\n")


with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
