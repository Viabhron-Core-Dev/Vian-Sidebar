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
- Validate Phase 8 builds successfully.
- Address remaining utilities (e.g. Note-taking, Timer) and integrate them.
- Continue migrating other core OS features.
