with open("app/src/main/java/com/example/feature/sidebar/SidebarView.kt", "r") as f:
    text = f.read()

import_str = "import android.view.MotionEvent\n"
if "import android.view.MotionEvent" not in text:
    text = text.replace("import android.view.ViewGroup", import_str + "import android.view.ViewGroup")

method_str = """
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            onClose()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            onClose()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
"""

if "fun onTouchEvent" not in text:
    text = text.replace("    fun detach() {", method_str + "    fun detach() {")

with open("app/src/main/java/com/example/feature/sidebar/SidebarView.kt", "w") as f:
    f.write(text)
