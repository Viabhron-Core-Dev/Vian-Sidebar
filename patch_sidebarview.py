import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

target = """                "app_tracker" -> {
                    AppTrackerPageView(context, onClose, { _ -> onClose() })
                }
                else -> {"""

replacement = """                "app_tracker" -> {
                    AppTrackerPageView(context, onClose, { _ -> onClose() })
                }
                "media_player" -> {
                    MediaPlayerPageView(context, onClose) { newHeight ->
                        if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }
                }
                "widget" -> {
                    WidgetPageView(context, config.id) { newHeight ->
                        if (wrapContent && viewPager.currentItem == bindingAdapterPosition) {
                            val params = viewPager.layoutParams
                            if (params.height != newHeight) {
                                params.height = newHeight
                                viewPager.layoutParams = params
                                windowManager.updateViewLayout(this@SidebarView, layoutParams)
                            }
                        }
                    }
                }
                else -> {"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Target not found")
