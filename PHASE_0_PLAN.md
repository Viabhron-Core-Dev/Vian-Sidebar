# Phase 0: Core Audit & Stabilization Plan

Phase 0 is the prerequisite stabilization gate. It audits, verifies, and stabilizes all existing foundations before any floating mini-app is introduced.

---

## 📋 Phase 0 Sub-Phases Breakdown

### 🔹 Phase 0.1: Sidebar Pages & Containers Audit
- **Goal**: Audit every existing Sidebar page view for layout inflation, dimension constraints, lifecycle disposal, and memory leaks.
- **Components to Audit**:
  - `AppsPageView.kt`
  - `WidgetsGridPageView.kt`
  - `HybridGridPageView.kt`
  - `NotificationPageView.kt`
  - `SchedulerPageView.kt`
  - `MediaPlayerPageView.kt`
  - `CalculatorPageView.kt`
  - `CompassPageView.kt`
  - `AppTrackerPageView.kt`
  - `SidebarView.kt` container lifecycle & `ViewPager2` lazy-loading.
- **Checklist**:
  - [ ] Sizing and height updates trigger properly across different screen densities.
  - [ ] Views release coroutines and listeners when detached or trimmed.
  - [ ] No unmigrated/dangling references.

---

### 🔹 Phase 0.2: Gestures, Touch Interception & Handle Navigation Audit
- **Goal**: Verify edge detection, gesture tracking, fling physics, and overlay dismiss handling.
- **Components to Audit**:
  - `TriggerHandleView.kt`
  - `SidebarView.kt` gesture recognizers and touch delegators.
  - Handle position persistence (screen rotations, restart).
- **Checklist**:
  - [ ] Handle swipe correctly triggers sidebar expansion.
  - [ ] Outside tap cleanly collapses the sidebar overlay.
  - [ ] Fast flings and velocity thresholds behave smoothly without UI glitches.
  - [ ] Multi-handle configurations switch containers cleanly.

---

### 🔹 Phase 0.3: Elements & Action Handlers General Audit
- **Goal**: Audit all element types, action dispatchers, and pickers to ensure correct execution routing.
- **Components to Audit**:
  - `SidebarItem` definitions and parsers (`SidebarAppsManager.kt`, `HybridGridPageView.kt`).
  - `ActionPickerActivity.kt`, `AddElementActivity.kt`, `AppPickerActivity.kt`, `ShortcutPickerActivity.kt`, `IntentPickerActivity.kt`.
  - System action handlers (Screenshot, Flashlight, Volume controls, Quick Settings, App Kill, Auto Scroll, Cursor).
- **Checklist**:
  - [ ] Direct system actions dispatch to their specific tools without error.
  - [ ] **Strict Check**: No element or action erroneously defaults to opening an empty/blank generic floating window.
  - [ ] Element shortcuts and intents resolve properly.

---

### 🔹 Phase 0.4: Independent Background Utilities Audit
- **Goal**: Verify that non-sidebar, non-mini-app background system tools function autonomously with their dedicated settings screens.
- **Components to Audit**:
  - `CallRecorderManager.kt` & `CallStateReceiver.kt` & `RecordingsActivity.kt` & `CallRecorderSettingsScreen.kt`.
  - `NetSpeedManager.kt` & `NetSpeedSettingsScreen.kt`.
- **Checklist**:
  - [ ] Call Recorder is strictly decoupled and only managed via `CallRecorderSettingsScreen`.
  - [ ] NetSpeed indicator observes screen-on/screen-off states and is configured via `NetSpeedSettingsScreen`.
  - [ ] Zero leakage into sidebar element lists or generic floating window registries.

---

### 🔹 Phase 0.5: Universal Log Keeper Verification & Wiring
- **Goal**: Confirm that `LogKeeper` is actively hooked into all system events as the central debug journal.
- **Components to Audit**:
  - `LogKeeper.kt` & `LogKeeperActivity.kt`.
- **Checklist**:
  - [ ] Sidebar lifecycle events logged (attach, detach, page switch, memory trim).
  - [ ] Gesture/handle touches and swipes logged.
  - [ ] Settings changes and handle updates logged.
  - [ ] Element activation/tap events logged.
  - [ ] Background utility state changes logged (`CallRecorderManager`, `NetSpeedManager`).
  - [ ] Log viewing and clear functions work cleanly in `LogKeeperActivity`.
