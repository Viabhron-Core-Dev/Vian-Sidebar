import re

with open('app/src/main/java/com/example/service/WidgetsGridEditOverlayView.kt', 'r') as f:
    code = f.read()

# Replace class declaration
code = code.replace('@SuppressLint("ViewConstructor")\nclass WidgetsGridEditOverlayView(', 'class WidgetsGridEditActivity : androidx.activity.ComponentActivity() {')

# Find init block and replace with onCreate
code = re.sub(r'init \{', r'override fun onCreate(savedInstanceState: android.os.Bundle?) {\nsuper.onCreate(savedInstanceState)\nval pageId = intent.getStringExtra("PAGE_ID") ?: return\n', code)

print(code[:500])
