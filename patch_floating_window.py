filepath = 'app/src/main/java/com/example/core/FloatingWindow.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("var isFullScreen = false", "var isFullScreen = false\n\n    var onClose: (() -> Unit)? = null")

old_hide = """    fun hide() {
        view?.let {
            windowManager.removeView(it)
            view = null
        }
        bubbleView?.let {
            windowManager.removeView(it)
            bubbleView = null
        }
    }"""
new_hide = """    fun hide() {
        onClose?.invoke()
        view?.let {
            windowManager.removeView(it)
            view = null
        }
        bubbleView?.let {
            windowManager.removeView(it)
            bubbleView = null
        }
    }"""
content = content.replace(old_hide, new_hide)

with open(filepath, 'w') as f:
    f.write(content)
print("FloatingWindow updated")
