import re

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "r") as f:
    text = f.read()

bad_block = """                // floatingView.findViewById<android.view.View>(R.id.btn_library_tracker)?.setOnClickListener {
                    // val intent = android.content.Intent(this, com.example.TrackerActivity::class.java).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    try { startActivity(intent) } catch (e: Exception) { AppLogger.d("Service", "Failed to start tracker: ${e.message}") }
                    setFolded(true)
                }"""

good_block = """                // floatingView.findViewById<android.view.View>(R.id.btn_library_tracker)?.setOnClickListener {
                //     val intent = android.content.Intent(this, com.example.TrackerActivity::class.java).apply {
                //         addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                //     }
                //     try { startActivity(intent) } catch (e: Exception) { AppLogger.d("Service", "Failed to start tracker: ${e.message}") }
                //     setFolded(true)
                // }"""

text = text.replace(bad_block, good_block)

with open("app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt", "w") as f:
    f.write(text)
