import re

with open('PHASE_PLAN.md', 'r') as f:
    pp = f.read()
pp = pp.replace(
    "- [ ] Migrate `MediaPlayerPageView`",
    "- [x] Migrate `MediaPlayerPageView`"
)
pp = pp.replace(
    "- [ ] Migrate `WidgetPageView`",
    "- [x] Migrate `WidgetPageView`"
)
pp = pp.replace(
    "- [ ] Wire both into `SidebarView.kt` router and settings menu.",
    "- [x] Wire both into `SidebarView.kt` router and settings menu."
)
with open('PHASE_PLAN.md', 'w') as f:
    f.write(pp)

with open('BLUEPRINT.md', 'r') as f:
    bp = f.read()
bp = bp.replace(
    "- Phase 8 Complete. Proceeding to Phase 8.5 (Orphaned Sidebar Pages).",
    "- Phase 8.5 Complete. Proceeding to Phase 9."
)
with open('BLUEPRINT.md', 'w') as f:
    f.write(bp)

with open('BLUEPRINT2.md', 'r') as f:
    bp2 = f.read()
bp2 = bp2.replace(
    "* [ ] Migrate `MediaPlayerPageView`",
    "* [x] Migrate `MediaPlayerPageView`"
)
bp2 = bp2.replace(
    "* [ ] Migrate `WidgetPageView`",
    "* [x] Migrate `WidgetPageView`"
)
bp2 = bp2.replace(
    "* [ ] Update `SidebarView.kt` router",
    "* [x] Update `SidebarView.kt` router"
)
ledger_entry = "*   **2026-08-11:** Implemented Phase 8.5: Migrated `MediaPlayerPageView` and `WidgetPageView`, wired them to `SidebarView.kt`, and added to Settings dropdowns."
bp2 = bp2.replace('---', '---\n\n' + ledger_entry)
with open('BLUEPRINT2.md', 'w') as f:
    f.write(bp2)
