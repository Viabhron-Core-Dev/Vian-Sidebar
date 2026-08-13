import sys

with open("app/src/main/java/com/example/feature/sidebar/SidebarView.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "val marginDp = 8f" in line:
        skip = True
        new_lines.append(line)
        new_lines.append("""
        val marginPx = (marginDp * density).toInt()
        setPadding(0, marginPx, 0, marginPx)

        val isLooping = pageConfigs.size > 2
        val startingIndex = if (isLooping) {
            val half = Int.MAX_VALUE / 2
            half - (half % pageConfigs.size) + max(0, defaultPageIndex)
        } else {
            max(0, defaultPageIndex)
        }

        // ViewPager must be added FIRST so the header floats on top
        viewPager = ViewPager2(context).apply {
            layoutParams = if (wrapContent) {
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            } else {
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            offscreenPageLimit = if (containerId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        }
""")
        continue
    
    if skip and "viewPager.adapter = object : RecyclerView.Adapter<SidebarPageViewHolder>() {" in line:
        skip = False
        
    if not skip:
        new_lines.append(line)

with open("app/src/main/java/com/example/feature/sidebar/SidebarView.kt", "w") as f:
    f.writelines(new_lines)
