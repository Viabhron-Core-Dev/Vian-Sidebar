import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    text = f.read()

# First, modify SidebarPageViewHolder to expose pageView
text = text.replace(
    'inner class SidebarPageViewHolder(private val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {',
    'inner class SidebarPageViewHolder(private val frame: FrameLayout) : RecyclerView.ViewHolder(frame) {\n        var pageView: View? = null'
)

text = text.replace(
    'val pageView: View = when (config.type) {',
    'pageView = when (config.type) {'
)

text = text.replace(
    'frame.addView(pageView)',
    'frame.addView(pageView)'
)

# Modify adapter to have a list of holders or something, or better yet, just broadcast via event or just find the view
page_change_old = """        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (pageConfigs.isNotEmpty()) {
                    val actualPosition = if (isLooping) position % pageConfigs.size else position
                    titleText.text = pageConfigs[actualPosition].title
                }
            }
        })"""

page_change_new = """        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (pageConfigs.isNotEmpty()) {
                    val actualPosition = if (isLooping) position % pageConfigs.size else position
                    titleText.text = pageConfigs[actualPosition].title
                }
                
                // Notify lifecycle
                val rcv = viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
                rcv?.let {
                    for (i in 0 until it.childCount) {
                        val child = it.getChildAt(i)
                        val holder = it.getChildViewHolder(child) as? SidebarPageViewHolder
                        if (holder?.bindingAdapterPosition == position) {
                            (holder.pageView as? SidebarPageControllable)?.onPageSelected()
                        } else {
                            (holder?.pageView as? SidebarPageControllable)?.onPageUnselected()
                        }
                    }
                }
            }
        })"""

text = text.replace(page_change_old, page_change_new)

# Add detach logic
detach_old = """    fun detach() {
        if (isAttached) {
            windowManager.removeView(this)
            isAttached = false
            viewScope.cancel()
        }
    }"""

detach_new = """    fun detach() {
        if (isAttached) {
            // Notify unselected before removing
            val rcv = viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
            rcv?.let {
                for (i in 0 until it.childCount) {
                    val child = it.getChildAt(i)
                    val holder = it.getChildViewHolder(child) as? SidebarPageViewHolder
                    (holder?.pageView as? SidebarPageControllable)?.onPageUnselected()
                }
            }
            windowManager.removeView(this)
            isAttached = false
            viewScope.cancel()
        }
    }"""

text = text.replace(detach_old, detach_new)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(text)
