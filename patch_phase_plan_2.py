import re

with open('PHASE_PLAN.md', 'r') as f:
    text = f.read()

old_phases = """## PHASE 10: The Background System Hub (Plugins)
- [ ] Establish `VianSideAccessibilityService` as a unified System Tools Hub.
- [ ] Migrate `CallRecorderManager` as a 100% dormant plugin, waking only via `TelephonyManager`.
- [ ] Migrate `NetSpeedManager` as a screen-aware plugin (`ACTION_SCREEN_ON`/`OFF`).
- [ ] Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`).

## PHASE 11: Sidebar On-Demand Optimization
- [ ] Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen).
- [ ] Migrate remaining dual-mode pages: `SchedulerPageView`, `NotificationPageView`, `ResourcesTrackerPageView`.

## PHASE 12: Unified Z-Window Manager & Heavy Apps
- [ ] Build a centralized Z-Window Manager (handling Z-Ordering and Dormant Folding/Bubbles).
- [ ] Migrate heavy Floating Apps to use the new manager:
    - [ ] `FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`.
    - [ ] `CursorManager` (Trackpad), `WorkNotesWindowManager`.

## PHASE 13: OS Popups & Floating Browser
- [ ] Migrate Text Selection Popups (`DictionaryPopupActivity`, `TranslationPopupActivity`).
- [ ] Migrate the Lightweight Floating Browser (Share Intents).
- [ ] Migrate eReader and Appywork sub-systems."""

new_phases = """## PHASE 10: The Background System Hub (Plugins & Accessibility)
- [ ] Establish `VianSideAccessibilityService` as a unified System Tools Hub.
- [ ] Implement module orchestrator for `CursorManager`, `AutoScrollManager`, and `LongScreenshotManager` (loaded STRICTLY on-demand, no background memory leak).
- [ ] Migrate `CallRecorderManager` as a completely decoupled, dormant module.
- [ ] Migrate `NetSpeedManager` to update the foreground notification dynamically. (CRITICAL: No overlay. Tied to `ACTION_SCREEN_ON`/`OFF` to save battery).
- [ ] Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`).

## PHASE 11: Sidebar On-Demand Optimization (COMPLETED)
- [x] Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen).
- [x] Migrate remaining dual-mode pages: `SchedulerPageView`, `NotificationPageView`, `ResourcesTrackerPageView`.

## PHASE 12: Unified Z-Window Manager & OS Popups
- [ ] Build a centralized Z-Window Manager (handling Z-Ordering and Dormant Folding/Bubbles).
- [ ] Migrate Text Selection Popups (`DictionaryPopupActivity`, `TranslationPopupActivity`).
- [ ] Migrate the Lightweight Floating Browser (Share Intents).

## PHASE 13: Heavy Floating Mini-Apps (Pre-PWA Bridge)
- [ ] Migrate heavy Floating Apps to use the new manager:
    - [ ] `FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`.
    - [ ] `CursorManager` (Trackpad) UI, `WorkNotesWindowManager`.
- [ ] Migrate eReader and Appywork sub-systems (Vibe Coding Hub).
- Note: These are the final native heavy windows before introducing the PWA engine."""

text = text.replace(old_phases, new_phases)

with open('PHASE_PLAN.md', 'w') as f:
    f.write(text)
