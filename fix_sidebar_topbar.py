import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    content = f.read()

# Change FrameLayout to LinearLayout
content = content.replace(') : FrameLayout(context) {', ') : LinearLayout(context) {')

# Set orientation
orientation_setup = """        orientation = VERTICAL

        isFocusableInTouchMode = true"""
content = content.replace('        isFocusableInTouchMode = true', orientation_setup)

# Create Topbar
topbar_code = """
        // Topbar
        val topBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding((12 * density).toInt(), (4 * density).toInt(), (12 * density).toInt(), (4 * density).toInt())
        }

        // Edit Button
        val editBtn = android.widget.ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(android.R.drawable.ic_menu_edit)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                // Toggle edit mode
                val currentEditMode = prefs.getBoolean("is_sidebar_edit_mode_${containerId}", false)
                prefs.edit().putBoolean("is_sidebar_edit_mode_${containerId}", !currentEditMode).apply()
            }
        }
        topBar.addView(editBtn)

        // Title / Page Indicator
        val titleText = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 14f
            text = if (pageConfigs.isNotEmpty()) pageConfigs[startingIndex].title else "Sidebar"
        }
        topBar.addView(titleText)

        // Settings Button
        val settingsBtn = android.widget.ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(android.R.drawable.ic_menu_preferences)
            setColorFilter(Color.WHITE)
            setOnClickListener {
                val intent = Intent(context, com.example.feature.settings.SettingsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                onClose()
            }
        }
        topBar.addView(settingsBtn)

        addView(topBar)

        // ViewPager
        viewPager = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            offscreenPageLimit = if (containerId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        }
"""

content = content.replace("""        viewPager = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            offscreenPageLimit = if (containerId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT // Semi-loaded for Handle 1
        }""", topbar_code)

# Add Page Change Callback to update titleText
page_callback_code = """
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (pageConfigs.isNotEmpty()) {
                    val actualPosition = if (isLooping) position % pageConfigs.size else position
                    titleText.text = pageConfigs[actualPosition].title
                }
            }
        })
        
        addView(viewPager)
"""
content = content.replace('viewPager.setCurrentItem(startingIndex, false)', 'viewPager.setCurrentItem(startingIndex, false)\n' + page_callback_code)


with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.write(content)
