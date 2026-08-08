import re

# HybridGridWindowManager.kt
with open('app/src/main/java/com/example/service/HybridGridWindowManager.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'val height = prefs\.getInt\("hybrid_grid_height", defaultH\)',
    r'val height = WindowManager.LayoutParams.WRAP_CONTENT',
    content
)

content = re.sub(
    r'hybridGridContainer\.addView\(gridPageView, 0, FrameLayout\.LayoutParams\(\s*FrameLayout\.LayoutParams\.MATCH_PARENT,\s*FrameLayout\.LayoutParams\.MATCH_PARENT\s*\)\)',
    r'hybridGridContainer.addView(gridPageView, 0, FrameLayout.LayoutParams(\n            FrameLayout.LayoutParams.MATCH_PARENT, \n            FrameLayout.LayoutParams.WRAP_CONTENT\n        ))',
    content
)

content = re.sub(
    r'val newH = max\(300, startResizeHeight \+ dy\.toInt\(\)\)\n\s*layoutParams!!\.width = newW\n\s*layoutParams!!\.height = newH',
    r'layoutParams!!.width = newW\n                    layoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT',
    content
)

with open('app/src/main/java/com/example/service/HybridGridWindowManager.kt', 'w') as f:
    f.write(content)

# layout_hybrid_grid_floating.xml
with open('app/src/main/res/layout/layout_hybrid_grid_floating.xml', 'r') as f:
    content = f.read()

content = re.sub(
    r'<LinearLayout\s+android:id="@+id/window_container"\s+android:layout_width="match_parent"\s+android:layout_height="match_parent"',
    r'<LinearLayout\n        android:id="@+id/window_container"\n        android:layout_width="match_parent"\n        android:layout_height="wrap_content"',
    content
)

content = re.sub(
    r'<FrameLayout\s+android:id="@+id/hybrid_grid_container"\s+android:layout_width="match_parent"\s+android:layout_height="match_parent"',
    r'<FrameLayout\n            android:id="@+id/hybrid_grid_container"\n            android:layout_width="match_parent"\n            android:layout_height="wrap_content"',
    content
)

with open('app/src/main/res/layout/layout_hybrid_grid_floating.xml', 'w') as f:
    f.write(content)

# HybridGridPageView.kt
with open('app/src/main/java/com/example/service/HybridGridPageView.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'val scrollView = ScrollView\(context\)\.apply \{\s*layoutParams = LayoutParams\(LayoutParams\.MATCH_PARENT, LayoutParams\.MATCH_PARENT\)',
    r'val scrollView = ScrollView(context).apply {\n            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)',
    content
)

with open('app/src/main/java/com/example/service/HybridGridPageView.kt', 'w') as f:
    f.write(content)

