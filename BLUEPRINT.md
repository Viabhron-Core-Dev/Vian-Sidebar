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
- **Phase 10: The Background System Hub (Plugins & Accessibility)**: Establish `VianSideAccessibilityService` as a unified System Tools Hub (orchestrator loaded on-demand). Migrate `CallRecorderManager` (dormant), `NetSpeedManager` (screen-aware, no overlay, updates notification icon), and Hardware Controls.
- **Phase 11: Sidebar On-Demand Optimization (COMPLETED)**: Leveraged existing `ViewPager2` lazy-loading.
- **Phase 12: Unified Z-Window Manager & OS Popups**: Build a centralized Z-Window Manager. Migrate Text Selection Popups and Lightweight Floating Browser.
- **Phase 13: Heavy Floating Mini-Apps (Pre-PWA Bridge)**: Migrate heavy Floating Apps (`FileExplorerPageView`, `LocalTerminalPageView`, `TermuxPageView`, `CursorManager` UI, `WorkNotesWindowManager`, and Appywork). These are the final heavy native windows before PWA.
- **Phase 14: PWA Engine**: Migrate `PwaWindowManager`, `PwaServer`, and `PwaDatabase`.
- **Phase 15: Polish, Launcher Prep & Finalization**: Implement Advanced Floating Grouping, Backup & Restore JSON framework, prepare architecture hooks for the external Android Launcher merge, and eradicate `reference/`.
