import re

with open('PHASE_PLAN.md', 'r') as f:
    content = f.read()

# Find the start of PHASE 9
split_point = content.find("## PHASE 9")
if split_point != -1:
    content = content[:split_point]

new_phases = """## PHASE 9: The UI Spines (Settings & Add Element)
- [ ] Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is from `reference/`.
- [ ] Wire up migrated pages/services; leave unmigrated features as harmless stubs.
- [ ] Prepare UI lists to send intents to our decoupled unified managers.

## PHASE 10: The Background System Hub (Plugins)
- [ ] Establish `VianSideAccessibilityService` as a unified System Tools Hub.
- [ ] Migrate `CallRecorderManager` as a 100% dormant plugin, waking only via `TelephonyManager`.
- [ ] Migrate `NetSpeedManager` as a screen-aware plugin (`ACTION_SCREEN_ON`/`OFF`).
- [ ] Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`).

## PHASE 11: Sidebar On-Demand Optimization
- [ ] Restructure `SidebarManager`/`SidebarView` to strictly enforce lazy-loading (On-Demand memory usage).
- [ ] Keep only Home/Apps Grid in memory; freeze/thaw other pages dynamically.
- [ ] Migrate remaining dual-mode pages: `SchedulerPageView`, `NotificationPageView`, `ResourcesTrackerPageView`.

## PHASE 12: Unified Z-Window Manager & Heavy Apps
- [ ] Build a centralized Z-Window Manager (handling Z-Ordering and Dormant Folding/Bubbles).
- [ ] Migrate heavy Floating Apps to use the new manager:
    - [ ] `FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`.
    - [ ] `CursorManager` (Trackpad), `WorkNotesWindowManager`.

## PHASE 13: OS Popups & Floating Browser
- [ ] Migrate Text Selection Popups (`DictionaryPopupActivity`, `TranslationPopupActivity`).
- [ ] Migrate the Lightweight Floating Browser (Share Intents).
- [ ] Migrate eReader and Appywork sub-systems.

## PHASE 14: PWA Engine
- [ ] Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.

## PHASE 15: Polish, Launcher Prep & Finalization
- [ ] Implement Advanced Floating Grouping (Bubble Stacking/Collisions).
- [ ] Build the Backup & Restore JSON framework.
- [ ] Prepare architecture hooks for the external Android Launcher merge.
- [ ] Eradicate the `reference/` directory entirely.
"""

with open('PHASE_PLAN.md', 'w') as f:
    f.write(content + new_phases)

print("Updated PHASE_PLAN.md")
