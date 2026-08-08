import re
with open('app/src/main/java/com/example/service/FloatingBrowserWindowManager.kt', 'r') as f:
    content = f.read()

content = content.replace('var layoutParams: WindowManager.LayoutParams?', 'var windowLayoutParams: WindowManager.LayoutParams?')
content = content.replace('layoutParams = WindowManager.LayoutParams(', 'windowLayoutParams = WindowManager.LayoutParams(')
content = content.replace('windowManager.addView(floatingView, layoutParams)', 'windowManager.addView(floatingView, windowLayoutParams)')
content = content.replace('windowManager.updateViewLayout(floatingView, layoutParams)', 'windowManager.updateViewLayout(floatingView, windowLayoutParams)')
content = content.replace('layoutParams?.', 'windowLayoutParams?.')
content = content.replace('layoutParams = FrameLayout.LayoutParams', 'this.layoutParams = FrameLayout.LayoutParams')
content = content.replace('layoutParams = LinearLayout.LayoutParams', 'this.layoutParams = LinearLayout.LayoutParams')
content = content.replace('layoutParams = ViewGroup.LayoutParams', 'this.layoutParams = ViewGroup.LayoutParams')

with open('app/src/main/java/com/example/service/FloatingBrowserWindowManager.kt', 'w') as f:
    f.write(content)
