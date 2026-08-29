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
* Timestamp: 2026-08-13T11:45:00-07:00
* One-line summary: Fix gesture action update syncing, remove confusing Default/Active option from gesture pickers, and fit calculator and compass snugly to sidebar.
* Exact files touched: `app/src/main/java/com/example/feature/settings/HandlesListSettingsScreen.kt`, `app/src/main/java/com/example/utils/PageManager.kt`, `app/src/main/java/com/example/feature/sidebar/SidebarView.kt`, `app/src/main/res/layout/page_compass.xml`
* What was actually done:
  - Updated `updateGesture()` in `HandlesListSettingsScreen.kt` to synchronize the container's page configurations array and active page index when an action is reassigned, ensuring changing gesture actions immediately updates the rendered sidebar page.
  - Removed "Default / Current Active Page" from the gesture action selection dropdowns to prevent user confusion.
  - Set `wrapContentHeight = true` with optimal default dimensions in `PageManager.kt` and `SidebarView.kt` for both Calculator and Compass, eliminating blank vertical gaps.
  - Scaled up the Compass draw view to 220dp x 220dp in `page_compass.xml` so the dial cleanly fills the sidebar layout without leaving empty space or shrinking.
* How it was verified: local build only (compile_applet and gradle compileDebugKotlin passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-28T02:02:00-07:00
* One-line summary: Implement strict on-demand ML Kit lifecycle and complete heap reclaim on sidebar close.
* Exact files touched: `app/src/main/java/com/example/feature/miniapps/translation/TranslationWindowManager.kt`, `app/src/main/java/com/example/feature/miniapps/translation/TranslationPopupActivity.kt`, `app/src/main/java/com/example/feature/system_hub/QRCropActivity.kt`, `app/src/main/java/com/example/feature/system_hub/BarcodeScannerActivity.kt`, `app/src/main/java/com/example/feature/sidebar/SidebarView.kt`
* What was actually done:
  - Managed ML Kit lifecycle in `TranslationWindowManager`, `TranslationPopupActivity`, `QRCropActivity`, and `BarcodeScannerActivity` by explicitly invoking `.close()` on `Translator`, `LanguageIdentifier`, and `TextRecognition`/`BarcodeScanning` clients upon completion, cancellation, and dismiss/destroy to release native C++ model memory.
  - Implemented complete heap reclaim in `SidebarView.detach()` by unregistering `ViewPager2.OnPageChangeCallback`, invoking `release()` on all ViewHolders to unbind page views, nullifying adapters, clearing recycled view pools, clearing container/dot layouts, and unbinding all page managers.
* How it was verified: local build only (compile_applet passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-28T06:45:00-07:00
* One-line summary: Align CallRecorderManager SharedPreferences to FloatingReaderPrefs and wire live listening into HandleService lifecycle.
* Exact files touched: `app/src/main/java/com/example/feature/system_hub/CallRecorderManager.kt`, `app/src/main/java/com/example/core/HandleService.kt`
* What was actually done:
  - Aligned SharedPreferences in `CallRecorderManager.getInstance()` to use `"FloatingReaderPrefs"`, ensuring settings toggles in `CallRecorderSettingsScreen` immediately take effect.
  - Added safe `isListening` tracking and listener reset in `CallRecorderManager.startListening()` and `stopListening()`.
  - Wired `CallRecorderManager.getInstance(this).startListening()` into `HandleService.onCreate()`, `onSharedPreferenceChanged()`, and `stopListening()` into `HandleService.onDestroy()`.
* How it was verified: local build only (compile_applet passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-28T12:25:00-07:00
* One-line summary: Calculate status bar speed icon dimensions from device density and implement 70/30 snug vertical typography layout.
* Exact files touched: `app/src/main/java/com/example/core/DynamicSpeedIconGenerator.kt`
* What was actually done:
  - Added `getNotificationIconSize(context)` to compute `(24 * density).toInt().coerceAtLeast(48)` directly from display metrics.
  - Set `bitmap.density = densityDpi` on bitmap creation to prevent SystemUI bilinear downsampling distortion and blur.
  - Updated font rendering to bold Sans-Serif with anti-aliasing and subpixel flags enabled.
  - Implemented the exact 70/30 layout split (top speed value `sizePx * 0.58f` centered at 36% height; bottom unit `sizePx * 0.28f` centered at 82% height) with font metrics centering.
* How it was verified: local build only (compile_applet passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-28T13:00:00-07:00
* One-line summary: Implement on-demand Audio Record element with compact dockable pill panel (Pause/Play, Next, Stop & Rename, Close) and file storage routing.
* Exact files touched:
  - `app/src/main/res/drawable/ic_stop.xml`
  - `app/src/main/res/drawable/ic_rec_dot.xml`
  - `app/src/main/res/layout/overlay_audio_record.xml`
  - `app/src/main/java/com/example/feature/system_hub/AudioRecordFloatingPanel.kt`
  - `app/src/main/java/com/example/feature/sidebar/SidebarAppsManager.kt`
  - `app/src/main/java/com/example/feature/settings/HandlesListSettingsScreen.kt`
  - `app/src/main/java/com/example/feature/system_hub/VianSideAccessibilityService.kt`
  - `app/src/main/java/com/example/feature/sidebar/SidebarManager.kt`
  - `app/src/main/java/com/example/feature/sidebar/HybridGridPageView.kt`
  - `app/src/main/java/com/example/feature/sidebar/AppsPageView.kt`
  - `app/src/main/java/com/example/feature/system_hub/RecordingsActivity.kt`
* What was actually done:
  - Added "Audio Record" (`audio_record`) action under the "Record" category (`ALL_SCREEN_CAPTURE_ACTIONS`) in `SidebarAppsManager.kt` and element action mapping in `HandlesListSettingsScreen.kt`.
  - Created `overlay_audio_record.xml` with a pill-style design matching `overlay_auto_scroll.xml`, featuring pulsing red record indicator, live `mm:ss` counter, `Pause/Resume` toggle button, `Next` split button, `Stop` button, and `Close` button.
  - Implemented `AudioRecordFloatingPanel.kt` managing on-demand `MediaRecorder` lifecycle:
    - Auto-starts recording on show into `AUDIO_yyyyMMdd_HHmmss.m4a` inside designated `.Records` or SAF folder.
    - Pause/Resume: toggles `mediaRecorder.pause()` / `mediaRecorder.resume()` with dynamic icon switch and paused timer state.
    - Next: saves/finalizes current audio file immediately to storage and starts a fresh recording session on the spot with reset timer.
    - Stop: stops active recording, displays inline rename input field to customize file name, commits file, and saves.
    - Close: shuts down recording, cleans up resources, and removes the overlay from `WindowManager`.
  - Integrated `audio_record` execution into `VianSideAccessibilityService`, `SidebarManager`, `HybridGridPageView`, and `AppsPageView`.
  - Updated `RecordingsActivity.kt` to index `AUDIO_` files alongside call recordings.
* How it was verified: local build only (compile_applet passed)
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-29T06:39:30-07:00
* One-line summary: Phase 1: Downsampled icon loading and bounded LRU cache in SidebarAppsManager to optimize RAM footprint.
* Exact files touched: `app/src/main/java/com/example/feature/sidebar/SidebarAppsManager.kt`
* What was actually done:
  - Downsampled bitmap decoding and drawable loading in `getBitmapFromDrawable` to exact 48dp display dimensions (`Math.round(48f * density).coerceIn(48, 144)`).
  - Downsampled custom user icon decodings in `bindIcon` and `getIconBitmap` to 48dp thumbnails.
  - Adjusted `iconCache` max memory allocation to a tightly bounded footprint (`maxMemory / 48`, capped at 512KB - 1024KB).
* How it was verified: local build only (compile_applet passed).
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-29T07:47:30-07:00
* One-line summary: Phase 4: Added Copy Result button to Sidebar Calculator page.
* Exact files touched:
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/res/layout/page_calculator.xml`
  - `app/src/main/java/com/example/feature/sidebar/CalculatorPageView.kt`
* What was actually done:
  - Added `calculator_copy_result` and `calculator_result_copied` strings to `strings.xml`.
  - Added `btn_copy_result` ImageView button beside the result display in `page_calculator.xml` with subtle tint and touch feedback.
  - Implemented `copyResultToClipboard()` in `CalculatorPageView.kt` copying the active result or evaluated expression to Android `ClipboardManager` and displaying a user confirmation toast.
* How it was verified: local build only (compile_applet passed).
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-29T07:59:30-07:00
* One-line summary: Phase 2 & 3: Implemented dynamic handle-edge snapping, animated sidebar transitions, lazy page inflation, and closed-state widget host idling.
* Exact files touched:
  - `app/src/main/java/com/example/feature/sidebar/SidebarView.kt`
  - `app/src/main/java/com/example/feature/sidebar/SidebarManager.kt`
* What was actually done:
  - Added dynamic `isRight` determination in `SidebarView` bound to the triggering `physicalHandleId`, correctly setting `Gravity.START` or `Gravity.END` with matching corner radii and background contours.
  - Implemented dynamic slide-in and `closeWithAnimation()` transitions that emerge from and retreat toward the matching handle edge.
  - Ensured secondary pages in `SidebarPageViewHolder` remain uninflated until actively navigated to, conserving RAM and startup CPU cycles.
  - Confirmed and fortified `AppWidgetHelper.startListening()` on sidebar open and `AppWidgetHelper.stopListening()` / child cleanup on sidebar detach to prevent widgets from running in the background.
* How it was verified: local build only (compile_applet passed).
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-29T11:51:30-07:00
* One-line summary: Upgraded dynamic icon text rendering sharpness and high-density bitmap scaling.
* Exact files touched:
  - `app/src/main/java/com/example/feature/sidebar/SidebarAppsManager.kt`
* What was actually done:
  - Updated short text / dynamic icon generation with `SUBPIXEL_TEXT_FLAG`, `Typeface.BOLD`, centered alignment, and font metric vertical centering on a high-density $56\text{dp} \times \text{density}$ canvas.
  - Upgraded target icon bitmap cache resolution to scale up to $192\text{px}$ on high-density displays (xxxhdpi/xxhdpi), preventing bilinear upscaling blur.
  - Verified with `compile_applet`.
* How it was verified: local build only (compile_applet passed).
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-29T12:21:40-07:00
* One-line summary: Implemented multi-process overlay isolation and proactive memory trimming for background services.
* Exact files touched:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/example/feature/sidebar/SidebarAppsManager.kt`
* What was actually done:
  - Configured `android:process=":overlay"` for both `HandleService` and `SidebarService` in `AndroidManifest.xml` to isolate the background overlay service from the main Compose/Settings process memory space.
  - Added `trimMemory(level)` in `SidebarAppsManager` to evict cached icon bitmaps when system trim memory events occur.
  - Verified clean build and full runtime compilation with `compile_applet`.
* How it was verified: local build only (compile_applet passed).
* Deviations: None.
* Known issues: None.
* Timestamp: 2026-08-29T13:32:00-07:00
* One-line summary: Locked in calibrated dynamic icon parameters for Xiaomi/Redmi 24dp status bar canvas and stripped customization sliders from NetSpeed settings.
* Exact files touched:
  - `app/src/main/java/com/example/core/DynamicSpeedIconGenerator.kt`
  - `app/src/main/java/com/example/feature/settings/NetSpeedSettingsScreen.kt`
  - `receipts/RECEIPTS_093.md`
* What was actually done:
  - Calibrated default `IconConfig` to `numScale = 0.88f`, `unitScale = 0.95f`, `letterSpacing = -0.02f`, and `resScale = 2.0f` to prevent edge clipping on 3-digit and decimal numbers on 48px status bar icons.
  - Stripped all customization sliders, font pickers, background shape options, supersampling/AA controls, test preview cards, and calibration tool UI from `NetSpeedSettingsScreen.kt`.
  - Retained clean core controls: Master speed monitor toggle, speed units (Auto/KB/s/MB/s), data usage units, and app data usage breakdown with daily/weekly/monthly filter and search.
  - Verified clean build with `compile_applet`.
* How it was verified: local build only (compile_applet passed).
* Deviations: None.
* Known issues: None.









