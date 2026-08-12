# Vian Sidebar - Migration & Restructuring Phase Plan

## PHASE 0: Staging & Build Configuration
- [ ] Create base directory structure: `core/`, `data/`, `feature/` under the new flat namespace.
- [ ] Configure Gradle to exclude `reference/` entirely to prevent duplicate classes.
- [ ] Ensure basic compilation.

## PHASE 1: Global Utilities & Core Logging
- [x] Migrate `LogKeeper.kt` to `core/` as a centralized singleton.
- [x] Set up centralized `IconManager` with Material placeholders.

## PHASE 2: Data Layer Foundation
- [x] Migrate Room Databases, DAOs, Repositories, and Entity models to `data/`.
- [x] Migrate `BootReceiver` and `MidnightResetReceiver` (network stats baseline).
- [x] Compile and verify data layer integrity.

## PHASE 3: Settings & Initial Configuration
- [x] Create Welcome/Permission flow. This flow must request all extensive permissions required by PWAs and mini-apps (Overlay, Storage, Camera, Audio, Location, Internet).
- [x] Pre-seed `SharedPreferences` with Handle 1 default config (Home Grid, Log Keeper, eReader).

## PHASE 4: Handle Service Extraction
- [x] Deconstruct God Service: Migrate edge detection and gesture parsing to `HandleService` (`core/`).
- [x] Ensure it runs continuously and remains incredibly lightweight.

## PHASE 5: Window Manager & Z-Order System
- [x] Establish the central `WindowManager` for handling floating view lifecycles.
- [x] Implement `onTrimMemory` logic for OS memory pressure fallback.

## PHASE 6: Sidebar Container & Selective Loading
- [x] Establish `SidebarManager` (`feature/sidebar/`) to listen for intents.
- [x] Build `SidebarView` container with ViewPager for lazy page instantiation (Freeze/Thaw UI logic).

## PHASE 7: Primary Grid Migration
- [x] Migrate standard pages: `AppsPageView`, `AppTrackerPageView`, etc.
- [x] Connect them to the Sidebar container.

## PHASE 8: Floating Apps & Utilities (Part 1)
- [x] Correct architecture: Revert Calculator and Compass to Sidebar Pages (`PageView`).
- [x] Rename `DictionaryPageView` to `DictionaryView` to reflect its Floating Window status.
- [x] Decouple `MiniAppManager` from page views.
- [x] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows.
- [x] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble).

## PHASE 8.5: Orphaned Sidebar Pages Catch-up
- [x] Migrate `MediaPlayerPageView` (Dockable Media Controls).
- [x] Migrate `WidgetPageView` (Dockable Android AppWidget host).
- [x] Wire both into `SidebarView.kt` router and settings menu.

## PHASE 9: The UI Spines (Settings & Add Element)
- [x] Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is from `reference/`.
- [x] Wire up migrated pages/services; leave unmigrated features as harmless stubs.
- [x] Prepare UI lists to send intents to our decoupled unified managers.

## PHASE 10: The Background System Hub (Plugins)
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
- [ ] Migrate eReader and Appywork sub-systems.

## PHASE 14: PWA Engine
- [ ] Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.

## PHASE 15: Polish, Launcher Prep & Finalization
- [ ] Implement Advanced Floating Grouping (Bubble Stacking/Collisions).
- [ ] Build the Backup & Restore JSON framework.
- [ ] Prepare architecture hooks for the external Android Launcher merge.
- [ ] Eradicate the `reference/` directory entirely.
