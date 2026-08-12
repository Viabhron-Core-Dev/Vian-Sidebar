import re

with open('app/src/main/java/com/example/core/HandleService.kt', 'r') as f:
    text = f.read()

# Add readerHandleView variable
text = text.replace(
    'private val triggerHandleViews = mutableListOf<TriggerHandleView>()',
    'private val triggerHandleViews = mutableListOf<TriggerHandleView>()\n    private var readerHandleView: com.example.feature.miniapps.reader.ReaderHandleView? = null'
)

# Initialize it in onCreate
text = text.replace(
    'reloadHandles()',
    'reloadHandles()\n        readerHandleView = com.example.feature.miniapps.reader.ReaderHandleView(this, prefs, windowManager)\n        if (prefs.getBoolean("reader_handle_enabled", false)) {\n            readerHandleView?.attach()\n        }'
)

# Update it on pref change
text = text.replace(
    'triggerHandleViews.forEach { it.updatePosition() }',
    'triggerHandleViews.forEach { it.updatePosition() }\n                readerHandleView?.updatePosition()'
)

# detach it in onDestroy
text = text.replace(
    'triggerHandleViews.clear()',
    'triggerHandleViews.clear()\n        readerHandleView?.detach()\n        readerHandleView = null'
)

# Update visibility mode
text = text.replace(
    'triggerHandleViews.forEach { it.setVisibility(if (editMode) true else null) }',
    'triggerHandleViews.forEach { it.setVisibility(if (editMode) true else null) }\n                readerHandleView?.setVisibility(editMode)'
)

# Add reader handle toggle in pref change
pref_change = """            } else {
                reloadHandles()
            }"""

pref_change_new = """            } else {
                reloadHandles()
            }
        } else if (key == "reader_handle_enabled") {
            if (prefs.getBoolean("reader_handle_enabled", false)) {
                readerHandleView?.attach()
            } else {
                readerHandleView?.detach()
            }
        }"""

text = text.replace(pref_change, pref_change_new)

with open('app/src/main/java/com/example/core/HandleService.kt', 'w') as f:
    f.write(text)

