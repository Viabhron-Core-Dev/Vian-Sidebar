import re

with open('BLUEPRINT.md', 'r') as f:
    text = f.read()

old_text = """- **Phase 10: The Background System Hub (Plugins)**: Establish `VianSideAccessibilityService` as a unified System Tools Hub. Migrate `CallRecorderManager` (dormant), `NetSpeedManager` (screen-aware), and Hardware Controls.
- **Phase 11: Sidebar On-Demand Optimization**: Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen). Migrate remaining dual-mode pages.
- **Phase 12: Unified Z-Window Manager & Heavy Apps**: Build a centralized Z-Window Manager. Migrate heavy Floating Apps (`FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`, `CursorManager`, `WorkNotesWindowManager`).
- **Phase 13: OS Popups & Floating Browser**: Migrate Text Selection Popups, Lightweight Floating Browser, eReader and Appywork sub-systems."""

new_text = """- **Phase 10: The Background System Hub (Plugins & Accessibility)**: Establish `VianSideAccessibilityService` as a unified System Tools Hub (orchestrator loaded on-demand). Migrate `CallRecorderManager` (dormant), `NetSpeedManager` (screen-aware, no overlay, updates notification icon), and Hardware Controls.
- **Phase 11: Sidebar On-Demand Optimization (COMPLETED)**: Leveraged existing `ViewPager2` lazy-loading.
- **Phase 12: Unified Z-Window Manager & OS Popups**: Build a centralized Z-Window Manager. Migrate Text Selection Popups and Lightweight Floating Browser.
- **Phase 13: Heavy Floating Mini-Apps (Pre-PWA Bridge)**: Migrate heavy Floating Apps (`FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`, `CursorManager` UI, `WorkNotesWindowManager`, and Appywork). These are the final heavy native windows before PWA."""

text = text.replace(old_text, new_text)

with open('BLUEPRINT.md', 'w') as f:
    f.write(text)
