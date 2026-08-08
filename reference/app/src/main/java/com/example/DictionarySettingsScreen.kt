package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Translate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.service.DictionaryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { Room.databaseBuilder(context.applicationContext, DictionaryDatabase::class.java, "dictionary.db").fallbackToDestructiveMigration().build() }
    val prefs = context.getSharedPreferences("FloatingReaderPrefs", Context.MODE_PRIVATE)
    
    var dicts by remember { mutableStateOf<List<String>>(emptyList()) }
    var activeDict by remember { mutableStateOf(prefs.getString("active_dict", "English") ?: "English") }
    var fontScale by remember { mutableStateOf(prefs.getFloat("dict_font_size_scale", 1.0f)) }
    val scope = rememberCoroutineScope()
    
    fun loadDicts() {
        scope.launch(Dispatchers.IO) {
            val list = db.dictionaryDao().getAvailableDictionaries()
            withContext(Dispatchers.Main) {
                dicts = list
                if (list.isNotEmpty() && !list.contains(activeDict)) {
                    activeDict = list.first()
                    prefs.edit().putString("active_dict", activeDict).apply()
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadDicts()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dictionary & Translations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(Intent(context, DictionaryImportActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }) {
                Icon(Icons.Default.Add, contentDescription = "Import Dictionary")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Select Active Dictionary:", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            
            if (dicts.isEmpty()) {
                Text("No dictionaries imported.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {
                    items(dicts) { dictName ->
                        ListItem(
                            headlineContent = { Text(dictName) },
                            leadingContent = {
                                RadioButton(
                                    selected = dictName == activeDict,
                                    onClick = {
                                        activeDict = dictName
                                        prefs.edit().putString("active_dict", dictName).apply()
                                    }
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        db.dictionaryDao().clearDictionary(dictName)
                                        loadDicts()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.clickable {
                                activeDict = dictName
                                prefs.edit().putString("active_dict", dictName).apply()
                            }
                        )
                        Divider()
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                headlineContent = { Text("Font Size Scale") },
                supportingContent = {
                    Slider(
                        value = fontScale,
                        onValueChange = { 
                            fontScale = it
                            prefs.edit().putFloat("dict_font_size_scale", it).apply()
                        },
                        valueRange = 0.5f..2.5f,
                        steps = 19
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You can import StarDict dictionaries (.idx and .dict/.dict.dz wrapped in a .zip). Search GitHub for 'StarDict dictionaries' to find compatible files.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TranslationSettingsSection(context)
        }
    }
}

@Composable
fun TranslationSettingsSection(context: Context) {
    val prefs = context.getSharedPreferences("TranslationPrefs", Context.MODE_PRIVATE)
    var targetLanguage by remember { mutableStateOf(prefs.getString("default_target_lang", com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH) ?: com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH) }
    
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Translations",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        ListItem(
            headlineContent = { Text("Default Target Language") },
            supportingContent = { Text(java.util.Locale(targetLanguage).displayLanguage) },
            modifier = Modifier.clickable {
                // For simplicity, cycle through a few common languages or open manager.
                // Or better, just open translation manager to pick.
                context.startActivity(Intent(context, com.example.service.TranslationManagementActivity::class.java))
            }
        )
        Divider()
        ListItem(
            headlineContent = { Text("Manage Language Models") },
            supportingContent = { Text("Download offline ML Kit translation models") },
            trailingContent = { Icon(Icons.Filled.Translate, "Translate") },
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, com.example.service.TranslationManagementActivity::class.java))
            }
        )
    }
}
