import re

# Update SidebarView.kt
with open('app/src/main/java/com/example/service/SidebarView.kt', 'r') as f:
    content = f.read()

# 1. Update header height and buttons padding
content = content.replace('val headerHeight = (28 * density).toInt()', 'val headerHeight = (24 * density).toInt()')
content = content.replace('textSize = 20f', 'textSize = 16f')
content = content.replace('setPadding(8, 8, 8, 8)', 'setPadding((4*density).toInt(), (4*density).toInt(), (4*density).toInt(), (4*density).toInt())')
content = content.replace('val pad = (12 * resources.displayMetrics.density).toInt()', 'val pad = (6 * resources.displayMetrics.density).toInt()')

# 2. Update viewPager layout parameters
old_vp = """        viewPager = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = (30 * density).toInt()
            }
        }"""
new_vp = """        viewPager = ViewPager2(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }"""
content = content.replace(old_vp, new_vp)

# 3. Move dots layout to header
old_dots = """        dotsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (30 * density).toInt()).apply {
                gravity = Gravity.BOTTOM
            }
        }"""
new_dots = """        dotsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                gravity = Gravity.CENTER
            }
        }"""
content = content.replace(old_dots, new_dots)

# 4. Change view additions
content = content.replace('container.addView(dotsLayout)', 'header.addView(dotsLayout)')

# 5. Update height calculation
content = content.replace('var targetHeight = pageHeightPx + (28 + 30) * density', 'var targetHeight = pageHeightPx + (24 * density)')

with open('app/src/main/java/com/example/service/SidebarView.kt', 'w') as f:
    f.write(content)

# Update item_sidebar_app.xml
with open('app/src/main/res/layout/item_sidebar_app.xml', 'r') as f:
    xml = f.read()

xml = xml.replace('android:padding="6dp"', 'android:padding="2dp"')
xml = xml.replace('android:layout_width="44dp"', 'android:layout_width="52dp"')
xml = xml.replace('android:layout_height="44dp"', 'android:layout_height="52dp"')

with open('app/src/main/res/layout/item_sidebar_app.xml', 'w') as f:
    f.write(xml)

