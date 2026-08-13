import re

with open('BLUEPRINT2.md', 'r') as f:
    text = f.read()

old_text = """*   **System Overlays (NetSpeed):**
    Standalone system indicators like the `NetSpeedManager` must be integrated into the new WindowManager framework so they can be toggled and managed alongside other floating utilities.
*   **Accessibility Suite & Screen Recording:**
    Utilities that rely on `AccessibilityService` (Cursor, Auto-Scroll, Long Screenshot) and `MediaProjection` (Screen Record) must be decoupled from the core services. They should only be active when explicitly triggered."""

new_text = """*   **System Overlays (NetSpeed):**
    Standalone system indicators like the `NetSpeedManager` must strictly dynamically update the foreground notification icon ONLY. There is no floating overlay required. It must be tied to `ACTION_SCREEN_ON`/`OFF` to save battery.
*   **Accessibility Suite & Screen Recording:**
    The `VianSideAccessibilityService` must act as a modular orchestrator. Utilities that rely on it (Cursor, Auto-Scroll, Long Screenshot) and `MediaProjection` (Screen Record) must be decoupled and loaded *strictly on-demand*. They should only occupy memory when explicitly triggered."""

text = text.replace(old_text, new_text)

old_phase10 = """**PHASE 10: The Background System Hub (Plugins)**
*   Establish `VianSideAccessibilityService` as a unified System Tools Hub.
*   Migrate `CallRecorderManager` as a 100% dormant plugin, waking only via `TelephonyManager`.
*   Migrate `NetSpeedManager` as a screen-aware plugin (`ACTION_SCREEN_ON`/`OFF`).
*   Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`)."""

new_phase10 = """**PHASE 10: The Background System Hub (Plugins & Accessibility)**
*   Establish `VianSideAccessibilityService` as a unified System Tools Hub (orchestrator for Cursor, AutoScroll, LongScreenshot, loaded strictly on-demand).
*   Migrate `CallRecorderManager` as a 100% dormant plugin, waking only via `TelephonyManager`.
*   Migrate `NetSpeedManager` as a screen-aware plugin (`ACTION_SCREEN_ON`/`OFF`) updating the notification only (NO overlay).
*   Migrate Hardware Controls (`QuickTileHandler`, `MediaVolumeHandler`, `DisplayHandler`)."""

text = text.replace(old_phase10, new_phase10)

with open('BLUEPRINT2.md', 'w') as f:
    f.write(text)
