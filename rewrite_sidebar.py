import re

with open("app/src/main/java/com/example/feature/sidebar/SidebarView.kt", "r") as f:
    text = f.read()

# 1. Change to FrameLayout
text = text.replace(") : LinearLayout(context) {", ") : FrameLayout(context) {")
text = text.replace("orientation = VERTICAL", "// orientation = VERTICAL")

# 2. Extract up to `val marginPx = (marginDp * density).toInt()`
#    `setPadding(0, marginPx, 0, marginPx)`
prefix_end = text.find("setPadding(0, marginPx, 0, marginPx)") + len("setPadding(0, marginPx, 0, marginPx)")
prefix = text[:prefix_end]

# 3. Extract after the ViewPager adapter block where it registers the callback
suffix_start = text.find("        viewPager.setCurrentItem(startingIndex, false)")
suffix = text[suffix_start:]

# Fix the bug in suffix (the index out of bounds)
suffix = suffix.replace("titleText.text = pageConfigs[actualPosition].title", """
                    val safePosition = if (pageConfigs.size > 2) actualPosition % pageConfigs.size else actualPosition
                    titleText.text = pageConfigs[safePosition].title
""")

# Build the new middle part
middle = """
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
        
        viewPager.adapter = object : RecyclerView.Adapter<SidebarPageViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SidebarPageViewHolder {
                val frame = FrameLayout(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
                return SidebarPageViewHolder(frame)
            }

            override fun onBindViewHolder(holder: SidebarPageViewHolder, position: Int) {
                if (pageConfigs.isEmpty()) return
                val actualPosition = if (isLooping) position % pageConfigs.size else position
                val config = pageConfigs[actualPosition]
                holder.bind(config)
            }
            
            override fun getItemCount(): Int = if (isLooping) Int.MAX_VALUE else pageConfigs.size
        }
        
        addView(viewPager)

        // Topbar (FrameLayout overlaid on top)
        val headerHeight = (36 * density).toInt()
        val edgeMargin = (16 * density).toInt()

        val topBar = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, headerHeight).apply {
                gravity = Gravity.TOP
            }
        }
        
        val closeText = TextView(context).apply {
            text = "✕"
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(headerHeight, headerHeight).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = edgeMargin
            }
            setOnClickListener { onClose() }
        }
        topBar.addView(closeText)
        
        val settingsBtn = android.widget.ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_preferences)
            setColorFilter(Color.WHITE)
            setPadding((8*density).toInt(), (8*density).toInt(), (8*density).toInt(), (8*density).toInt())
            layoutParams = FrameLayout.LayoutParams(headerHeight, headerHeight).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = edgeMargin + headerHeight
            }
            setOnClickListener {
                val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
                onClose()
            }
        }
        topBar.addView(settingsBtn)

        val editBtn = android.widget.ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_edit)
            setColorFilter(Color.WHITE)
            val pad = (8 * density).toInt()
            setPadding(pad, pad, pad, pad)
            layoutParams = FrameLayout.LayoutParams(headerHeight, headerHeight).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                marginStart = edgeMargin
            }
            setOnClickListener {
                if (pageConfigs.isNotEmpty()) {
                    val currentItem = viewPager.currentItem
                    val actualPosition = if (pageConfigs.size > 2) currentItem % pageConfigs.size else currentItem
                    val currentPage = pageConfigs[actualPosition]
                    
                    val recyclerView = viewPager.getChildAt(0) as RecyclerView
                    val holder = recyclerView.findViewHolderForAdapterPosition(currentItem) as? SidebarPageViewHolder
                    var handledLocally = false
                    if (holder != null) {
                        val frame = holder.itemView as FrameLayout
                        if (frame.childCount > 0) {
                            val child = frame.getChildAt(0)
                            val pageView = if (child is android.widget.ScrollView) (child as android.view.ViewGroup).getChildAt(0) else child
                            if (pageView is SidebarPageControllable) {
                                pageView.onEditClicked()
                                handledLocally = true
                            } else if (child is SidebarPageControllable) {
                                child.onEditClicked()
                                handledLocally = true
                            }
                        }
                    }
                    
                    if (!handledLocally) {
                        when (currentPage.type) {
                            "hybrid_grid" -> {
                                val intent = Intent(context, com.example.HybridGridEditActivity::class.java).apply {
                                    putExtra("PAGE_ID", currentPage.id)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onClose()
                            }
                            "widgets_grid" -> {
                                val intent = Intent(context, com.example.WidgetsGridEditActivity::class.java).apply {
                                    putExtra("PAGE_ID", currentPage.id)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onClose()
                            }
                            "apps" -> {
                                val intent = Intent(context, com.example.SidebarEditActivity::class.java).apply {
                                    putExtra("PAGE_ID", currentPage.id)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onClose()
                            }
                            "app_tracker" -> {
                                val intent = Intent(context, com.example.AppTrackerSettingsActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onClose()
                            }
                            else -> {
                                val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java).apply {
                                    putExtra("sidebar_edit_page_id", currentPage.id)
                                    putExtra("sidebar_container_id", containerId)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onClose()
                            }
                        }
                    }
                }
            }
        }
        topBar.addView(editBtn)
        
        val titleText = TextView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            setTextColor(Color.WHITE)
            textSize = 14f
            val safePos = if (pageConfigs.size > 2) startingIndex % pageConfigs.size else startingIndex
            text = if (pageConfigs.isNotEmpty()) pageConfigs[safePos].title else "Sidebar"
        }
        topBar.addView(titleText)
        
        addView(topBar)

"""

# Write it out
with open("app/src/main/java/com/example/feature/sidebar/SidebarView.kt", "w") as f:
    f.write(prefix + "\n" + middle + "\n" + suffix)

