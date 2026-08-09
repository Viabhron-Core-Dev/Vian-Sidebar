import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import com.example.feature.sidebar.AppsPageView
import com.example.feature.sidebar.SidebarAppsManager
import com.example.feature.sidebar.HybridGridPageView
import com.example.feature.sidebar.WidgetsGridPageView
import com.example.feature.sidebar.AppTrackerPageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
"""

if 'import kotlinx.coroutines.CoroutineScope' not in content:
    content = content.replace('import kotlin.math.max\n', 'import kotlin.math.max\n' + imports)

# Add CoroutineScope to SidebarView
if 'private val viewScope = CoroutineScope(Dispatchers.Main + Job())' not in content:
    content = content.replace(
        'private var isAttached = false',
        'private var isAttached = false\n    private val viewScope = CoroutineScope(Dispatchers.Main + Job())\n    private val appsManagers = mutableMapOf<String, SidebarAppsManager>()'
    )

if 'viewScope.cancel()' not in content:
    content = content.replace(
        'isAttached = false',
        'isAttached = false\n            viewScope.cancel()'
    )

# Replace the inner class instantiation
target_bind = """            // TODO: In Phase 7, instantiate AppsPageView, AppTrackerPageView, etc.
            // For now, put a placeholder Text to prove freeze/thaw and lazy loading works.
            val tv = TextView(context).apply {
                text = "Page: ${config.title}\\nType: ${config.type}\\n(Phase 6 Lazy Load Placeholder)"
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            frame.addView(tv)"""

replacement_bind = """            val pageView: View = when (config.type) {
                "apps" -> {
                    val prefKey = "sidebar_apps_${physicalHandleId}_${config.id}"
                    val manager = appsManagers.getOrPut(prefKey) {
                        SidebarAppsManager(context, prefs, viewScope, prefKey) {}
                    }
                    manager.ensureLoaded()
                    val p = AppsPageView(context, physicalHandleId, config, manager, viewScope,
                        onCloseSidebar = { onClose() },
                        onHeightChanged = { newHeight ->
                            // Optional: adjust height if wrap_content
                        }
                    )
                    p.updateData(manager.activeItems)
                    p
                }
                "hybrid_grid" -> {
                    HybridGridPageView(context, config.id) { newHeight ->
                        // Optional: adjust height
                    }
                }
                "widgets_grid" -> {
                    WidgetsGridPageView(context, config.id) { newHeight ->
                        // Optional: adjust height
                    }
                }
                "app_tracker" -> {
                    AppTrackerPageView(context, onClose, { _ -> onClose() })
                }
                else -> {
                    TextView(context).apply {
                        text = "Page: ${config.title}\\nType: ${config.type}\\n(Not Implemented)"
                        setTextColor(Color.WHITE)
                        textSize = 16f
                        gravity = Gravity.CENTER
                        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    }
                }
            }
            frame.addView(pageView)"""

if 'val pageView: View = when (config.type)' not in content:
    content = content.replace(target_bind, replacement_bind)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)

