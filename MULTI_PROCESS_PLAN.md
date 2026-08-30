# Multi-Process Architecture & Gesture Container System Plan

## 1. Executive Summary & Core Objectives
- **Problem**: When memory-heavy apps (such as Chromium / AI Studio web editor) run in the foreground, Android's Low Memory Killer (LMK) force-closes or reclaims the monolithic background service.
- **Solution**: 
  1. Separate into a **3-Process Model** (`:core`, `:sidebar`, `:ui`).
  2. Implement an **Ultra-Lightweight Background Daemon** (`:core`) locked at ~15-20MB RAM with zero UI view bloat.
  3. Implement **Downsampled WebP Icon Disk Caching** (48x48 / 64x64) to eliminate main-thread Binder IPC blocking during gestures.
  4. Enforce strict **Gesture Container Isolation** (`containerId = "${handleId}_${gesture}"`), where each gesture on any handle owns an independent page deck and layout configuration.
  5. Default configuration rule: **The default gesture for the default first handle/sidebar (Home Grid) is strictly `swipe_left`**.

---

## 2. Process Boundaries & Resource Allocation

### Process 1: `:core` (Ultra-Lightweight Resident Daemon)
- **Role**: Permanent touch detector, network speed monitor, and passive sensor host.
- **Target RAM Footprint**: ~15MB – 22MB.
- **Components**:
  - `HandleService`: Running as an Android Foreground Service with a sticky notification.
  - `TriggerHandleView`: Minimal raw Android `View` strips attached to `WindowManager`. Zero Compose / Glide / heavy rendering libraries in memory.
  - `NetSpeedManager`: 1-second interval `TrafficStats` poller with direct 1:1 status bar icon generation.
  - `PhoneCallStateReceiver` / `TelephonyCallback`: Dormant event listener consuming zero audio buffers or background threads until active call state (`OFFHOOK`).
- **Touch & Gesture Response**:
  - Executes instant haptic vibration and visual edge indicator directly on touch.
  - For direct actions (`system:screenshot`, `action:lock_screen`), executes directly within `:core` via Accessibility/System Broadcasts without waking the sidebar process.
  - For sidebar triggers, dispatches an explicit `Intent` to `SidebarService` in `:sidebar`.

### Process 2: `:sidebar` (On-Demand Overlay Engine & Mini-App Host)
- **Role**: Sidebar drawer rendering, multi-page gesture container manager, and floating window host.
- **Target RAM Footprint**: ~40MB – 60MB (Active only when in use; safely reclaimable by Android LMK without killing `:core`).
- **Components**:
  - `SidebarService` & `SidebarManager`: Receives launch intents with `extra_container_id` (`${handleId}_${gesture}`).
  - `SidebarView`: Dynamic `ViewPager2` host for gesture-specific page decks.
  - **Grid Views**:
    - **Home / Hybrid Grid**: Loads node schema for the specific gesture container. Default initial setup (`sidebar_swipe_left`) loads default tiles (E-Book Reader, Log Keeper, 3-column layout).
    - **Apps Grid**: Fast catalog loading using local downsampled 48x48 WebP icon cache.
    - **Widget Grid**: Custom-drawn, state-backed mini-app tiles.
  - **Floating Mini-Apps**: Calculator, E-Book Reader, Audio Recorder, Dictionary overlays. On minimize/dismiss, detaches view trees to immediately release graphic buffers.

### Process 3: `:ui` / Main Process (Configuration & Full-Screen Settings)
- **Role**: Interactive Compose settings screens, layout customizer, log viewer, and backup/restore.
- **Target RAM Footprint**: ~70MB – 120MB (Alive ONLY while user configures settings; 100% garbage collected upon exit).
- **Components**:
  - `SettingsActivity`, `HandlesListSettingsScreen`, `SidebarSettingsScreen`, `PageManagementSettingsScreen`, `LogKeeperActivity`, `NotificationHistoryActivity`.
  - Background worker for one-time icon downsampling and indexing.

---

## 3. IPC & Multi-Process Synchronization Architecture

### Cross-Process Preference Sync (`OverlaySyncManager`)
- **Challenge**: Standard Android `SharedPreferencesImpl` caches data in process memory, causing cross-process staleness when `:ui` writes changes.
- **Mechanism**:
  1. Writes in any process execute `prefs.edit().commit()` to flush to disk synchronously.
  2. Emits an explicit broadcast `Intent("com.example.ACTION_OVERLAY_SYNC")` with `KEY` and `VALUE`.
  3. Receivers in `:core` and `:sidebar` reload the modified preference keys into memory immediately.

### Launch Intent IPC Contract
- When `HandleService` (:core) detects a gesture, it starts `SidebarService` (:sidebar) via explicit Intent:
  - `extra_handle_id`: Originating handle identifier (e.g. `sidebar`, `handle_1`, `handle_2`).
  - `extra_gesture`: Triggered gesture (e.g. `swipe_left`, `tap`, `swipe_right`, `long_press`, `double_tap`).
  - `extra_container_id`: Unique container key `${handleId}_${gesture}` (e.g. `sidebar_swipe_left`).

---

## 4. Gesture Container Isolation & Default Configuration Rules

### Container Model
- Every gesture on every handle is an isolated container instance with its own independent:
  - Default face (Page 0).
  - Page deck order and active pages (`handle_${containerId}_pages`).
  - Layout customizer properties (columns, node arrangements, background styling).

### Default Primary Configuration
- **Primary Handle**: `sidebar` (or `handle_1`).
- **Default Action / Gesture**: **`swipe_left`** is assigned to open the primary sidebar container (`sidebar_swipe_left`).
- **Default Face**: `default_hybrid_sidebar_swipe_left` (Home / Hybrid Grid).
- **Default Tiles**: E-Book Reader tile + Log Keeper tile, 3-column layout, wrap content enabled.
- **Secondary Gestures / Handles**: Each additional gesture or new handle receives a clean, isolated container key without shared state or tile bleeding.

---

## 5. High-Speed Downsampled Icon Cache Pipeline

### Problem
- Synchronous calls to `PackageManager.getApplicationIcon()` on the UI thread cause 80ms–250ms Binder IPC delays, resulting in severe swipe stutter and frame drops.

### Solution
1. **Background Indexing Worker**:
   - Fetches installed packages asynchronously.
   - Downsamples icons to **48x48 / 64x64 pixels** in WebP format.
   - Persists to internal storage: `/data/data/com.example/cache/icons/{packageName}.webp`.
   - Listens to `ACTION_PACKAGE_ADDED` and `ACTION_PACKAGE_REMOVED` for automated updates.
2. **Instant Grid Loading**:
   - Hybrid and Apps grids read directly from the 48x48 WebP cache.
   - Memory cost: ~9KB per icon (~180KB for 20 icons).
   - Zero main-thread Binder calls; gesture-to-render latency under 12ms (60–120fps smooth).

---

## 6. Phased Implementation Roadmap

- [ ] **Phase 12.6.1: Manifest & Process Separation**
  - Configure `android:process=":core"` for `HandleService` and `android:process=":sidebar"` for `SidebarService` in `AndroidManifest.xml`.
  - Validate inter-process permission grants and service start flags.
- [ ] **Phase 12.6.2: Gesture Defaults & Container Key Alignment**
  - Set default gesture for default first handle to `swipe_left` targeting `sidebar_swipe_left`.
  - Align `PageManager.kt`, `SidebarManager.kt`, and `HandlesListSettingsScreen.kt` for `sidebar_swipe_left` default container initialization.
- [ ] **Phase 12.6.3: Downsampled Icon Cache Engine**
  - Implement `IconCacheManager.kt` with 48x48 WebP generation and passive package change listeners.
  - Connect `HybridGridPageView.kt` and `AppsPageView.kt` to load from downsampled cache.
- [ ] **Phase 12.6.4: Floating Mini-App Lifecycle & Passive Call Sensor**
  - Verify on-demand spawn, view unbinding, and state persistence for floating tools.
  - Confirm zero-buffer dormant telephony listener in `:core`.
