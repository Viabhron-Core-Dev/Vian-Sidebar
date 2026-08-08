2026-08-02T17:03:00Z
- Requested: Fix Dictionary floating window element in Sidebar opening permission screens instead of the dictionary window.
- Modified: `HybridGridPageView.kt`
- Action:
  1. Refactored the `SystemAction` click handler in `HybridGridPageView` to match `AppsPageView`. It now routes all system actions (like `dictionary_floating`, `dictionary_full`, `work_notes`) through `SidebarService` using `EXECUTE_ACTION`, rather than falling back to the Accessibility Service dispatcher for unhandled actions. This fixes the issue where clicking the dictionary from a hybrid page would fail and launch the accessibility settings screen (which the user misidentified as overlay permissions).
- Verified: Local build only (BUILD SUCCESSFUL).
