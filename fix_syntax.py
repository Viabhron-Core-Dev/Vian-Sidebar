import re

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == 'private':
        new_lines.append('    private val wrapContent = prefs.getBoolean("handle_${containerId}_sidebar_wrap_content", prefs.getBoolean("sidebar_wrap_content", true))\n')
    else:
        new_lines.append(line)

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    f.writelines(new_lines)
