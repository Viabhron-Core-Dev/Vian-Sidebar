import re

with open('PHASE_PLAN.md', 'r') as f:
    pp = f.read()
pp = pp.replace(
    "- [ ] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows.",
    "- [x] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows."
)
pp = pp.replace(
    "- [ ] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble).",
    "- [x] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble)."
)
with open('PHASE_PLAN.md', 'w') as f:
    f.write(pp)

with open('BLUEPRINT.md', 'r') as f:
    bp = f.read()
bp = bp.replace(
    "- Address missing architecture (Phase 9): Universal `PageWindowManager`, `FloatingTriggerService`.",
    "- Phase 8 Complete. Proceeding to Phase 9."
)
with open('BLUEPRINT.md', 'w') as f:
    f.write(bp)

with open('BLUEPRINT2.md', 'r') as f:
    bp2 = f.read()
bp2 = bp2.replace(
    "* [ ] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows.",
    "* [x] Migrate universal `PageWindowManager` and `PageWindowService` to spawn generic `PageViews` as floating windows."
)
bp2 = bp2.replace(
    "* [ ] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble).",
    "* [x] Migrate `FloatingTriggerService` (Persistent floating shortcut bubble)."
)
ledger_entry = "*   **2026-08-11:** Executed remainder of Phase 8: Migrated universal PageWindow wrapper and FloatingTriggerService. Phase 8 complete."
bp2 = bp2.replace('---', '---\n\n' + ledger_entry)
with open('BLUEPRINT2.md', 'w') as f:
    f.write(bp2)
