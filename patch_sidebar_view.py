filepath = 'app/src/main/java/com/example/feature/sidebar/SidebarView.kt'
with open(filepath, 'r') as f:
    content = f.read()

old_line = 'offscreenPageLimit = if (containerId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT'
new_line = 'offscreenPageLimit = if (physicalHandleId == "sidebar") 1 else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT'

content = content.replace(old_line, new_line)

with open(filepath, 'w') as f:
    f.write(content)
print("SidebarView patched")
