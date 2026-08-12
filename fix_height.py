import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

# Update onHeightChanged for pages to adjust the ViewPager height
height_handler = """                        onHeightChanged = { newHeight ->
                            if (wrapContent && viewPager.currentItem == actualPosition) {
                                val params = viewPager.layoutParams
                                if (params.height != newHeight) {
                                    params.height = newHeight
                                    viewPager.layoutParams = params
                                    windowManager.updateViewLayout(this@SidebarView, layoutParams)
                                }
                            }
                        }"""
                        
content = re.sub(
    r'onHeightChanged = { newHeight ->\s*// Optional: adjust height if wrap_content\s*}',
    height_handler,
    content
)

# Also apply for hybrid_grid and widgets_grid
hybrid_handler = """                    HybridGridPageView(context, config.id) { newHeight ->
                        if (wrapContent && viewPager.currentItem == actualPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }"""
content = re.sub(
    r'HybridGridPageView\(context, config\.id\) { newHeight ->\s*// Optional: adjust height\s*}',
    hybrid_handler,
    content
)

widget_handler = """                    WidgetsGridPageView(context, config.id) { newHeight ->
                        if (wrapContent && viewPager.currentItem == actualPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }"""
content = re.sub(
    r'WidgetsGridPageView\(context, config\.id\) { newHeight ->\s*// Optional: adjust height\s*}',
    widget_handler,
    content
)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
