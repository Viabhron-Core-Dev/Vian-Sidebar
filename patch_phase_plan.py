with open('PHASE_PLAN.md', 'r') as f:
    text = f.read()

# Phase 4
text = text.replace('- [ ] Deconstruct God Service: Migrate edge detection and gesture parsing to `HandleService` (`core/`).', '- [x] Deconstruct God Service: Migrate edge detection and gesture parsing to `HandleService` (`core/`).')
text = text.replace('- [ ] Ensure it runs continuously and remains incredibly lightweight.', '- [x] Ensure it runs continuously and remains incredibly lightweight.')

# Phase 5
text = text.replace('- [ ] Establish the central `WindowManager` for handling floating view lifecycles.', '- [x] Establish the central `WindowManager` for handling floating view lifecycles.')
text = text.replace('- [ ] Implement `onTrimMemory` logic for OS memory pressure fallback.', '- [x] Implement `onTrimMemory` logic for OS memory pressure fallback.')

# Phase 6
text = text.replace('- [ ] Establish `SidebarManager` (`feature/sidebar/`) to listen for intents.', '- [x] Establish `SidebarManager` (`feature/sidebar/`) to listen for intents.')
text = text.replace('- [ ] Build `SidebarView` container with ViewPager for lazy page instantiation (Freeze/Thaw UI logic).', '- [x] Build `SidebarView` container with ViewPager for lazy page instantiation (Freeze/Thaw UI logic).')

# Phase 7
text = text.replace('- [ ] Migrate standard pages: `AppsPageView`, `AppTrackerPageView`, etc.', '- [x] Migrate standard pages: `AppsPageView`, `AppTrackerPageView`, etc.')
text = text.replace('- [ ] Connect them to the Sidebar container.', '- [x] Connect them to the Sidebar container.')

with open('PHASE_PLAN.md', 'w') as f:
    f.write(text)
