import re

with open('BLUEPRINT.md', 'r') as f:
    bp_content = f.read()

# For BLUEPRINT.md, find "## Next Steps (Phase 9+)" and replace
match = re.search(r"## Next Steps \(Phase 9\+\).*", bp_content, flags=re.DOTALL)
if match:
    new_bp_content = bp_content[:match.start()] + """## Next Steps (Phase 9+)
- **Phase 9: The UI Spines (Settings & Add Element)**: Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is. Wire up migrated pages/services; leave unmigrated features as harmless stubs.
- **Phase 10: The Background System Hub (Plugins)**: Establish `VianSideAccessibilityService` as a unified System Tools Hub. Migrate `CallRecorderManager` (dormant), `NetSpeedManager` (screen-aware), and Hardware Controls.
- **Phase 11: Sidebar On-Demand Optimization**: Restructure `SidebarManager`/`SidebarView` to strictly enforce lazy-loading. Keep only Home/Apps Grid in memory; freeze/thaw other pages dynamically. Migrate remaining dual-mode pages.
- **Phase 12: Unified Z-Window Manager & Heavy Apps**: Build a centralized Z-Window Manager. Migrate heavy Floating Apps (`FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`, `CursorManager`, `WorkNotesWindowManager`).
- **Phase 13: OS Popups & Floating Browser**: Migrate Text Selection Popups, Lightweight Floating Browser, eReader and Appywork sub-systems.
- **Phase 14: PWA Engine**: Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.
- **Phase 15: Polish, Launcher Prep & Finalization**: Implement Advanced Floating Grouping, Backup & Restore JSON framework, prepare architecture hooks for the external Android Launcher merge, and eradicate `reference/`.
"""
    with open('BLUEPRINT.md', 'w') as f:
        f.write(new_bp_content)


with open('BLUEPRINT2.md', 'r') as f:
    bp2_content = f.read()

match2 = re.search(r"\*\*PHASE 9:.*?(?=---)", bp2_content, flags=re.DOTALL)
if match2:
    new_phases_for_bp2 = """**PHASE 9: The UI Spines (Settings & Add Element)**
*   Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is from `reference/`.
*   Wire up migrated pages/services; leave unmigrated features as harmless stubs.
*   Prepare UI lists to send intents to our decoupled unified managers.

**PHASE 10: The Background System Hub (Plugins)**
*   Establish `VianSideAccessibilityService` as a unified System Tools Hub.
*   Migrate `CallRecorderManager` as a 100% dormant plugin, waking only via `TelephonyManager`.
*   Migrate `NetSpeedManager` as a screen-aware plugin (`ACTION_SCREEN_ON`/`OFF`).
*   Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`).

**PHASE 11: Sidebar On-Demand Optimization**
*   Restructure `SidebarManager`/`SidebarView` to strictly enforce lazy-loading (On-Demand memory usage).
*   Keep only Home/Apps Grid in memory; freeze/thaw other pages dynamically.
*   Migrate remaining dual-mode pages: `SchedulerPageView`, `NotificationPageView`, `ResourcesTrackerPageView`.

**PHASE 12: Unified Z-Window Manager & Heavy Apps**
*   Build a centralized Z-Window Manager (handling Z-Ordering and Dormant Folding/Bubbles).
*   Migrate heavy Floating Apps to use the new manager:
    *   `FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`.
    *   `CursorManager` (Trackpad), `WorkNotesWindowManager`.

**PHASE 13: OS Popups & Floating Browser**
*   Migrate Text Selection Popups (`DictionaryPopupActivity`, `TranslationPopupActivity`).
*   Migrate the Lightweight Floating Browser (Share Intents).
*   Migrate eReader and Appywork sub-systems.

**PHASE 14: PWA Engine**
*   Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.

**PHASE 15: Polish, Launcher Prep & Finalization**
*   Implement Advanced Floating Grouping (Bubble Stacking/Collisions).
*   Build the Backup & Restore JSON framework.
*   Prepare architecture hooks for the external Android Launcher merge.
*   Eradicate the `reference/` directory entirely.
"""
    new_bp2_content = bp2_content[:match2.start()] + new_phases_for_bp2 + bp2_content[match2.end():]
    with open('BLUEPRINT2.md', 'w') as f:
        f.write(new_bp2_content)

print("Updated blueprints")
