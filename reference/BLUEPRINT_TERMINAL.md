# Blueprint: Terminal & Termux Hybrid Mini-Apps

## Overview
This blueprint outlines the implementation of two distinct, isolated floating window mini-apps within the Appywork ecosystem. Both provide command-line interfaces but serve different use cases based on the user's need for speed versus capabilities.

## 1. Local Terminal Mini-App
**Purpose:** A fast, lightweight, zero-dependency shell for quick system queries.

**Architecture & Implementation Details:**
- **Service & Window Manager:** Integrated into `PageWindowManager` as a custom page view.
- **Process Management:** 
  - Spawns a persistent Android shell via `ProcessBuilder("/system/bin/sh")`.
  - Utilizes Kotlin Coroutines (`Dispatchers.IO`) to asynchronously read streams.
- **UI/UX:**
  - Implemented using native XML layout (`page_local_terminal.xml`) containing a `ScrollView`, `TextView` for output, and `EditText` for input, ensuring stability within the overlay window architecture.

## 2. Termux (PRoot) Environment Mini-App
**Purpose:** A full, robust Linux environment running inside the app.

**Architecture & Implementation Details:**
- **Service & Window Manager:** Integrated into `PageWindowManager` as a custom page view.
- **Terminal Emulator (xterm.js):**
  - Uses `WebView` loading a local `xterm.html` asset containing `xterm.js` and `xterm-addon-fit`.
  - Bridges Android and Javascript via `@JavascriptInterface` to send/receive streams.
- **Process Management (Phase 1):**
  - Currently connects to standard `/system/bin/sh` as a proof-of-concept for the terminal emulator.
- **Installation Flow (Pending Phase):**
  - Includes a placeholder "Install Termux Environment" UI state to be wired up later to download an Alpine Linux rootfs and bootstrap PRoot.

## Development Phases

### Phase 1: Local Terminal Implementation (Complete)
- Built native XML layout and `LocalTerminalPageView.kt`.
- Wired up `ProcessBuilder` for `/system/bin/sh`.
- Added to `PageWindowPickerActivity` and `PageWindowManager`.

### Phase 2: Termux Terminal Emulator UI Component (Complete)
- Created `xterm.html` loading xterm.js via CDN.
- Created `page_termux.xml` and `TermuxPageView.kt` with a WebView.
- Setup `JavascriptInterface` to bridge shell I/O to the web-based terminal emulator.
- Fallback connection to `/system/bin/sh` implemented to verify xterm.js rendering.

### Phase 3: PRoot & Rootfs Downloader (Pending)
- Build the downloader utility to fetch an Alpine Linux rootfs tarball.
- Implement extraction logic into `files/termux_rootfs`.
- Provide the PRoot binary (compiled for the device architecture).

### Phase 4: Termux Mini-App Integration (Pending)
- Wire the terminal UI to execute the `proot` command instead of standard `sh`.
