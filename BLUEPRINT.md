# Vian OS App Migration Blueprint (Updated)

## Phase 8: Floating Apps & Utilities (Part 1)
- [x] Migrate `CalculatorPageView` to `feature/miniapps` and wrap in `CalculatorFloatingWindow`
- [x] Migrate `CompassPageView` and `CompassDrawView` to `feature/miniapps` and wrap in `CompassFloatingWindow`
- [x] Migrate `DictionaryPageView` to `feature/miniapps` and wrap in `DictionaryFloatingWindow`
- [x] Create `MiniAppManager` to handle app launching logic (toggle app).
- [x] Update Grid adapters (`AppsPageView`, `HybridGridPageView`) to use `MiniAppManager` instead of `PageWindowService`.
- [x] Update `FloatingWindowManager` to expose `activeWindows` list.
- [x] Refactor Database instance creation for `DictionaryPageView`.
- [x] Address compilation errors (LogKeeper reference in CompassPageView).

## Next Steps
- Validate Phase 8 architecture fixes (completed).
- Phase 12 (Part 1) Complete. Proceeding to Phase 12 Popups.
## Phase 8.5: Orphaned Sidebar Pages Catch-up
- Migrate `MediaPlayerPageView` and `WidgetPageView` to the new Sidebar container.

## Next Steps (Phase 9+)
- **Phase 9: The UI Spines (Settings & Add Element)**: Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is. Wire up migrated pages/services; leave unmigrated features as harmless stubs.
- **Phase 10: The Background System Hub (Plugins & Accessibility) (COMPLETED)**: Established `VianSideAccessibilityService` as a unified System Tools Hub with on-demand module instantiation (Cursor, AutoScroll, Screenshot, AppKiller). Hardware controls, decoupled dormant CallRecorder, and screen-aware NetSpeedManager are successfully integrated.
- **Phase 11: Sidebar On-Demand Optimization (COMPLETED)**: Leveraged existing `ViewPager2` lazy-loading.
- **Phase 12: Unified Z-Window Manager & OS Popups (Part 1 COMPLETE)**: Built a centralized Z-Window Manager (FloatingWindowManager) with automated Z-ordering, dormant folding, and magnetic grouping. (Popups and Floating Browser deferred to next step).
- **Phase 13: Heavy Floating Mini-Apps (Pre-PWA Bridge)**: Migrate heavy Floating Apps (`FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`, `CursorManager` UI, `WorkNotesWindowManager`, and Appywork). These are the final heavy native windows before PWA.
- **Phase 14: PWA Engine**: Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.
- **Phase 15: Polish, Launcher Prep & Finalization**: Implement Advanced Floating Grouping, Backup & Restore JSON framework, prepare architecture hooks for the external Android Launcher merge, and eradicate `reference/`.
