import re

# Fix CalculatorFloatingWindow
with open('app/src/main/java/com/example/feature/miniapps/CalculatorFloatingWindow.kt', 'r') as f:
    content = f.read()
content = re.sub(
    r'class CalculatorFloatingWindow\(\s*context: Context,\s*windowManager: WindowManager\s*\) : FloatingWindow\(context, windowManager, "Calculator"\)',
    r'class CalculatorFloatingWindow(context: Context) : FloatingWindow(context, "Calculator")',
    content
)
with open('app/src/main/java/com/example/feature/miniapps/CalculatorFloatingWindow.kt', 'w') as f:
    f.write(content)

# Fix CompassFloatingWindow
with open('app/src/main/java/com/example/feature/miniapps/CompassFloatingWindow.kt', 'r') as f:
    content = f.read()
content = re.sub(
    r'class CompassFloatingWindow\(\s*context: Context,\s*windowManager: WindowManager\s*\) : FloatingWindow\(context, windowManager, "Compass"\)',
    r'class CompassFloatingWindow(context: Context) : FloatingWindow(context, "Compass")',
    content
)
with open('app/src/main/java/com/example/feature/miniapps/CompassFloatingWindow.kt', 'w') as f:
    f.write(content)

# Fix DictionaryFloatingWindow
with open('app/src/main/java/com/example/feature/miniapps/DictionaryFloatingWindow.kt', 'r') as f:
    content = f.read()
content = re.sub(
    r'class DictionaryFloatingWindow\(\s*context: Context,\s*windowManager: WindowManager\s*\) : FloatingWindow\(context, windowManager, "Dictionary"\)',
    r'class DictionaryFloatingWindow(context: Context) : FloatingWindow(context, "Dictionary")',
    content
)
with open('app/src/main/java/com/example/feature/miniapps/DictionaryFloatingWindow.kt', 'w') as f:
    f.write(content)

# Fix DictionaryPageView.kt (db instance and btn_back)
with open('app/src/main/java/com/example/feature/miniapps/DictionaryPageView.kt', 'r') as f:
    content = f.read()
content = content.replace(
    'private val db = DictionaryDatabase.getInstance(context)',
    'private val db = androidx.room.Room.databaseBuilder(context.applicationContext, DictionaryDatabase::class.java, "dictionary.db").fallbackToDestructiveMigration().build()'
)
content = content.replace('R.id.btn_back_search', 'R.id.btn_back')
with open('app/src/main/java/com/example/feature/miniapps/DictionaryPageView.kt', 'w') as f:
    f.write(content)

# Fix MiniAppManager
with open('app/src/main/java/com/example/feature/miniapps/MiniAppManager.kt', 'r') as f:
    content = f.read()
content = content.replace(
    'val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager\n        \n        // Check if window already exists in FloatingWindowManager\n        val windows = FloatingWindowManager.getWindows()',
    '// Check if window already exists in FloatingWindowManager\n        val windows = FloatingWindowManager.activeWindows'
)
content = content.replace('CalculatorFloatingWindow(context, windowManager)', 'CalculatorFloatingWindow(context)')
content = content.replace('CompassFloatingWindow(context, windowManager)', 'CompassFloatingWindow(context)')
content = content.replace('DictionaryFloatingWindow(context, windowManager)', 'DictionaryFloatingWindow(context)')
with open('app/src/main/java/com/example/feature/miniapps/MiniAppManager.kt', 'w') as f:
    f.write(content)

# Fix FloatingWindowManager
with open('app/src/main/java/com/example/core/FloatingWindowManager.kt', 'r') as f:
    content = f.read()
if 'val activeWindows: List<FloatingWindow>' not in content:
    content = content.replace('private val windows = mutableListOf<FloatingWindow>()', 'private val windows = mutableListOf<FloatingWindow>()\n    val activeWindows: List<FloatingWindow> get() = windows.toList()')
with open('app/src/main/java/com/example/core/FloatingWindowManager.kt', 'w') as f:
    f.write(content)

