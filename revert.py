import os

# Revert SidebarManager.kt
with open('app/src/main/java/com/example/feature/sidebar/SidebarManager.kt', 'r') as f:
    sm_content = f.read()
sm_content = sm_content.replace('val containerId = "${handleId}_${gesture}"', '')
sm_content = sm_content.replace('val gesture = intent.getStringExtra("gesture") ?: "tap"\n        ', '')
sm_content = sm_content.replace('toggleSidebar(handleId, containerId)', 'toggleSidebar(handleId, handleId)')
sm_content = sm_content.replace('toggleSidebar(handleId, containerId, pageId)', 'toggleSidebar(handleId, handleId, pageId)')
with open('app/src/main/java/com/example/feature/sidebar/SidebarManager.kt', 'w') as f:
    f.write(sm_content)

# The other files are quite changed, let's just leave them if we can't revert easily, OR we can fetch from git... but wait, no git repository!
