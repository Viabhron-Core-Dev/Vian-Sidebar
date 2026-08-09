with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '    private var isAttached = false\\n            viewScope.cancel()\\n    private val viewScope',
    '    private var isAttached = false\\n    private val viewScope'
)

content = content.replace(
    '        if (isAttached) {\\n            windowManager.removeView(this)\\n            isAttached = false\\n        }',
    '        if (isAttached) {\\n            windowManager.removeView(this)\\n            isAttached = false\\n            viewScope.cancel()\\n        }'
)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
