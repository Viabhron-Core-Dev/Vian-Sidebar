import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

# Make wrapContent a class property
content = content.replace(
    'class SidebarView(',
    'class SidebarView(\n'
)
content = content.replace(
    'private val onClose: () -> Unit\n) : LinearLayout(context) {',
    'private val onClose: () -> Unit\n) : LinearLayout(context) {\n\n    private val wrapContent = prefs.getBoolean("handle_${containerId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))'
)
# Remove the local wrapContent definition in init block
content = content.replace(
    'val wrapContent = prefs.getBoolean("handle_${containerId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))\n',
    ''
)

# Wait, `actualPosition` error in ViewPager OnPageChangeCallback:
# In `onPageSelected`, there is no `actualPosition` in scope for the `wrapContent` check if I just used it! 
# Let's see: I wrote:
# if (wrapContent) {
#   val recyclerView = viewPager.getChildAt(0) as RecyclerView
# ...

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
