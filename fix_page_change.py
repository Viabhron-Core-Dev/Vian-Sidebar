import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

page_change_code = """        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (pageConfigs.isNotEmpty()) {
                    val actualPosition = if (pageConfigs.size > 2) position % pageConfigs.size else position
                    titleText.text = pageConfigs[actualPosition].title
                    
                    if (wrapContent) {
                        val recyclerView = viewPager.getChildAt(0) as RecyclerView
                        val holder = recyclerView.findViewHolderForAdapterPosition(position) as? SidebarPageViewHolder
                        if (holder != null) {
                            val frame = holder.itemView as FrameLayout
                            if (frame.childCount > 0) {
                                val child = frame.getChildAt(0)
                                val pageView = if (child is android.widget.ScrollView) (child as android.view.ViewGroup).getChildAt(0) else child
                                var newHeight = -1
                                if (pageView is HybridGridPageView) {
                                    newHeight = pageView.getCurrentHeightPx()
                                } else if (pageView is WidgetsGridPageView) {
                                    newHeight = pageView.getCurrentHeightPx()
                                } else if (pageView is AppsPageView) {
                                    newHeight = pageView.getCurrentHeightPx()
                                }
                                if (newHeight > 0) {
                                    val params = viewPager.layoutParams
                                    if (params.height != newHeight) {
                                        params.height = newHeight
                                        viewPager.layoutParams = params
                                        windowManager.updateViewLayout(this@SidebarView, layoutParams)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })"""

# Insert right after viewPager definition
content = content.replace('        viewPager.adapter = adapter\n        viewPager.setCurrentItem(startingIndex, false)', '        viewPager.adapter = adapter\n        viewPager.setCurrentItem(startingIndex, false)\n\n' + page_change_code)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
