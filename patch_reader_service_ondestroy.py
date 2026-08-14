filepath = 'app/src/main/java/com/example/feature/miniapps/reader/FloatingReaderService.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_ondestroy = """        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }"""
new_ondestroy = """        if (::windowManager.isInitialized && ::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch(e:Exception){}
            try { bubbleView?.let { windowManager.removeView(it) } } catch(e:Exception){}
        }"""
content = content.replace(old_ondestroy, new_ondestroy)

with open(filepath, 'w') as f:
    f.write(content)
print("FloatingReaderService onDestroy updated")
