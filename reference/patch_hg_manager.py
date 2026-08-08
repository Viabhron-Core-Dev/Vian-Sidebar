import re

with open('app/src/main/java/com/example/service/HybridGridWindowManager.kt', 'r') as f:
    content = f.read()

# Remove reading height from prefs
content = re.sub(
    r'val height = prefs\.getInt\("hybrid_grid_height", defaultH\)',
    r'val height = WindowManager.LayoutParams.WRAP_CONTENT',
    content
)

# In ACTION_MOVE of resizeHandle, only change width
content = re.sub(
    r'val newH = max\(300, startResizeHeight \+ dy\.toInt\(\)\)\n\s*layoutParams!!\.width = newW\n\s*layoutParams!!\.height = newH',
    r'layoutParams!!.width = newW\n                    layoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT',
    content
)

with open('app/src/main/java/com/example/service/HybridGridWindowManager.kt', 'w') as f:
    f.write(content)
