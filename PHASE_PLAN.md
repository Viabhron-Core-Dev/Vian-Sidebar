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

## PHASE 10: The Background System Hub (Plugins & Accessibility)
- [x] Establish `VianSideAccessibilityService` as a unified System Tools Hub.
- [x] Implement module orchestrator for `CursorManager`, `AutoScrollManager`, `LongScreenshotManager`, and `AppKillerManager` (loaded STRICTLY on-demand, no background memory leak).
- [x] Migrate `CallRecorderManager` as a completely decoupled, dormant module.
- [x] Migrate `NetSpeedManager` to update the foreground notification dynamically. (CRITICAL: No overlay. Tied to `ACTION_SCREEN_ON`/`OFF` to save battery).
- [x] Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`).

## PHASE 11: Sidebar On-Demand Optimization (COMPLETED)
- [x] Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen).
- [x] Migrate remaining dual-mode pages: `SchedulerPageView`, `NotificationPageView`, `ResourcesTrackerPageView`.

## PHASE 12: Unified Z-Window Manager & OS Popups
- [x] Build a centralized Z-Window Manager (handling Z-Ordering and Dormant Folding/Bubbles).
- [ ] Migrate Text Selection Popups (`DictionaryPopupActivity`, `TranslationPopupActivity`).
- [ ] Migrate the Lightweight Floating Browser (Share Intents).

## PHASE 13: Heavy Floating Mini-Apps & Appywork (Pre-PWA Bridge)
- [ ] **Phase 13.1: Heavy Native Floating Windows**
    - [ ] `FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`.
    - [ ] `CursorManager` (Trackpad) UI, `WorkNotesWindowManager`.
- [ ] **Phase 13.2: eReader Subsystem**
    - [ ] Migrate eReader floating reading canvas, EPUB engine, and reading settings.
- [ ] **Phase 13.3: Appywork (Isolated & Deprecatable Vibe-Coding Module)**
    - [ ] Migrate `AppyworkWindowManager`, `AppyworkFloatingService`, `AppyworkParser`, and `AppyworkDao` strictly isolated inside `feature/appywork/`.
    - [ ] Use dedicated `AppyworkDatabase` so app DB schema remains clean.
    - [ ] Wire via decoupled Intent Action (`ACTION_OPEN_APPYWORK`) so it can be excised in a single step with zero side-effects when the external AI Host App replaces its functionality.

## PHASE 14: PWA Engine & External AI App Integration
- [ ] **Phase 14.1: Local PWA Runner Core**
    - [ ] Migrate `PwaServer` (NanoHTTPD local server), `PwaWindowManager`, `PwaDatabase`, and `PwaImportActivity` from `reference/` to `feature/pwa/`.
- [ ] **Phase 14.2: Dual-Mode Execution (Lightweight vs Heavy)**
    - [ ] Floating Window (`isLightweight = true`): Draggable, resizable, dockable overlay via `PwaWindowManager`.
    - [ ] Fullscreen Container (`isLightweight = false`): Dedicated `PwaActivity` WebView wrapper.
- [ ] **Phase 14.3: Remote AI Host App IPC & ContentProvider Sync**
    - [ ] Implement `RemotePwaRepository` using `ContentResolver` to query `content://<ai_app_authority>/pwas`.
    - [ ] Stream PWA files via `ParcelFileDescriptor` (`openFile`) without copying assets to disk.
    - [ ] Update `SidebarBridge.kt` so `saveData()` syncs state back to the external AI App's database.
- [ ] **Phase 14.4: (Future Hook) Headless Local AI Inference Client**
    - [ ] Define AIDL interfaces (`IAiInferenceService.aidl`, `IAiCallback.aidl`) in `com.example.ai`.
    - [ ] Provide optional floating chat bubble binding to the external AI Host App for background token streaming without launching full UI.

## PHASE 15: Polish, Launcher Prep & Finalization
- [ ] Implement Advanced Floating Grouping (Bubble Stacking/Collisions).
- [ ] Build the Backup & Restore JSON framework.
- [ ] Prepare architecture hooks for the external Android Launcher merge.
- [ ] Eradicate the `reference/` directory entirely.
