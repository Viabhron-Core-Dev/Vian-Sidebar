import re

with open('PHASE_PLAN.md', 'r') as f:
    pp = f.read()
pp = pp.replace(
    "## PHASE 9:",
    "## PHASE 8.5: Orphaned Sidebar Pages Catch-up\n- [ ] Migrate `MediaPlayerPageView` (Dockable Media Controls).\n- [ ] Migrate `WidgetPageView` (Dockable Android AppWidget host).\n- [ ] Wire both into `SidebarView.kt` router and settings menu.\n\n## PHASE 9:"
)
with open('PHASE_PLAN.md', 'w') as f:
    f.write(pp)

with open('BLUEPRINT.md', 'r') as f:
    bp = f.read()
bp = bp.replace(
    "- Phase 8 Complete. Proceeding to Phase 9.",
    "- Phase 8 Complete. Proceeding to Phase 8.5 (Orphaned Sidebar Pages)."
)
bp = bp.replace(
    "- Migrate standalone Floating Windows",
    "## Phase 8.5: Orphaned Sidebar Pages Catch-up\n- Migrate `MediaPlayerPageView` and `WidgetPageView` to the new Sidebar container.\n\n## Next Steps (Phase 9+)\n- Migrate standalone Floating Windows"
)
with open('BLUEPRINT.md', 'w') as f:
    f.write(bp)

with open('BLUEPRINT2.md', 'r') as f:
    bp2 = f.read()
bp2 = bp2.replace(
    "## Phase 9:",
    "## Phase 8.5: Orphaned Sidebar Pages Catch-up\n* [ ] Migrate `MediaPlayerPageView` (Dockable Media Controls).\n* [ ] Migrate `WidgetPageView` (Dockable Android AppWidget host).\n* [ ] Update `SidebarView.kt` router and `SidebarSettingsScreen`.\n\n## Phase 9:"
)
ledger_entry = "*   **2026-08-11:** Created Phase 8.5 to catch up on orphaned Sidebar Pages (`MediaPlayerPageView`, `WidgetPageView`) missed during Phase 7 grid migration."
bp2 = bp2.replace('---', '---\n\n' + ledger_entry)
with open('BLUEPRINT2.md', 'w') as f:
    f.write(bp2)
