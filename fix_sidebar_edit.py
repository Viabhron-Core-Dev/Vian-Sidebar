import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

edit_btn_old = """            setOnClickListener {
                // Toggle edit mode
                val currentEditMode = prefs.getBoolean("is_sidebar_edit_mode_${containerId}", false)
                prefs.edit().putBoolean("is_sidebar_edit_mode_${containerId}", !currentEditMode).apply()
            }"""
edit_btn_new = """            setOnClickListener {
                val currentItem = viewPager.currentItem
                val recyclerView = viewPager.getChildAt(0) as RecyclerView
                val holder = recyclerView.findViewHolderForAdapterPosition(currentItem) as? SidebarPageViewHolder
                if (holder != null) {
                    val frame = holder.itemView as FrameLayout
                    if (frame.childCount > 0) {
                        val child = frame.getChildAt(0)
                        val pageView = if (child is android.widget.ScrollView) (child as android.view.ViewGroup).getChildAt(0) else child
                        if (pageView is SidebarPageControllable) {
                            pageView.onEditClicked()
                            return@setOnClickListener
                        } else if (child is SidebarPageControllable) {
                            child.onEditClicked()
                            return@setOnClickListener
                        }
                    }
                }
                
                val currentEditMode = prefs.getBoolean("is_sidebar_edit_mode_${containerId}", false)
                prefs.edit().putBoolean("is_sidebar_edit_mode_${containerId}", !currentEditMode).apply()
                context.sendBroadcast(Intent("com.example.UPDATE_SIDEBAR_ICONS"))
            }"""

content = content.replace(edit_btn_old, edit_btn_new)
with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
