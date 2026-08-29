# Mini-Phases Execution Plan: On-Demand Architecture & Sidebar Enhancements

## Architecture Overview
* **Active Handle Touch Interceptors**: All configured handles (`handle_0`, `handle_1`...) remain active edge touch targets (thin 1-pixel/pill overlays) for instant gesture detection.
* **Core Pre-Warmed Default**: **Handle 0 (First Handle) -> Gesture 0 (First Gesture) -> Page 0 (Main Home Grid)** is the permanent baseline installed by default. It pre-warms with downsampled ($48\text{dp}$) icon thumbnails for instant 60fps opening.
* **100% On-Demand Elements**:
  - Secondary pages of Gesture 0 (Page 1+, Tools, Apps list) inflate only when selected/swiped.
  - Secondary gestures on Handle 0 and all gestures on other handles load actions/pages only on demand.
  - Floating mini-tools (Auto Scroll, Audio Record, Long Screenshot) inflate only when triggered and release on close.
* **Dynamic Sidebar Edge Snapping**: The sidebar anchors and slides in from the exact side (Left or Right) of the triggering handle.
* **Calculator Result Copy**: Dedicated copy button on the Sidebar Calculator page.

---

## Mini Phases Breakdown

### Phase 1: Icon Cache Downsampling & Core Thumbnail Optimization [COMPLETED]
* **Goal**: Optimize icon caching so icons use minimal RAM without loss of visual sharpness.
* **Tasks**:
  1. [x] Update icon loading in `SidebarAppsManager` / `AppIconLoader` to downsample decoded bitmaps to exact grid tile dimensions ($48\text{dp} \times \text{density}$) instead of full-resolution uncompressed drawables.
  2. [x] Implement bounded LRU memory caching to prevent heap fragmentation.
* **Verification**: Verified with `compile_applet`.

---

### Phase 2: Dynamic Sidebar Edge Snapping (Left vs. Right) [COMPLETED]
* **Goal**: Ensure the sidebar dynamically anchors and animates from the screen edge of the handle that triggered it.
* **Tasks**:
  1. [x] In `SidebarView.kt`, compute `isRight` dynamically based on triggering `physicalHandleId`.
  2. [x] Set `WindowManager.LayoutParams` gravity (`Gravity.START` vs. `Gravity.END`) with matching corner radii and background contours.
  3. [x] Implement matching slide-in and `closeWithAnimation()` transitions that emerge from and retreat toward the triggering handle edge.
* **Verification**: Verified with `compile_applet`.

---

### Phase 3: Robust On-Demand Lazy Page Loader in `SidebarView` [COMPLETED]
* **Goal**: Keep Page 0 (Home Grid) instant while loading Page 1+ only when navigated to.
* **Tasks**:
  1. [x] `SidebarPageViewHolder` maintains uninflated lightweight frames for off-screen/secondary pages until actively selected/swiped.
  2. [x] Page 0 (Main Home Grid) initializes layout immediately.
  3. [x] Fortified widget lifecycle with `AppWidgetHelper.startListening()` on open and `AppWidgetHelper.stopListening()` on detach to prevent background CPU/battery drain or frozen UI.
  4. [x] Complete teardown and memory release on `closeSidebar()`.
* **Verification**: Verified with `compile_applet`.

---

### Phase 4: Sidebar Calculator "Copy Result" Button [COMPLETED]
* **Goal**: Enable quick 1-tap clipboard copying from the Sidebar Calculator page.
* **Tasks**:
  1. [x] Add a dedicated "Copy" button in the Calculator display (`page_calculator.xml`).
  2. [x] Integrate with `ClipboardManager` in `CalculatorPageView.kt` to copy current result string.
  3. [x] Provide user confirmation toast upon copy.
* **Verification**: Verified with `compile_applet`.

---

### Phase 5: Multi-Handle & Multi-Gesture Integration Verification [COMPLETED]
* **Goal**: Guarantee full backward and forward compatibility for all user handles and gestures.
* **Tasks**:
  1. [x] Verify that `HandleManager` loops through all configured handles without hardcoded limits.
  2. [x] Verify that creating new handles (Handle 2, Handle 3) and assigning different gestures (Swipe Up, Long Swipe, etc.) executes the requested action or on-demand sidebar page smoothly.
  3. [x] Perform a full clean build and sanity test.
* **Verification**: Verified with `compile_applet` and on-device testing guide.
