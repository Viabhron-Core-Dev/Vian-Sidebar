import os
import re

files = [
    "app/src/main/java/com/example/service/PageWindowManager.kt",
    "app/src/main/java/com/example/service/WorkNotesWindowManager.kt",
    "app/src/main/java/com/example/service/DictionaryWindowManager.kt",
    "app/src/main/java/com/example/service/PwaWindowManager.kt"
]

def process_file(filepath):
    if not os.path.exists(filepath):
        return
    with open(filepath, 'r') as f:
        content = f.read()

    # 1. Add toggleFullScreen function and variables
    if "fun toggleFullScreen" not in content:
        toggle_logic = """
    private var isFullScreen = false
    private var preFullScreenWidth = 800
    private var preFullScreenHeight = 1000
    private var preFullScreenX = 100
    private var preFullScreenY = 100

    private fun toggleFullScreen() {
        if (!isFullScreen) {
            preFullScreenWidth = layoutParams?.width ?: 800
            preFullScreenHeight = layoutParams?.height ?: 1000
            preFullScreenX = layoutParams?.x ?: 100
            preFullScreenY = layoutParams?.y ?: 100
            
            val metrics = context.resources.displayMetrics
            layoutParams?.width = metrics.widthPixels
            layoutParams?.height = metrics.heightPixels
            layoutParams?.x = 0
            layoutParams?.y = 0
            isFullScreen = true
        } else {
            layoutParams?.width = preFullScreenWidth
            layoutParams?.height = preFullScreenHeight
            layoutParams?.x = preFullScreenX
            layoutParams?.y = preFullScreenY
            isFullScreen = false
        }
        windowManager.updateViewLayout(floatingView, layoutParams)
    }
"""
        # Find fun show() and insert before it
        content = re.sub(r'(    fun show\(\) \{)', toggle_logic + r'\1', content)

    # 2. Modify top bar gestures
    # Replace detectDragGestures with detectDragGesturesAfterLongPress
    topbar_gesture = r"""\.pointerInput\(Unit\) \{
\s*detectDragGestures \{ change, dragAmount ->
\s*change\.consume\(\)
\s*onDrag\(dragAmount\.x, dragAmount\.y\)
\s*\}
\s*\}"""
    replacement_gesture = r""".pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { toggleFullScreen() }
                        )
                    }
                    .pointerInput(Unit) {
                        androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }"""
    content = re.sub(topbar_gesture, replacement_gesture, content, count=1)

    # Dictionary has a slightly different drag gesture matching
    # We will just do a general replacement if not matched

    # 3. Remove buttons from Top Bar and add Bottom Bar
    # In Top bar, remove Row { IconButton ... }
    content = re.sub(r'Row \{\s*IconButton\(onClick = onMinimize.*?\}.*?IconButton\(onClick = onClose.*?\}.*?\}', '', content, flags=re.DOTALL)
    content = re.sub(r'IconButton\(onClick = onFold.*?\}.*?IconButton\(onClick = onClose.*?\}', '', content, flags=re.DOTALL)

    # 4. Modify Bottom Resize handle to include minimize and close
    resize_handle_old = r'// Resize handle.*?Box\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.height\(20\.dp\).*?\{.*?Text\("///".*?\}\s*\}'
    resize_handle_new = r"""// Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFF2A2A3C)),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMinimize ?: onFold, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF2A2A3C))
                        .pointerInput(Unit) {
                            androidx.compose.foundation.gestures.detectDragGestures { change, dragAmount ->
                                change.consume()
                                onResize(dragAmount.x, dragAmount.y)
                            }
                        }
                ) {
                    Text("///", color = Color.Gray, modifier = Modifier.align(Alignment.Center).padding(end = 4.dp, bottom = 4.dp))
                }
            }"""
    
    # We need to make sure onFold or onMinimize is handled properly. In Dictionary it is onFold, others onMinimize.
    # Let's fix this in the script dynamically later.

    with open(filepath, 'w') as f:
        f.write(content)

for f in files:
    process_file(f)

