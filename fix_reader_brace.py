import re

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

bad_block = """        // floatingView.findViewById<View>(R.id.btn_top_notes)?.setOnClickListener {
            val intent = android.content.Intent(this, com.example.FloatingTrackerEditActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("book_title", currentBook?.title ?: "")
            }
            startActivity(intent)
        }"""

good_block = """        // floatingView.findViewById<View>(R.id.btn_top_notes)?.setOnClickListener {
        //     val intent = android.content.Intent(this, com.example.FloatingTrackerEditActivity::class.java).apply {
        //         addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        //         putExtra("book_title", currentBook?.title ?: "")
        //     }
        //     startActivity(intent)
        // }"""

text = text.replace(bad_block, good_block)

# Fix ActiveAppTracker import which caused 'Unresolved reference: onPause' error
if "ActiveAppTracker" in text and "import com.example.service.ActiveAppTracker" not in text:
    text = text.replace("package com.example.feature.miniapps.reader\n", "package com.example.feature.miniapps.reader\n\nimport com.example.service.ActiveAppTracker\n")

# Also, there were some other LogKeeper usages that were not fully qualified in FloatingReaderService
text = text.replace("com.example.LogKeeper.writeLog", "com.example.core.LogKeeper.writeLog")

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
