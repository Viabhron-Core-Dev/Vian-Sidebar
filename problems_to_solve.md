# Vian Sidebar - Migration & Restructuring Problems to Solve

## 1. Splitting the Monolithic "SidebarService"
**Current State:** `SidebarService.kt` is the "God Service". It manages the edge triggers (`TriggerHandleView`), parses the swipe gestures, queries `PageManager`, instantiates all the page views (e.g., `AppsPageView`, `HybridGridPageView`), constructs the `SidebarView` container, attaches it to the Android `WindowManager`, and handles the page-aware "Edit" button callbacks.
**Problem:** This violates the blueprint's "Deconstructing the God Service" mandate.
**Solution Required:** We must split this into:
*   **HandleService (core/):** Only manages the edge handles and detects gestures. Upon gesture, it broadcasts an intent or calls a singleton manager.
*   **SidebarManager (feature/sidebar/):** Listens for the trigger, retrieves the page configurations for that specific `handle_gesture` container ID, and builds the `SidebarView`.

## 2. Memory Management in SidebarView (Semi-Loaded Grids)
**Current State:** `SidebarView.kt` takes a list of pre-instantiated Views (`pagesList`) and adds them all to a `ViewPager2` or internal list. This means if a user has 5 pages in a sidebar, all 5 heavy UI layouts are loaded into memory simultaneously.
**Problem:** This violates the "Freeze/Thaw" and "Semi-Loaded Grids" requirements.
**Solution Required:** We need to update `SidebarView` and its adapter to lazily instantiate pages only when they are swiped into view, and destroy the View layer (keeping only the lightweight data model) when they are off-screen or when `onTrimMemory` is triggered.

## 3. Initial Configuration (Default Handle & Welcome Flow)
**Current State:** `HandleManager.kt` creates a default handle if `handles_list` is missing.
**Problem:** The blueprint requires specific defaults: "Handle 1 (default) -> Swipe Left -> Home Grid (Hybrid Grid with Log Keeper and eReader added by default, 3 columns)".
**Solution Required:** During the new onboarding/Welcome flow, we must pre-seed `SharedPreferences` (or the database) with this exact default configuration, ensuring that subsequent handles created are fully empty.

## 4. Floating Button Stacking (Grouping)
**Current State:** The legacy codebase has no concept of grouping floating icons by dragging them together.
**Problem:** This is a completely new feature requested in the blueprint.
**Solution Required:** We need to implement a centralized `WindowManager` (Z-Order) that tracks the screen coordinates of all active floating bubbles. In the `OnTouchListener`'s `ACTION_UP` event, we must calculate bounding box intersections. If they intersect, remove both individual bubbles and spawn a new "Stack" bubble that expands radially or linearly when clicked.

## 5. Granular Backup Engine
**Current State:** `BackupHelper.kt` likely just copies files or the DB entirely.
**Problem:** The blueprint requests a granular system (ZIP) with a checklist UI to exclude heavy assets (EPUBs, PWAs) while retaining JSON settings.
**Solution Required:** Build a custom `BackupManager` that serializes `SharedPreferences` and Room tables to a `settings.json` file, presents a checklist UI, and then uses `ZipOutputStream` to selectively package only the chosen physical asset folders along with the JSON file.
