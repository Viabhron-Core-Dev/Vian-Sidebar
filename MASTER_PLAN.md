# Master Plan: Abbles Rebuild

## Overview
This document outlines the ideal, clean structure and terminology for a full app rebuild from scratch of the "Abbles" application. The core philosophy centers around "bubbles" (floating interactive elements) that provide context-rich, on-demand capabilities overlaying the Android OS. 

## Structure & Terminology
- **Abbles**: The unified suite of floating tools and overlay modules.
- **Bubbles**: The primary folded state of any floating window. A compact, 36dp circular icon that persists on-screen.
- **Canvas / Window**: The unfolded, expanded state of a bubble providing the full user interface (e.g., Work Notes, Dictionary, Mini Browser).
- **Dock**: An optional magnetic edge or centralized holding area for inactive bubbles.

## Rebuild Strategy (Future Phase)
*Note: This phase is deferred to a later time.*
1. **Reference Archiving**: Move the entirety of the current repository into a `reference/` folder.
2. **Clean Initialization**: Initialize a new, modern Jetpack Compose + Kotlin Coroutine architecture at the root.
3. **Module Import**: Selectively import and adapt logic from `reference/` for each core capability:
   - **Floating Manager**: A unified `BubbleManager` to handle lifecycle, touch gestures, and window parameters for all bubble types, replacing disparate WindowManager classes.
   - **Service Layer**: Consolidate AccessibilityServices and OverlayServices.
   - **Persistence**: Centralize settings and Room database usage.

## Component Standardization
- **Bubbles**: Standard 36dp `CircleShape` with `Alpha(0.9f)`, using Material/System icons for identification.
- **Window Controls**: Consistent, unified bottom-right overlay controls (Close, Minimize/Fold, Resize) across all modules to ensure a cohesive user experience.
