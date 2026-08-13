# Vian Sidebar - Architecture Blueprint

## 1. Staging & Build Configuration
*   **Exclude from Build:** Ensure the new Gradle configuration completely ignores the `reference/` (legacy code) folder so the APK builds cleanly without duplicating classes or compiling legacy code. (The folder is located outside the app module, so it is automatically excluded by Gradle, but we must ensure it remains ignored).

## 2. General Architecture & Structure
Rather than defining deep, complex rules, follow a "Package by Feature" physical directory structure, while keeping namespaces relatively flat to avoid import hell during the migration.
*   `core/` (Services, Utilities, Memory Management)
*   `data/` (Room Databases, Entities, Repositories)
*   `feature/<feature_name>/` (Sidebar, Floating Windows, Pages, Settings)

## 3. Core Systems & Capabilities to Implement
As we rebuild features from the reference folder, we will integrate the following architectural requirements:

*   **Omnipresent Log Keeper:** 
    The Log Keeper must be centralized and connected to *everything*. It should capture events, errors, and state changes across all services and mini-apps.
*   **Selective Backup Engine:**
    Implement a comprehensive backup/restore system for the full app. This system must include options to exclude heavy data (e.g., let the user leave out imported EPUB books or PWA zips, but retain settings, UI configurations, and database records). Implementation: A custom BackupManager serializes SharedPreferences and Room tables to a `settings.json` file, presents a checklist UI, and selectively packages assets via ZipOutputStream.
*   **Floating Button Stacking (Grouping):**
    If floating bubbles (icons/mini-apps) are dragged and dropped onto one another, group them into a simple "Stack". Do not create a complex new folder UI/icon; just visually stack the existing buttons. Moving the top button moves the group. Tapping the stack ungroups them. Implementation: The centralized WindowManager tracks screen coordinates of active bubbles and calculates bounding box intersections on `ACTION_UP`.
*   **PWA Loader & Local Server (NanoHTTPD):**
    A core feature is the ability to load and run PWAs (Progressive Web Apps) in isolated, floating WebView windows. This requires a local HTTP server (NanoHTTPD) to serve the PWA assets securely to the WebViews. The PWA loader must support multiple concurrent floating instances.
*   **Comprehensive Permission Management:**
    Because the PWAs and floating utilities require extensive access, the Onboarding/Settings flow must robustly handle requesting a wide array of permissions: `SYSTEM_ALERT_WINDOW` (overlay), `INTERNET`, Location (Fine/Coarse), Storage (`READ/WRITE/MANAGE`), Camera, and `RECORD_AUDIO`. This must be built into the Phase 3 Welcome flow.
*   **Context Menu Integration (`PROCESS_TEXT` & `SEND`):**
    Specific utilities (Dictionary, Translation, Read Aloud, Browser) must register intent filters for Android's native text selection context menu (`PROCESS_TEXT`) and share sheet (`SEND`). This allows users to highlight text in any app and send it directly to Vian Sidebar's floating windows.
*   **Call Recorder Modularity:**
    The Call Recorder (`CallRecorderManager`) must be built as a strictly isolated, decoupled module. Because call recording APIs and permissions are highly volatile across Android versions, the architecture must allow this feature to be smoothly removed or disabled without breaking any other components.
*   **Appywork (Vibe Coding Hub):**
    A specialized, modular floating code editor and Git management overlay (Appywork). It must be strictly isolated and easy to remove from the app, similar to the Call Recorder. It interacts with mobile browsers via `PROCESS_TEXT` ("Push via Appywork"), uses the WindowManager for floating UI, includes an embedded NanoHTTPD server for PWA preview, and performs stateless push operations via GitHub APIs.
*   **System Overlays (NetSpeed):**
    Standalone system indicators like the `NetSpeedManager` must strictly dynamically update the foreground notification icon ONLY. There is no floating overlay required. It must be tied to `ACTION_SCREEN_ON`/`OFF` to save battery.
*   **Accessibility Suite & Screen Recording:**
    The `VianSideAccessibilityService` must act as a modular orchestrator. Utilities that rely on it (Cursor, Auto-Scroll, Long Screenshot) and `MediaProjection` (Screen Record) must be decoupled and loaded *strictly on-demand*. They should only occupy memory when explicitly triggered.
*   **Background Event Receivers:**
    Network stats monitoring (midnight reset), notification tracking (`NotificationListenerService`), and boot triggers (`BootReceiver`) must be migrated as lightweight background tasks that do not inflate the memory footprint of the core `HandleService`.
*   **Iconography Placeholders:**
    Keep the architecture ready for custom icons (floating window icons, in-app mini-apps, elements, and tools). Use standard Material icons as placeholders for now, but build the UI components so swapping them for custom drawables later is a seamless process.

## 4. Deep Hierarchy & Strict Isolation
The application follows a strictly isolated, hierarchical structure. Components do not share state across boundaries.
*   **Onboarding & Settings Entry:** When the app is first opened, the user is presented with the Welcome/Permission page (Settings page). From here, they access the Handle and Sidebar settings/management pages.
*   **Default Handle Configuration:** During the initial onboarding/Welcome flow, pre-seed SharedPreferences with the following configuration: The first handle (Handle 1) is created by default. Its default gesture is "swipe left", triggering a "Home Grid" sidebar (a hybrid grid with Log Keeper and eReader elements added by default, laid out in 3 columns). Any newly created handles are completely empty by default.
*   **Multiple Isolated Handles:** A Handle acts like an isolated root folder. Handle 1 and Handle 2 contain completely different content and configurations. There is NO sharing of state or items between different handles.
*   **Hierarchical Flow:**
    *   **Handle** -> Detects **Gesture**
    *   **Gesture** -> Triggers **Element (Action)** OR **Sidebar Container**
    *   **Sidebar Container** -> Contains multiple **Sidebar Pages** (configured in Sidebar Settings)
    *   **Sidebar Page** -> Launches **Floating Windows or Mini-Apps**
*   **Sidebar Container & Edit Button:** The Sidebar is a container that holds one or more pages. Gestures can directly trigger elements, or they can open a Sidebar Container. Pages are configured within the Sidebar settings, not as direct gesture options. It has an "Edit" button that is *page-aware*. The action triggered by the Edit button is defined by the currently active page.
*   **Strict Separation:** Every gesture within a handle is independent. Sidebars of two different gestures are completely separate.

## 5. Development & Code Re-use Policy
*   **Reuse Original Code:** Avoid building entirely new files from scratch where possible. Prefer migrating, adjusting, and customizing the original (OG) code from the `reference/` folder.
*   **Create New When Necessary:** Only create new files if it's strictly necessary for a simple, workable architecture, but always default to leveraging existing code first.

## 6. Deconstructing the "God Service"
The legacy architecture relied on massive, monolithic services handling everything from gestures to UI grids to floating windows. We must decouple these:
*   **Handle Service (core/):** All active handles run continuously (they are *never* loaded "on demand") because they must instantly detect edge gestures. This service must remain incredibly lightweight. Upon detecting a gesture, it broadcasts the trigger to the Sidebar Manager.
*   **Sidebar Manager (feature/sidebar/):** Listens for the trigger, retrieves the page configurations for that specific `handle_gesture` container ID, and builds the `SidebarView`.
*   **Window Manager (Z-Order):** A central system to manage the lifecycle, layering (Z-index), and focus of mini-apps and floating utilities.

## 7. Memory, State, & Resource Consumption
*   **The Freeze/Thaw Lifecycle:** Floating windows (mini-apps) consume heavy resources. When a window is minimized or "folded" into a floating bubble, we must destroy/detach its heavy UI View layer but retain its lightweight Data Model in memory. When unfolded, rebuild the UI from the frozen data state.
*   **Selective Loading (Semi-Loaded vs. On-Demand):** *Only* the first handle's (Handle 1) default sidebar is semi-loaded (keeping its data model cached for instant response). All other sidebars and pages across the application must be loaded strictly *on-demand* (e.g., lazily instantiated in a ViewPager only when swiped into view) to minimize memory overhead. They must load as quickly as possible when requested, and immediately unload their heavy View layer when off-screen. Note: **Handles** themselves are *never* loaded on demand; they remain continuously active.
*   **System Memory Pressure:** Implement `onTrimMemory` in the core service. If the Android OS signals memory pressure, gracefully kill background data models and folded windows, falling back entirely to just the Handle.

## 8. Phased Execution Plan

**PHASE 0: Staging & Build Configuration**
*   Create base directory structure: `core/`, `data/`, `feature/` under the new flat namespace.
*   Configure Gradle to exclude `reference/` entirely to prevent duplicate classes.
*   Ensure basic compilation.

**PHASE 1: Global Utilities & Core Logging**
*   Migrate `LogKeeper.kt` to `core/` as a centralized singleton.
*   Set up centralized `IconManager` with Material placeholders.

**PHASE 2: Data Layer Foundation**
*   Migrate Room Databases, DAOs, Repositories, and Entity models to `data/`.
*   Compile and verify data layer integrity.

**PHASE 3: Settings & Initial Configuration**
*   Create Welcome/Permission flow. This flow must robustly handle requesting all necessary permissions for floating apps and PWAs (Overlay, Storage, Camera, Audio, Location, etc.).
*   Pre-seed `SharedPreferences` with Handle 1 default config (Home Grid, Log Keeper, eReader).

**PHASE 4: Handle Service Extraction**
*   Deconstruct God Service: Migrate edge detection and gesture parsing to `HandleService` (`core/`).
*   Ensure it runs continuously and remains incredibly lightweight.

**PHASE 5: Window Manager & Z-Order System**
*   Establish the central `WindowManager` for handling floating view lifecycles.
*   Implement `onTrimMemory` logic for OS memory pressure fallback.

**PHASE 6: Sidebar Container & Selective Loading**
*   Establish `SidebarManager` (`feature/sidebar/`) to listen for intents and launch the appropriate Sidebar or Action Element based on the Gesture.
*   Build `SidebarView` container that holds one or more pages (managed via Sidebar Settings).
*   Implement ViewPager for lazy page instantiation (Freeze/Thaw UI logic). *Note: Ensure Handle 1's default sidebar remains semi-loaded for instant response, while all other sidebars and pages load strictly on-demand.*

**PHASE 7: Primary Grid Migration**
*   Migrate standard pages: `AppsPageView`, `AppTrackerPageView`, etc.
*   Connect them to the Sidebar container.

**PHASE 8: Floating Apps & Utilities (Part 1)
* [x] Correct architecture: Revert Calculator and Compass to Sidebar Pages (`PageView`).
* [x] Rename `DictionaryPageView` to `DictionaryView` to reflect its Floating Window status.
* [x] Decouple `MiniAppManager` from page views.
* [x] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows.
* [x] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble).

**PHASE 9: The UI Spines (Settings & Add Element)**
*   Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is from `reference/`.
*   Wire up migrated pages/services; leave unmigrated features as harmless stubs.
*   Prepare UI lists to send intents to our decoupled unified managers.

**PHASE 10: The Background System Hub (Plugins & Accessibility)**
*   Establish `VianSideAccessibilityService` as a unified System Tools Hub (orchestrator for Cursor, AutoScroll, LongScreenshot, loaded strictly on-demand).
*   Migrate `CallRecorderManager` as a 100% dormant plugin, waking only via `TelephonyManager`.
*   Migrate `NetSpeedManager` as a screen-aware plugin (`ACTION_SCREEN_ON`/`OFF`) updating the notification only (NO overlay).
*   Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`).

**PHASE 11: Sidebar On-Demand Optimization**
*   Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen).
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
---

*   **2026-08-11:** Implemented Phase 8.5: Migrated `MediaPlayerPageView` and `WidgetPageView`, wired them to `SidebarView.kt`, and added to Settings dropdowns.

*   **2026-08-11:** Created Phase 8.5 to catch up on orphaned Sidebar Pages (`MediaPlayerPageView`, `WidgetPageView`) missed during Phase 7 grid migration.

*   **2026-08-11:** Executed remainder of Phase 8: Migrated universal PageWindow wrapper and FloatingTriggerService. Phase 8 complete.

*   **2026-08-11:** Updated Phase 9 blueprints to enforce strict architectural boundaries: separated Dual-Mode Pages (Scheduler, Notifications, Resources Tracker) from Floating-Only PageWindows (File Explorer, Local Terminal, Termux) which cannot be docked in the Sidebar.

*   **2026-08-11:** Identified missing architecture in Floating Apps (Phase 8/9): Universal PageWindowManager, FloatingTriggerService, standalone floating windows (Cursor, Work Notes, PWA Loader), remaining Sidebar Pages (Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux), OS text selection popups, and Share-to-App lightweight browser. Corrected Calculator/Compass to Sidebar Pages and renamed DictionaryView.

## Migration Ledger
*   **2026-08-08:** Modified blueprint and phase plan to explicitly detail background receivers, accessibility services, notification listeners, and the Appywork (Vibe Coding Hub) module.
*   **2026-08-08:** Executed Phase 1 - Created `core` package, migrated `LogKeeper` and extracted `App`, and created centralized `IconManager` with Material placeholders.
*   **2026-08-08:** Executed Phase 2 - Migrated Data Layer Foundation (Room DAOs, Entities) and core receivers (`BootReceiver`, `MidnightResetReceiver`). Built successfully.
*   **2026-08-08:** Executed Phase 3 - Created combined Welcome/Settings `PermissionManagerScreen` requesting all extensive permissions. Pre-seeded `SharedPreferences` with default Handle 1 configuration. Built successfully.
*   **2026-08-08:** Optimization: Swapped ML Kit dependencies for Google Play Services unbundled equivalents to massively reduce APK size. Removed `com.google.mlkit:translate` entirely for now (no unbundled version exists); it will be re-added when the translation floating window and popup are implemented.
*   **2026-08-08:** Executed Phase 4 - Deconstructed God Service by extracting `HandleManager`, `HandleShapeDrawable`, and `TriggerHandleView` into the `core/` and `util/` packages. Implemented `HandleService` as an independent, lightweight foreground service that manages gesture detection and dispatches intents to standard services. Removed hardcoded `instance` dependencies. Built successfully.
*   **2026-08-08:** Executed Phase 5 - Established `FloatingWindowManager` and `FloatingWindow` base class in `core/` package. Implemented XML-based container `layout_floating_window_container.xml` with topbar drag/fullscreen and bottom-right controls (Close, minimize, resize). Created folded "bubble" state. Integrated `onTrimMemory` in `HandleService` to fold windows under memory pressure. Built successfully.
*   **2026-08-09:** Executed Phase 6 - Established `SidebarManager` and `SidebarView` in `feature/sidebar/`. Implemented ViewPager2 for lazy loading with freeze/thaw logic. Built successfully.
*   **2026-08-09:** Executed Phase 7 - Migrated standard page grids (`AppsPageView`, `HybridGridPageView`, `WidgetsGridPageView`, `AppTrackerPageView`) and `SidebarAppsManager`. Linked them to `SidebarView` via lazy instantiation. Mocked un-migrated utility classes for stable compilation. Built successfully.
