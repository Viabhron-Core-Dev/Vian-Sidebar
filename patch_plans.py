import re

# Update BLUEPRINT.md
with open('BLUEPRINT.md', 'r') as f:
    bp1 = f.read()
bp1 = bp1.replace("""## Next Steps
- Validate Phase 8 builds successfully.
- Address remaining utilities (e.g. Note-taking, Timer) and integrate them.
- Continue migrating other core OS features.""", """## Next Steps
- Validate Phase 8 architecture fixes (completed).
- Address missing architecture (Phase 9): Universal `PageWindowManager`, `FloatingTriggerService`.
- Migrate standalone Floating Windows: `CursorManager`, `WorkNotesWindowManager`.
- Migrate remaining Sidebar Pages: Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux.
- Migrate OS Popups: Dictionary/Translation text selection.
- Migrate Share-to-App Lightweight Browser: `BrowserReceiverActivity`, `FloatingBrowserService`.
- Migrate PWA Loader.""")
with open('BLUEPRINT.md', 'w') as f:
    f.write(bp1)

# Update PHASE_PLAN.md
with open('PHASE_PLAN.md', 'r') as f:
    pp = f.read()

phase8 = """## PHASE 8: Floating Apps & Utilities (Part 1)
- [x] Correct architecture: Revert Calculator and Compass to Sidebar Pages (`PageView`).
- [x] Rename `DictionaryPageView` to `DictionaryView` to reflect its Floating Window status.
- [x] Decouple `MiniAppManager` from page views.
- [ ] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows.
- [ ] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble)."""

phase9 = """## PHASE 9: Floating Apps & Utilities (Part 2)
- [ ] Migrate standalone Floating Windows: `CursorManager`, `WorkNotesWindowManager`.
- [ ] Migrate remaining Sidebar Pages: Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux.
- [ ] Migrate OS Integration Popups: Dictionary (`DictionaryPopupActivity`) and Translation (`TranslationPopupActivity`) via Android Text Selection (`PROCESS_TEXT`).
- [ ] Migrate Lightweight Mobile Browser: `BrowserReceiverActivity`, `FloatingBrowserService`, and `FloatingBrowserWindowManager` (Triggered via `ACTION_SEND`/`ACTION_VIEW` share intents, *not* Add Element).
- [ ] Migrate PWA Loader architecture (`PwaWindowManager`, `PwaServer`, `PwaDatabase`) to support multiple concurrent floating WebView instances.
- [ ] Migrate heavy mini-apps (eReader, Call Recorder, etc.).
- [ ] Migrate Appywork as an isolated, modular Vibe Coding Hub. Ensure its `PROCESS_TEXT` intent, Git API stateless push, and embedded NanoHTTPD features are cleanly decoupled."""

pp = re.sub(r'## PHASE 8: Floating Apps.*?## PHASE 10:', phase8 + '\n\n' + phase9 + '\n\n## PHASE 10:', pp, flags=re.DOTALL)

with open('PHASE_PLAN.md', 'w') as f:
    f.write(pp)

# Update BLUEPRINT2.md
with open('BLUEPRINT2.md', 'r') as f:
    bp2 = f.read()

bp2 = re.sub(r'\*\*PHASE 8: Floating Apps.*?\*\*PHASE 10:', phase8.replace('## PHASE', '**PHASE').replace('- [', '* [') + '\n\n' + phase9.replace('## PHASE', '**PHASE').replace('- [', '* [') + '\n\n**PHASE 10:', bp2, flags=re.DOTALL)

# Add migration ledger entry
ledger_entry = """*   **2026-08-11:** Identified missing architecture in Floating Apps (Phase 8/9): Universal PageWindowManager, FloatingTriggerService, standalone floating windows (Cursor, Work Notes, PWA Loader), remaining Sidebar Pages (Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux), OS text selection popups, and Share-to-App lightweight browser. Corrected Calculator/Compass to Sidebar Pages and renamed DictionaryView."""
bp2 = bp2.replace('---', '---\n\n' + ledger_entry)

with open('BLUEPRINT2.md', 'w') as f:
    f.write(bp2)

