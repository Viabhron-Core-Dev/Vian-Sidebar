package com.example

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.service.PwaDatabase
import com.example.service.PwaEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.lifecycleScope

class PwaImportActivity : ComponentActivity() {

    private lateinit var db: PwaDatabase
    private var isImporting by mutableStateOf(false)
    private var importStatus by mutableStateOf("Ready to import.")
    
    private var selectedUri by mutableStateOf<Uri?>(null)
    private var pwaName by mutableStateOf("")
    private var isLightweight by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = PwaDatabase.getDatabase(applicationContext)

        val pickZipLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                selectedUri = uri
                pwaName = "App_${System.currentTimeMillis()}" // Default name
            } else {
                finish()
            }
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (selectedUri == null) {
                        LaunchedEffect(Unit) {
                            pickZipLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Import PWA", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            OutlinedTextField(
                                value = pwaName,
                                onValueChange = { pwaName = it },
                                label = { Text("PWA Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isLightweight,
                                    onCheckedChange = { isLightweight = it }
                                )
                                Text("Lightweight (Floating Window)")
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(importStatus, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isImporting) {
                                CircularProgressIndicator()
                            } else {
                                Button(onClick = {
                                    importZip(selectedUri!!, pwaName, isLightweight)
                                }) {
                                    Text("Import")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun importZip(uri: Uri, name: String, lightweight: Boolean) {
        isImporting = true
        importStatus = "Copying ZIP..."
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fileName = "pwa_${System.currentTimeMillis()}.zip"
                val destFile = File(filesDir, fileName)
                
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val prefs = getSharedPreferences("PwaDefaults", android.content.Context.MODE_PRIVATE)
                val defaultVirtualHost = prefs.getBoolean("defaultUseVirtualHost", true)
                val defaultIncognito = prefs.getBoolean("defaultIncognitoMode", false)
                db.pwaDao().insertPwa(PwaEntry(
                    name = name,
                    zipPath = destFile.absolutePath,
                    isLightweight = lightweight,
                    useVirtualHost = defaultVirtualHost,
                    incognitoMode = defaultIncognito
                ))
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PwaImportActivity, "Import complete!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    importStatus = "Error: ${e.message}"
                    isImporting = false
                }
            }
        }
    }
}
