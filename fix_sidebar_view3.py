with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'r') as f:
    lines = f.readlines()

with open('app/src/main/java/com/example/feature/sidebar/SidebarView.kt', 'w') as f:
    for line in lines:
        if 'viewScope.cancel()' in line and 'isAttached = false' not in line and 'private var isAttached' not in line:
            # We want to keep it inside detach(), but it seems it was injected weirdly.
            pass
        f.write(line.replace('            viewScope.cancel()\\n', ''))

