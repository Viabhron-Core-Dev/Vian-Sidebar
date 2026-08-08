package com.example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.AppDatabase
import com.example.data.AppyworkProject
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppyworkSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).appyworkDao() }
    val scope = rememberCoroutineScope()
    
    val projects by dao.getAllProjectsFlow().collectAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<AppyworkProject?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appywork Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        editingProject = null
                        showDialog = true
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Project")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text("Vibe Coding Projects", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            }
            
            if (projects.isEmpty()) {
                item {
                    Text("No projects configured yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(projects) { project ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                editingProject = project
                                showDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(project.name, style = MaterialTheme.typography.titleMedium)
                                Text(project.remoteUrl.ifBlank { "No URL" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Auth: ${project.authType}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                scope.launch { dao.deleteProject(project) }
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        
        if (showDialog) {
            AppyworkProjectDialog(
                initialProject = editingProject,
                onDismiss = { showDialog = false },
                onSave = { project ->
                    scope.launch {
                        if (project.id == 0) {
                            dao.insertProject(project)
                        } else {
                            dao.updateProject(project)
                        }
                        showDialog = false
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppyworkProjectDialog(
    initialProject: AppyworkProject?,
    onDismiss: () -> Unit,
    onSave: (AppyworkProject) -> Unit
) {
    var name by remember { mutableStateOf(initialProject?.name ?: "") }
    var remoteUrl by remember { mutableStateOf(initialProject?.remoteUrl ?: "") }
    var authType by remember { mutableStateOf(initialProject?.authType ?: "PAT") }
    var authToken by remember { mutableStateOf(initialProject?.authToken ?: "") }
    
    val authOptions = listOf("PAT", "GITHUB_APP", "MCP")
    var authDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProject == null) "New Project" else "Edit Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = remoteUrl,
                    onValueChange = { remoteUrl = it },
                    label = { Text("Remote Git URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Auth Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = authDropdownExpanded,
                    onExpandedChange = { authDropdownExpanded = !authDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = authType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Auth Type") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = authDropdownExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = authDropdownExpanded,
                        onDismissRequest = { authDropdownExpanded = false }
                    ) {
                        authOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    authType = option
                                    authDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = authToken,
                    onValueChange = { authToken = it },
                    label = { Text("Token/Secret") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        AppyworkProject(
                            id = initialProject?.id ?: 0,
                            name = name,
                            remoteUrl = remoteUrl,
                            authType = authType,
                            authToken = authToken,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
