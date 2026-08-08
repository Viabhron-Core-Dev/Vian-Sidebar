# PLAN SIDEBAR: APPYWORK (Element Blueprint)

## 1. Overview
**Identity:** "Appywork" is a specialized vibe-coding element integrated into the "Sidebar" super-app. It is not a standalone application, but rather a powerful modular tool that lives alongside other Sidebar tools (like the popup dictionary).
**Theme:** Inherits or utilizes the Sky Blue and White light theme.
**Core Purpose:** To provide a seamless, floating code editor and Git management overlay that interacts directly with mobile web browsers during AI chat sessions.

## 2. Integration with Sidebar
*   **System Context Menu:** Appywork registers as an Android text selection action (e.g., "Push via Appywork"). Just like sending text to the Dictionary, highlighting AI-generated code in a browser and selecting this option instantly routes the text to the Appywork module.
*   **Floating Window Manager:** Appywork utilizes Sidebar's existing floating window infrastructure.
    *   Can be spawned from the Sidebar or triggered via the context menu.
*   **Main App Integration (Settings Page):** Appywork has its own dedicated item/page within the main Sidebar settings menu (similar to how the Dictionary tool is configured). This specific "Appywork Settings" page handles full-screen Project management, GitHub PATs, AI Site links, and global behavior toggles.

## 3. Floating Window UI (The Vibe Coding Hub)
*   **Window Controls (Bottom Right Row):** A small compact buttons row in the bottom right corner containing standard controls: `[Close]` (kills the Appywork instance, returning to the main Sidebar/device UI), `[Fold]` (minimizes to a floating bubble to sleep/save resources) and `[Resize]`
*   **Window Topbar:** Contains a dedicated `[Settings ⚙️]` button in the top-right corner. Tapping this button instantly opens the full-screen "Appywork Settings" page within the main Sidebar app.
*   **Default View:** List of Projects.
*   **Project Header:** Quick action buttons to `[Copy AI Link]` and `[Copy GitHub Link]` to clipboard.
*   **Repo Tree:** 
    *   Standard code editor file tree.
    *   **Folder Creation:** Ability to create new folders directly in the repo tree. Additionally, automatic folder creation is supported—if a new file path specifies a location (e.g., `src/readme`), the necessary directories (e.g., `src/`) are created automatically if they don't exist.
    *   `[3-dots menu]` per file: Copy full content or specific lines (e.g., 23-45), Replace lines, Download (saves to device Downloads, appending `.txt`).
    *   **Multi-Select / Group Actions:** Select multiple files/folders to Move or Delete in bulk.
    *   **Search:** In-repo search directly within the floating window.
*   **PWA Preview:** On-demand WebView inside the floating window, served by an embedded local HTTP server (localhost:8080).

## 4. The Import & Parse Engine
*   **Share/Push Action:** Text routed to Appywork triggers the parser.
*   **Cascading Regex Engine:** Scans text for code blocks using multiple fallback patterns (Markdown blocks, `file:path/to/file` headers, HTML comments). No on-device AI used.
*   **Quarantine (Manual Placement):** If a code block is detected but the file path cannot be extracted, it is quarantined. The UI presents these orphaned blocks one by one, allowing the user to select the target file from the Repo Tree or type a new path.
*   **Diff Preview:** Once parsed (and quarantined items resolved), a summary is shown (e.g., "Updating 3 files: 🟢 index.html (New), 🔵 app.js (Modified)").

## 5. Inbuilt Repo & Lightweight Push
*   **Local State:** Raw files stored in Sidebar's `getFilesDir()/{projectId}/`. Room DB manages metadata (path, local hash, sync status: `NEW`, `MODIFIED`, `DELETED`).
*   **Authentication Options:** 
    *   **Personal Access Token (PAT):** Standard static token authentication.
    *   **GitHub MCP (Model Context Protocol):** Leveraging an MCP server for intelligent, context-aware Git interactions.
    *   **Custom GitHub App:** OAuth-based flow utilizing a custom GitHub App for secure, scoped repository access without managing raw PATs.
*   **Stateless Push (Git Data API):**
    1. Fetch latest commit SHA and base Tree SHA.
    2. Upload `NEW`/`MODIFIED` files as Git Blobs.
    3. Construct a new Tree payload (including `null` SHAs for grouped `DELETED` files).
    4. Create Commit and fast-forward the branch reference.
*   **Advantage:** Zero `.git` bloat, highly resistant to corruption, perfectly suited for atomic group actions and low-bandwidth mobile usage.

## 6. Vibe Coding Workflow in Sidebar
1. Chat with AI in mobile browser.
2. AI outputs code. Highlight the text -> Context Menu -> "Push via Appywork".
3. Sidebar instantly opens the Appywork Floating Window.
4. Appywork parses the text via Regex. Any missing file paths trigger the Quarantine manual placement UI.
5. Diff Preview confirms the changes.
6. Tap `[Apply & Push]`. Files are saved locally and a lightweight Git Data API push is executed using the project's PAT (or GitHub App/MCP integration).
7. Tap `[Fold]` (in the bottom right corner) to minimize Appywork back into the Sidebar bubble, ready for the next prompt.

*XML floating window.* 
*Floating window and button like rest.*

## 7. Execution Phases

### Phase 1: Foundation & Data Layer
*   **1.1 Database Schema:** Create Room entities for `Project` (name, remote URL, auth type, auth token) and `FileNode` (path, local hash, sync state, projectId).
*   **1.2 Local File System:** Implement storage controllers to read/write raw files to `getFilesDir()/{projectId}/`.
*   **1.3 Main App Settings UI:** Build the full-screen "Appywork Settings" page in the Sidebar app for CRUD operations on Projects and Auth management (PAT, GitHub App OAuth config, MCP config).

### Phase 2: Intent Interception & Parsing Engine
*   **2.1 Context Menu Registration:** Add `android.intent.action.PROCESS_TEXT` intent filter to an `AppyworkReceiverActivity` to handle "Push via Appywork" from text selection.
*   **2.2 Regex Parser Engine:** Develop the cascading regex engine to extract file paths and code blocks from raw text.
*   **2.3 Quarantine Logic:** Implement the system to flag code blocks missing valid file paths and hold them in memory.

### Phase 3: Floating Window Core Infrastructure
*   **3.1 Window Manager:** Extend the existing Sidebar floating window system for Appywork (`AppyworkWindowManager`).
*   **3.2 Base XML Layout:** Create the floating XML layout (`layout_appywork_floating.xml`) with topbar, content area, and bottom right control row (Close, Fold, Resize).
*   **3.3 State Management:** Implement expanding, folding (minimizing to bubble), dragging, and resizing logic.

### Phase 4: Floating Window UI - Project & File Tree
*   **4.1 Project Selector:** UI to select active project from the floating window.
*   **4.2 File Tree UI:** Build a collapsible tree view or list for local files.
*   **4.3 File Operations:** Implement context menus (3-dots) for files/folders (Create, Delete, Move, Copy lines, Download).
*   **4.4 Multi-select & Search:** Add search bar and multi-selection mode for bulk actions.

### Phase 5: Diff Preview & Quarantine Resolution UI
*   **5.1 Diff Screen:** UI to display parsed changes (`NEW`, `MODIFIED`, `DELETED`) before applying.
*   **5.2 Quarantine Resolver:** UI to present orphaned code blocks and allow manual assignment to existing or new file paths.
*   **5.3 Apply Engine:** Logic to write approved changes to the local file system and update Room DB sync states.

### Phase 6: GitHub Integration & Push Mechanics
*   **6.1 Auth Providers:** Implement logic to retrieve the correct token based on Project settings (PAT vs GitHub App token exchange).
*   **6.2 Git Data API Client:** Build the stateless push logic (Fetch SHA -> Upload Blobs -> Create Tree -> Create Commit -> Update Ref).
*   **6.3 Push UI/UX:** Add progress indicators, error handling, and success notifications within the floating window.

### Phase 7: PWA Preview & Polish
*   **7.1 Embedded NanoHTTPD Server:** Configure a local server mapping to the project's root directory.
*   **7.2 WebView Integration:** Add a "Preview" tab/button in the floating window to render `localhost:8080`.
*   **7.3 Final Testing:** End-to-end vibe coding workflow test from text highlight to GitHub commit.
