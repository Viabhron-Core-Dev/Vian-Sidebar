import re

def process_sidebar():
    with open('app/src/main/java/com/example/service/SidebarService.kt', 'r') as f:
        content = f.read()
    
    # Change class name
    content = content.replace('class FloatingReaderService', 'class SidebarService')
    
    # Remove ereader specific methods
    ereader_methods = [
        "setupFloatingView", "initViews", "loadLibraryBooks", "inflateLibraryIfNeeded",
        "openLibraryView", "inflateChaptersIfNeeded", "openChaptersView", "openNotesView",
        "inflateBookmarksIfNeeded", "inflateSearchIfNeeded", "openBookmarksView",
        "setupListeners", "createLongPressDragListener", "loadBook", "loadChapterText",
        "renderChapter", "navigateChapter", "performFullBookSearch", "loadAndJumpToOffset",
        "startAutoSaveTimer", "saveCurrentPosition", "toggleTts", "executeTtsToggle",
        "getCoverCacheDir", "loadEpubCover", "showExplorerContextMenu", "inflateNotesIfNeeded",
        "updateNotesUi", "loadNotes", "showNoteDialog", "saveLibraryState"
    ]
    
    for method in ereader_methods:
        # regex to remove from `private fun METHOD_NAME` to the matching closing brace
        pattern = re.compile(r'(\s*(?:private |public |)fun ' + method + r'\s*\(.*?\)\s*\{)')
        
        while True:
            match = pattern.search(content)
            if not match:
                break
            
            start_index = match.start()
            
            # Find the matching closing brace
            brace_count = 0
            in_method = False
            end_index = -1
            
            for i in range(match.end() - 1, len(content)):
                if content[i] == '{':
                    brace_count += 1
                    in_method = True
                elif content[i] == '}':
                    brace_count -= 1
                    if in_method and brace_count == 0:
                        end_index = i + 1
                        break
            
            if end_index != -1:
                content = content[:start_index] + content[end_index:]
            else:
                break

    # Also we need to strip `FloatingReaderService` mentions inside it, like Intent(this, FloatingReaderService::class.java)
    # wait, the intent to start reader should stay, because SidebarService starts FloatingReaderService when "ebook_reader" is triggered.
    
    # Now remove the call to setupFloatingView() in onCreate
    content = re.sub(r'\s*setupFloatingView\(\)', '', content)
    content = re.sub(r'\s*saveCurrentPosition\(\)', '', content)
    content = re.sub(r'\s*mediaSession\?\.isActive = false', '', content)
    content = re.sub(r'\s*mediaSession\?\.release\(\)', '', content)
    content = re.sub(r'\s*tts\?\.stop\(\)', '', content)
    content = re.sub(r'\s*tts\?\.shutdown\(\)', '', content)
    
    with open('app/src/main/java/com/example/service/SidebarService.kt', 'w') as f:
        f.write(content)

process_sidebar()

def strip_more():
    with open('app/src/main/java/com/example/service/SidebarService.kt', 'r') as f:
        content = f.read()
    
    more_methods = [
        "toggleReader", "setFolded", "syncWindowStates", "hideOverlays",
        "updateBottomControlsVisibility", "updateWindowFocusAbility", 
        "updateTopDragBarVisibility", "toggleFullScreen"
    ]
    
    for method in more_methods:
        pattern = re.compile(r'(\s*(?:private |public |)fun ' + method + r'\s*\(.*?\)\s*\{)')
        while True:
            match = pattern.search(content)
            if not match:
                break
            
            start_index = match.start()
            brace_count = 0
            in_method = False
            end_index = -1
            
            for i in range(match.end() - 1, len(content)):
                if content[i] == '{':
                    brace_count += 1
                    in_method = True
                elif content[i] == '}':
                    brace_count -= 1
                    if in_method and brace_count == 0:
                        end_index = i + 1
                        break
            
            if end_index != -1:
                content = content[:start_index] + content[end_index:]
            else:
                break
    
    with open('app/src/main/java/com/example/service/SidebarService.kt', 'w') as f:
        f.write(content)

strip_more()

def process_ereader():
    with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'r') as f:
        content = f.read()
    
    # Remove sidebar specific methods
    sidebar_methods = [
        "rebuildSidebarPages", "closeSidebar", "showSidebar", "openSidebarPage",
        "openGestureSidebar", "openGestureSidebarPage", "showStandalonePage",
        "showWidgetsGridEditOverlay", "showHybridGridEditOverlay", "showSidebarEditOverlay",
        "executeElementAction", "setupNetSpeed", "createSpeedIcon", "reloadHandles"
    ]
    
    for method in sidebar_methods:
        pattern = re.compile(r'(\s*(?:private |public |)fun ' + method + r'\s*\(.*?\)\s*\{)')
        while True:
            match = pattern.search(content)
            if not match:
                break
            
            start_index = match.start()
            brace_count = 0
            in_method = False
            end_index = -1
            
            for i in range(match.end() - 1, len(content)):
                if content[i] == '{':
                    brace_count += 1
                    in_method = True
                elif content[i] == '}':
                    brace_count -= 1
                    if in_method and brace_count == 0:
                        end_index = i + 1
                        break
            
            if end_index != -1:
                content = content[:start_index] + content[end_index:]
            else:
                break
                
    content = re.sub(r'\s*reloadHandles\(\)', '', content)
    content = re.sub(r'\s*setupNetSpeed\(\)', '', content)
    content = re.sub(r'\s*rebuildSidebarPages\("sidebar"\)', '', content)

    with open('app/src/main/java/com/example/service/FloatingReaderService.kt', 'w') as f:
        f.write(content)

process_ereader()
