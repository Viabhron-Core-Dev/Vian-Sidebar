# LiteReader Blueprint: Floating Browser Feature

## Overview
A lightweight, secure, floating web browser designed for quick link checking and reading. It operates primarily as a share target from other browsers, opening links in draggable, resizable floating windows. Multiple instances are supported, and each can be minimized into a frozen floating bubble to save resources.

## Phase 1: Intent Handling & Service Architecture
- **Browser Receiver Activity**: A lightweight, transparent activity to intercept `ACTION_SEND` (text/plain links) and `ACTION_VIEW` intents.
- **Floating Browser Service / Manager**: Manages multiple floating window instances. Each shared link spawns a separate instance.

## Phase 2: Floating Window Mechanics & UI
- **Window Layout**:
  - **Draggable Topbar**: Displays the webpage title. Includes a Settings cog, Minimize (to bubble), Close, and Resize handle (corner).
  - **Content Area**: Swipe-to-refresh layout wrapping the WebView.
  - **FABs**: Floating buttons to save the page as `.txt` or `.mht`.
- **States**:
  - **Expanded (Live)**: Fully interactive, active rendering.
  - **Minimized (Bubble)**: Frozen state (WebView paused/timers paused) represented by a draggable bubble. Tapping restores to the Expanded state, preserving scroll position.

## Phase 3: WebView Configuration & Security
- **Base Profile**:
  - User-Agent mocked as a low-end keypad/mobile phone for lightweight page delivery.
- **Site-Specific Settings (Accessed via Topbar Cog)**:
  - Toggles for: JavaScript execution, Image loading, Ad/Tracker blocking, and Text Wrap (viewport adjustments).
- **Page Extraction**:
  - Logic to parse page text and save to a `.txt` file.
  - Logic to save Web Archive (`.mht`) using WebView's built-in archive capabilities.

## Phase 4: Global Settings & Data Privacy
- **Global Settings Screen**:
  - Integrated into the main app settings (`SidebarSettingsScreen.kt`).
  - Allows configuring default behaviors (e.g., JS off by default, images off by default).
- **Aggressive Cleanup Routine**:
  - Upon closing a floating window instance, the app will explicitly clear the WebView's cache, history, and memory.
  - Ensure zero residual data is left on the device from the session, with the strict exception of user-saved `.txt` or `.mht` files.

---
*Status: Planning Complete. Awaiting approval to begin implementation.*
