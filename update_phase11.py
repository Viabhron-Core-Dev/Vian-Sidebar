import re

def update_file(filename, is_bp2=False):
    try:
        with open(filename, 'r') as f:
            content = f.read()
            
        old_text = "Restructure `SidebarManager`/`SidebarView` to strictly enforce lazy-loading (On-Demand memory usage).\n- [ ] Keep only Home/Apps Grid in memory; freeze/thaw other pages dynamically."
        new_text = "Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen)."
        
        if is_bp2:
            old_text2 = "*   Restructure `SidebarManager`/`SidebarView` to strictly enforce lazy-loading (On-Demand memory usage).\n*   Keep only Home/Apps Grid in memory; freeze/thaw other pages dynamically."
            new_text2 = "*   Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen)."
            content = content.replace(old_text2, new_text2)
        else:
            old_text_md = "Restructure `SidebarManager`/`SidebarView` to strictly enforce lazy-loading. Keep only Home/Apps Grid in memory; freeze/thaw other pages dynamically."
            new_text_md = "Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen)."
            content = content.replace(old_text, new_text)
            content = content.replace(old_text_md, new_text_md)
            
        with open(filename, 'w') as f:
            f.write(content)
            
        print(f"Updated {filename}")
    except Exception as e:
        print(f"Failed {filename}: {e}")

update_file('PHASE_PLAN.md')
update_file('BLUEPRINT.md')
update_file('BLUEPRINT2.md', is_bp2=True)
