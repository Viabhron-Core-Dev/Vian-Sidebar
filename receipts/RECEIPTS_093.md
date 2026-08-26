* Timestamp: 2026-08-13T10:53:39-07:00
* One-line summary: Fix Handle color override and Sidebar color preset bug.
* Exact files touched: `app/src/main/java/com/example/core/HandleManager.kt`, `app/src/main/java/com/example/feature/settings/SidebarSettingsScreen.kt`
* What was actually done: Removed hardcoded purple color default for first launch in `HandleManager` to sync with new slanted block #242962ff defaults. Updated `SidebarSettingsScreen` to default to #1E1E2E instead of pure black #000000 and added #1E1E2E to the list of 7 color preset options to perfectly align with `SidebarView` defaults.
* How it was verified: local build only (gradle :app:compileDebugKotlin passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-13T11:05:00-07:00
* One-line summary: Fix unhandled intent crash by registering missing editor activities and wire up dead settings edit buttons.
* Exact files touched: `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/example/feature/settings/PageCustomizeScreen.kt`
* What was actually done: Registered missing Edit activities in AndroidManifest (HybridGrid, WidgetsGrid, SidebarEdit, AppTrackerSettings) to fix ActivityNotFoundException crash. Updated PageCustomizeScreen to conditionally map the "EDIT APPS/GRID/WIDGETS" buttons to launch those real activities with proper PAGE_ID intents, and added missing UI support for configuring the App Tracker page from the Settings menu.
* How it was verified: local build only (gradle :app:compileDebugKotlin passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-13T11:21:40-07:00
* One-line summary: Clean up global container settings and implement Universal Edit Mode fallback.
* Exact files touched: `app/src/main/java/com/example/feature/settings/SidebarSettingsScreen.kt`, `app/src/main/java/com/example/feature/sidebar/SidebarView.kt`, `app/src/main/java/com/example/feature/settings/SettingsActivity.kt`
* What was actually done: Removed redundant layout sliders (Width, Height, Wrap Content Height) and dead variables from SidebarSettingsScreen.kt so it now purely acts as an Appearance / Page Management manager. Updated SidebarView.kt so clicking Edit on generic pages passes an `edit_page:PAGE_ID` payload. Updated SettingsActivity.kt to parse that payload and inject it directly into the initialEditPageId parameter of SidebarSettingsScreen.kt to launch the exact page customization sheet automatically.
* How it was verified: local build only (gradle :app:compileDebugKotlin)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-26T01:05:00-07:00
* One-line summary: Fix multi-page sidebar loading / swiping and port Battery-Indicator-Pro exact dynamic text rendering.
* Exact files touched: `app/src/main/java/com/example/utils/PageManager.kt`, `app/src/main/java/com/example/core/DynamicSpeedIconGenerator.kt`, `app/src/main/java/com/example/feature/settings/NetSpeedSettingsScreen.kt`
* What was actually done: Fixed page configuration lookup fallback in PageManager so added pages are properly loaded when handles trigger the sidebar, enabling multiple pages, swipe transitions, and dot indicators. Ported Battery-Indicator-Pro exact Rect.getTextBounds integer-aligned baseline text rendering with crisp paint flags to eliminate blur on low-DPI displays. Added one-tap Compact and Stacked ultra-sharp preset chips to NetSpeedSettingsScreen.
* How it was verified: local build only (compile_applet passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-26T01:31:30-07:00
* One-line summary: Implement aggressive on-demand ML Kit lifecycle disposal and complete SidebarView memory release.
* Exact files touched: `app/src/main/java/com/example/feature/system_hub/BarcodeScannerActivity.kt`, `app/src/main/java/com/example/feature/system_hub/QRCropActivity.kt`, `app/src/main/java/com/example/feature/miniapps/translation/TranslationWindowManager.kt`, `app/src/main/java/com/example/feature/sidebar/SidebarView.kt`
* What was actually done: Implemented explicit close()/disposal for ML Kit TextRecognizer and BarcodeScanner in BarcodeScannerActivity, QRCropActivity, and TranslationWindowManager (including LanguageIdentifier and Translator lifecycle cleanup) immediately after task completion and view destruction. Enhanced SidebarView detach routine to explicitly notify child pages, null out view holders, remove all child frames, and clear ViewPager adapter so full heap memory is reclaimed on sidebar close.
* How it was verified: local build only (compile_applet passed)
* Deviations: None.
* Known issues: None.

