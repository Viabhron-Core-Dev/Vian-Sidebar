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
- Phase 8.5 Complete. Proceeding to Phase 9.
## Phase 8.5: Orphaned Sidebar Pages Catch-up
- Migrate `MediaPlayerPageView` and `WidgetPageView` to the new Sidebar container.

## Next Steps (Phase 9+)
- **Phase 9: The UI Spines (Settings & Add Element)**: Import `SettingsActivity`, `AddElementActivity`, and `ActionPickerActivity` mostly as-is. Wire up migrated pages/services; leave unmigrated features as harmless stubs.
- **Phase 10: The Background System Hub (Plugins)**: Establish `VianSideAccessibilityService` as a unified System Tools Hub. Migrate `CallRecorderManager` (dormant), `NetSpeedManager` (screen-aware), and Hardware Controls.
- **Phase 11: Sidebar On-Demand Optimization**: Leverage existing `ViewPager2` lazy-loading. Implement lifecycle management to freeze/thaw background tasks (pause coroutines/polling when off-screen). Migrate remaining dual-mode pages.
- **Phase 12: Unified Z-Window Manager & Heavy Apps**: Build a centralized Z-Window Manager. Migrate heavy Floating Apps (`FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`, `CursorManager`, `WorkNotesWindowManager`).
- **Phase 13: OS Popups & Floating Browser**: Migrate Text Selection Popups, Lightweight Floating Browser, eReader and Appywork sub-systems.
- **Phase 14: PWA Engine**: Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.
- **Phase 15: Polish, Launcher Prep & Finalization**: Implement Advanced Floating Grouping, Backup & Restore JSON framework, prepare architecture hooks for the external Android Launcher merge, and eradicate `reference/`.
