package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.service.PwaDatabase
import com.example.service.PwaEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PwaManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = PwaDatabase.getDatabase(applicationContext)
        val prefs = getSharedPreferences("PwaDefaults", Context.MODE_PRIVATE)

        setContent {
            MaterialTheme {
                PwaManagerScreen(
                    db = db,
                    prefs = prefs,
                    onImportClick = {
                        startActivity(Intent(this, PwaImportActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PwaManagerScreen(
    db: PwaDatabase,
    prefs: android.content.SharedPreferences,
    onImportClick: () -> Unit
) {
    var pwas by remember { mutableStateOf(emptyList<PwaEntry>()) }
    var selectedPwa by remember { mutableStateOf<PwaEntry?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    var defaultUseVirtualHost by remember { mutableStateOf(prefs.getBoolean("defaultUseVirtualHost", true)) }
    var defaultIncognitoMode by remember { mutableStateOf(prefs.getBoolean("defaultIncognitoMode", false)) }
    
    LaunchedEffect(Unit) {
        db.pwaDao().getAllPwas().collect { list ->
            pwas = list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("PWA Manager") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onImportClick) {
                Icon(Icons.Default.Add, contentDescription = "Import PWA")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // General Settings
            Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Default Settings for New PWAs", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Virtual Host Interception", modifier = Modifier.weight(1f))
                        Switch(checked = defaultUseVirtualHost, onCheckedChange = { 
                            defaultUseVirtualHost = it
                            prefs.edit().putBoolean("defaultUseVirtualHost", it).apply()
                        })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Ephemeral/Incognito Mode", modifier = Modifier.weight(1f))
                        Switch(checked = defaultIncognitoMode, onCheckedChange = { 
                            defaultIncognitoMode = it
                            prefs.edit().putBoolean("defaultIncognitoMode", it).apply()
                        })
                    }
                }
            }
            
            Text("Imported PWAs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(pwas) { pwa ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    val intent = Intent(context, com.example.service.SidebarService::class.java).apply {
                                        action = "EXECUTE_ACTION"
                                        putExtra("ACTION_ID", "pwa:${pwa.id}")
                                    }
                                    context.startService(intent)
                                },
                                onLongClick = { showEditDialog = true; selectedPwa = pwa }
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(pwa.name, style = MaterialTheme.typography.titleMedium)
                            Text("Virtual Host: ${pwa.useVirtualHost} | Incognito: ${pwa.incognitoMode} | Port: ${if (pwa.persistentPort > 0) pwa.persistentPort else "Ephemeral"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedPwa != null) {
        val pwa = selectedPwa!!
        var editName by remember { mutableStateOf(pwa.name) }
        var editVirtualHost by remember { mutableStateOf(pwa.useVirtualHost) }
        var editIncognito by remember { mutableStateOf(pwa.incognitoMode) }
        var editPersistentPort by remember { mutableStateOf(pwa.persistentPort.toString()) }
        val coroutineScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit PWA: ${pwa.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Virtual Host Interception", modifier = Modifier.weight(1f))
                        Switch(checked = editVirtualHost, onCheckedChange = { editVirtualHost = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Incognito Mode", modifier = Modifier.weight(1f))
                        Switch(checked = editIncognito, onCheckedChange = { editIncognito = it })
                    }
                    OutlinedTextField(
                        value = editPersistentPort,
                        onValueChange = { editPersistentPort = it },
                        label = { Text("Persistent Port (0 for ephemeral)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val port = editPersistentPort.toIntOrNull() ?: 0
                        val updatedPwa = pwa.copy(
                            name = editName,
                            useVirtualHost = editVirtualHost,
                            incognitoMode = editIncognito,
                            persistentPort = port
                        )
                        db.pwaDao().updatePwa(updatedPwa)
                        showEditDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        db.pwaDao().deletePwa(pwa)
                        showEditDialog = false
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}
