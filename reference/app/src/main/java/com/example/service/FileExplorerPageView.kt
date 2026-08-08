package com.example.service

import android.content.Context
import android.content.Intent
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class FileExplorerPageView(context: Context) : FrameLayout(context) {
    init {
        val composeView = ComposeView(context).apply {
            setContent {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        background = Color(0xFF1E2124),
                        surface = Color(0xFF282B30),
                        onSurface = Color.White,
                        primary = Color(0xFF7289DA)
                    )
                ) {
                    FileExplorerUI(context = context)
                }
            }
        }
        addView(composeView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FileExplorerUI(context: Context) {
    var currentPath by remember { mutableStateOf(android.os.Environment.getExternalStorageDirectory().absolutePath) }
    var files by remember { mutableStateOf(emptyList<File>()) }
    var selectedFiles by remember { mutableStateOf(setOf<File>()) }
    var clipboard by remember { mutableStateOf<Pair<List<File>, Boolean>?>(null) } // true for cut
    
    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var showInfoDialog by remember { mutableStateOf<File?>(null) }
    var bookmarks by remember { mutableStateOf(setOf<String>()) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    fun refresh() {
        val dir = File(currentPath)
        if (dir.exists() && dir.isDirectory) {
            val list = dir.listFiles()?.toList() ?: emptyList()
            files = list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
    }
    
    LaunchedEffect(currentPath) {
        refresh()
        selectedFiles = emptySet()
    }
    
    fun openFile(file: File) {
        if (file.isDirectory) {
            currentPath = file.absolutePath
        } else {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pathSegments = currentPath.split("/").filter { it.isNotEmpty() }
                    items(pathSegments.size) { index ->
                        val segment = pathSegments[index]
                        val pathSoFar = "/" + pathSegments.take(index + 1).joinToString("/")
                        Text(
                            text = segment.uppercase(),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                currentPath = pathSoFar
                            }
                        )
                        if (index < pathSegments.size - 1) {
                            Text(" » ", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
                if (clipboard != null) {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val (clipFiles, isCut) = clipboard!!
                            for (clipFile in clipFiles) {
                                val dest = File(currentPath, clipFile.name)
                                if (isCut) {
                                    clipFile.renameTo(dest)
                                } else {
                                    if (clipFile.isDirectory) {
                                        clipFile.copyRecursively(dest, overwrite = true)
                                    } else {
                                        clipFile.copyTo(dest, overwrite = true)
                                    }
                                }
                            }
                            if (isCut) {
                                clipboard = null
                            }
                            withContext(Dispatchers.Main) { refresh() }
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = {
                    if (bookmarks.contains(currentPath)) bookmarks -= currentPath
                    else bookmarks += currentPath
                }) {
                    Icon(if (bookmarks.contains(currentPath)) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            // File List
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(files) { file ->
                    val isSelected = selectedFiles.contains(file)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .combinedClickable(
                                onClick = {
                                    if (selectedFiles.isNotEmpty()) {
                                        if (isSelected) selectedFiles -= file
                                        else selectedFiles += file
                                    } else {
                                        openFile(file)
                                    }
                                },
                                onLongClick = {
                                    if (isSelected) selectedFiles -= file
                                    else selectedFiles += file
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = if (file.isDirectory) Color(0xFFF3C76A) else Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = file.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                            val date = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()).format(Date(file.lastModified()))
                            val sizeText = if (file.isDirectory) {
                                val children = file.list()?.size ?: 0
                                "$children items"
                            } else {
                                "${file.length() / 1024} KB"
                            }
                            Text(text = "$date  •  $sizeText", color = Color.Gray, fontSize = 12.sp)
                        }
                        if (selectedFiles.isNotEmpty()) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { 
                                    if (it) selectedFiles += file else selectedFiles -= file 
                                }
                            )
                        }
                    }
                }
            }
            
            // Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedFiles.isEmpty()) {
                    IconButton(onClick = { /* Search */ }) { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) }
                    IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.LightGray) }
                    IconButton(onClick = { refresh() }) { Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.LightGray) }
                    IconButton(onClick = { showBookmarksDialog = true }) { Icon(Icons.Default.Bookmarks, contentDescription = "Bookmarks", tint = Color.LightGray) }
                } else {
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            selectedFiles.forEach { it.deleteRecursively() }
                            withContext(Dispatchers.Main) {
                                selectedFiles = emptySet()
                                refresh()
                            }
                        }
                    }) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray) }
                    
                    IconButton(onClick = {
                        clipboard = Pair(selectedFiles.toList(), false)
                        selectedFiles = emptySet()
                    }) { Icon(Icons.Default.FileCopy, contentDescription = "Copy", tint = Color.LightGray) }
                    
                    IconButton(onClick = {
                        clipboard = Pair(selectedFiles.toList(), true)
                        selectedFiles = emptySet()
                    }) { Icon(Icons.Default.ContentCut, contentDescription = "Cut", tint = Color.LightGray) }
                    
                    if (selectedFiles.size == 1) {
                        IconButton(onClick = { showRenameDialog = selectedFiles.first() }) { Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.LightGray) }
                        IconButton(onClick = { showInfoDialog = selectedFiles.first() }) { Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.LightGray) }
                    }
                    
                    IconButton(onClick = { selectedFiles = emptySet() }) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.LightGray) }
                }
            }
        }
        
        if (showInfoDialog != null) {
            val file = showInfoDialog!!
            AlertDialog(
                onDismissRequest = { showInfoDialog = null },
                title = { Text("Properties") },
                text = {
                    Column {
                        Text("Name: ${file.name}")
                        Text("Path: ${file.absolutePath}")
                        Text("Size: ${if (file.isDirectory) "Directory" else file.length().toString() + " bytes"}")
                        Text("Modified: ${SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()).format(Date(file.lastModified()))}")
                        Text("Readable: ${file.canRead()}")
                        Text("Writable: ${file.canWrite()}")
                    }
                },
                confirmButton = { TextButton(onClick = { showInfoDialog = null }) { Text("OK") } }
            )
        }
        
        if (showRenameDialog != null) {
            val fileToRename = showRenameDialog!!
            var newName by remember { mutableStateOf(fileToRename.name) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = null },
                title = { Text("Rename") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank() && newName != fileToRename.name) {
                            fileToRename.renameTo(File(fileToRename.parent, newName))
                            refresh()
                        }
                        showRenameDialog = null
                        selectedFiles = emptySet()
                    }) { Text("Rename") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
                }
            )
        }
        
        if (showBookmarksDialog) {
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
        
        if (showCreateDialog) {
            var newName by remember { mutableStateOf("") }
            var isFolder by remember { mutableStateOf(true) }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New") },
                text = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isFolder, onClick = { isFolder = true })
                            Text("Folder", modifier = Modifier.clickable { isFolder = true })
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(selected = !isFolder, onClick = { isFolder = false })
                            Text("File", modifier = Modifier.clickable { isFolder = false })
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Name") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank()) {
                            val f = File(currentPath, newName)
                            if (isFolder) {
                                f.mkdirs()
                            } else {
                                f.createNewFile()
                            }
                            refresh()
                        }
                        showCreateDialog = false
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
