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
- [ ] Deconstruct God Service: Migrate edge detection and gesture parsing to `HandleService` (`core/`).
- [ ] Ensure it runs continuously and remains incredibly lightweight.

## PHASE 5: Window Manager & Z-Order System
- [ ] Establish the central `WindowManager` for handling floating view lifecycles.
- [ ] Implement `onTrimMemory` logic for OS memory pressure fallback.

## PHASE 6: Sidebar Container & Selective Loading
- [ ] Establish `SidebarManager` (`feature/sidebar/`) to listen for intents.
- [ ] Build `SidebarView` container with ViewPager for lazy page instantiation (Freeze/Thaw UI logic).

## PHASE 7: Primary Grid Migration
- [ ] Migrate standard pages: `AppsPageView`, `AppTrackerPageView`, etc.
- [ ] Connect them to the Sidebar container.

## PHASE 8: Floating Apps & Utilities (Part 1)
- [ ] Migrate essential mini-apps (Calculator, Dictionary, Translation, Compass, etc.).
- [ ] Apply unbind/rebind Freeze/Thaw mechanics.
- [ ] Configure `PROCESS_TEXT` and `SEND` intent filters in `AndroidManifest.xml` for Dictionary, Translation, Read Aloud, and Browser so they appear in Android's native text selection context menu.
- [ ] Migrate standalone system overlays like `NetSpeedManager`, hooking them into the WindowManager framework.

## PHASE 9: Floating Apps & Utilities (Part 2)
- [ ] Migrate heavy mini-apps (eReader, Call Recorder, etc.).
- [ ] Implement Call Recorder (`CallRecorderManager`) as a highly decoupled, modular feature that can be completely disabled or removed without breaking other app functionality.
- [ ] Migrate Appywork as an isolated, modular Vibe Coding Hub. Ensure its `PROCESS_TEXT` intent, Git API stateless push, and embedded NanoHTTPD features are cleanly decoupled.
- [ ] Migrate local HTTP server (NanoHTTPD) and PWA Loader architecture to support multiple concurrent floating WebView instances.
- [ ] Migrate `VianSideAccessibilityService` utilities (Cursor, Auto-Scroll, Long Screenshot) and `ScreenRecordService`.
- [ ] Migrate `AppNotificationListener` for notification tracking features.

## PHASE 10: Advanced Floating Grouping
- [ ] Implement "Floating Button Stacking" logic for dragged/dropped bubbles via `ACTION_UP` bounding box collisions.

## PHASE 11: Granular Backup Engine
- [ ] Build the modular JSON/ZIP selective Backup & Restore system.

## PHASE 12: Finalization & Legacy Eradication
- [ ] Run global `grep` to ensure zero dependencies on `reference/`.
- [ ] Delete the `reference/` directory entirely.
- [ ] Final `gradle assembleDebug`.
