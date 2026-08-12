import re

# Update BLUEPRINT.md
with open('BLUEPRINT.md', 'r') as f:
    bp = f.read()
bp = bp.replace(
    "- Migrate remaining Sidebar Pages: Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux.",
    "- Migrate Dual-Mode Pages (Dockable & Floating): Scheduler, Notifications, Resources Tracker.\n- Migrate Heavy PageWindows (Floating Only): File Explorer, Local Terminal, Termux."
)
with open('BLUEPRINT.md', 'w') as f:
    f.write(bp)

# Update PHASE_PLAN.md
with open('PHASE_PLAN.md', 'r') as f:
    pp = f.read()
pp = pp.replace(
    "- [ ] Migrate remaining Sidebar Pages: Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux.",
    "- [ ] Migrate Dual-Mode Pages (Dockable & Floating): Scheduler, Notifications, Resources Tracker.\n- [ ] Migrate Heavy PageWindows (Floating Only, via PageWindowManager): File Explorer, Local Terminal, Termux."
)
with open('PHASE_PLAN.md', 'w') as f:
    f.write(pp)

# Update BLUEPRINT2.md
with open('BLUEPRINT2.md', 'r') as f:
    bp2 = f.read()
bp2 = bp2.replace(
    "* [ ] Migrate remaining Sidebar Pages: Scheduler, Notifications, Resources Tracker, File Explorer, Local Terminal, Termux.",
    "* [ ] Migrate Dual-Mode Pages (Dockable & Floating): Scheduler, Notifications, Resources Tracker.\n* [ ] Migrate Heavy PageWindows (Floating Only, via PageWindowManager): File Explorer, Local Terminal, Termux."
)
ledger_entry = "*   **2026-08-11:** Updated Phase 9 blueprints to enforce strict architectural boundaries: separated Dual-Mode Pages (Scheduler, Notifications, Resources Tracker) from Floating-Only PageWindows (File Explorer, Local Terminal, Termux) which cannot be docked in the Sidebar."
bp2 = bp2.replace('---', '---\n\n' + ledger_entry)
with open('BLUEPRINT2.md', 'w') as f:
    f.write(bp2)

