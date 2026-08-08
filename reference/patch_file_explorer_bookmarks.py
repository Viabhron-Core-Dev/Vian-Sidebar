import os

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'r') as f:
    content = f.read()

target_state = """    var showInfoDialog by remember { mutableStateOf<File?>(null) }"""
replacement_state = """    var showInfoDialog by remember { mutableStateOf<File?>(null) }
    var bookmarks by remember { mutableStateOf(setOf<String>()) }
    var showBookmarksDialog by remember { mutableStateOf(false) }"""

if target_state in content:
    content = content.replace(target_state, replacement_state)
else:
    print("target_state not found")

target_topbar_end = """                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.primary)
                    }
                }"""
replacement_topbar_end = """                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = {
                    if (bookmarks.contains(currentPath)) bookmarks -= currentPath
                    else bookmarks += currentPath
                }) {
                    Icon(if (bookmarks.contains(currentPath)) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.onSurface)
                }"""

if target_topbar_end in content:
    content = content.replace(target_topbar_end, replacement_topbar_end)
else:
    print("target_topbar_end not found")

target_bottom_bar = """                    IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) }
                    IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.LightGray) }
                    IconButton(onClick = { refresh() }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.LightGray) }"""
replacement_bottom_bar = """                    IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) }
                    IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.LightGray) }
                    IconButton(onClick = { refresh() }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.LightGray) }
                    IconButton(onClick = { showBookmarksDialog = true }) { Icon(Icons.Default.Bookmarks, contentDescription = "Bookmarks", tint = Color.LightGray) }"""

if target_bottom_bar in content:
    content = content.replace(target_bottom_bar, replacement_bottom_bar)
else:
    print("target_bottom_bar not found")

target_dialog = """        if (showCreateDialog) {"""
replacement_dialog = """        if (showBookmarksDialog) {
            AlertDialog(
                onDismissRequest = { showBookmarksDialog = false },
                title = { Text("Bookmarks") },
                text = {
                    LazyColumn {
                        items(bookmarks.toList()) { b ->
                            Text(b, modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentPath = b
                                    showBookmarksDialog = false
                                }
                                .padding(vertical = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showBookmarksDialog = false }) { Text("Close") } }
            )
        }
        
        if (showCreateDialog) {"""

if target_dialog in content:
    content = content.replace(target_dialog, replacement_dialog)
else:
    print("target_dialog not found")

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'w') as f:
    f.write(content)

