import re

with open('app/src/main/java/com/example/service/HybridGridWindowManager.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'val height = WindowManager.LayoutParams.WRAP_CONTENT',
    r'val height = prefs.getInt("hybrid_grid_height", defaultH)',
    content
)

content = re.sub(
    r'layoutParams!!.width = newW\n                    layoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT',
    r'val newH = max(300, startResizeHeight + dy.toInt())\n                    layoutParams!!.width = newW\n                    layoutParams!!.height = newH',
    content
)

with open('app/src/main/java/com/example/service/HybridGridWindowManager.kt', 'w') as f:
    f.write(content)
