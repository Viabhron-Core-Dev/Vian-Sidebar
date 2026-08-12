import re

with open('app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt', 'r') as f:
    text = f.read()

old_action = """    private fun handleAction(gesture: String) {
        if (com.example.feature.miniapps.reader.FloatingReaderService.instance != null) {
            com.example.feature.miniapps.reader.FloatingReaderService.instance?.toggleReader()
        } else {
            val intent = android.content.Intent(context, com.example.feature.miniapps.reader.FloatingReaderService::class.java)
            intent.putExtra("UNFOLD", true)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }"""

new_action = """    private fun handleAction(gesture: String) {
        val intent = android.content.Intent(context, com.example.feature.miniapps.reader.FloatingReaderService::class.java)
        intent.putExtra("UNFOLD", true)
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
    }"""

text = text.replace(old_action, new_action)

with open('app/src/main/java/com/example/feature/miniapps/reader/ReaderHandleView.kt', 'w') as f:
    f.write(text)
