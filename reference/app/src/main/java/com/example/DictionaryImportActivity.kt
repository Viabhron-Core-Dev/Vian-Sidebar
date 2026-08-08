package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.service.DictionaryDatabase
import com.example.service.DictionaryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

class DictionaryImportActivity : ComponentActivity() {

    private lateinit var db: DictionaryDatabase
    private var isImporting by mutableStateOf(false)
    private var importProgress by mutableStateOf(0f)
    private var importStatus by mutableStateOf("Ready to import.")
    private var dictName by mutableStateOf("English")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext, DictionaryDatabase::class.java, "dictionary.db").fallbackToDestructiveMigration().build()

        val pickZipLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                importStarDictZip(uri, dictName)
            }
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("StarDict Importer", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = dictName,
                            onValueChange = { dictName = it },
                            label = { Text("Dictionary Name (e.g. English)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(importStatus, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (isImporting) {
                            LinearProgressIndicator(progress = { importProgress }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            Button(onClick = {
                                pickZipLauncher.launch(arrayOf("*/*"))
                            }) {
                                Text("Select StarDict ZIP File")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun importStarDictZip(uri: Uri, dictName: String) {
        isImporting = true
        importProgress = 0f
        importStatus = "Extracting..."
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Clear existing DB
                db.dictionaryDao().clearDictionary(dictName)

                val resolver = contentResolver
                
                var idxData: ByteArray? = null
                var dictData: ByteArray? = null
                var isDictGzipped = false

                // 1. Read ZIP
                resolver.openInputStream(uri)?.use { stream ->
                    val zis = ZipInputStream(stream)
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith(".idx")) {
                            idxData = zis.readBytes()
                        } else if (entry.name.endsWith(".dict")) {
                            dictData = zis.readBytes()
                            isDictGzipped = false
                        } else if (entry.name.endsWith(".dict.dz")) {
                            dictData = zis.readBytes()
                            isDictGzipped = true
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }

                if (idxData == null || dictData == null) {
                    withContext(Dispatchers.Main) {
                        importStatus = "Error: Invalid zip file. Missing .idx or .dict/.dict.dz file."
                        isImporting = false
                    }
                    return@launch
                }

                importStatus = "Parsing index..."

                // 2. Parse .idx
                // format: word_str (null terminated), offset (4 bytes, uint32), size (4 bytes, uint32)
                val buffer = ByteBuffer.wrap(idxData).order(ByteOrder.BIG_ENDIAN)
                val entries = mutableListOf<IdxEntry>()
                
                while (buffer.hasRemaining()) {
                    val wordBytes = ByteArrayOutputStream()
                    var b = buffer.get()
                    while (b.toInt() != 0) {
                        wordBytes.write(b.toInt())
                        b = buffer.get()
                    }
                    val word = String(wordBytes.toByteArray(), Charsets.UTF_8)
                    val offset = buffer.getInt().toUInt().toLong()
                    val size = buffer.getInt().toUInt().toLong()
                    entries.add(IdxEntry(word, offset, size))
                }

                // Sort by offset to read dict sequentially
                entries.sortBy { it.offset }
                
                importStatus = "Reading definitions..."

                // 3. Read .dict/.dict.dz
                val dictInputStream: InputStream = if (isDictGzipped) {
                    GZIPInputStream(dictData!!.inputStream())
                } else {
                    dictData!!.inputStream()
                }

                var currentOffset = 0L
                val dbEntries = mutableListOf<DictionaryEntry>()
                var count = 0
                val total = entries.size

                val dao = db.dictionaryDao()
                val batchSize = 1000

                dictInputStream.use { input ->
                    for (entry in entries) {
                        val skip = entry.offset - currentOffset
                        if (skip > 0) {
                            var skipped = 0L
                            while (skipped < skip) {
                                val s = input.skip(skip - skipped)
                                if (s <= 0) break // fallback, skip should work
                                skipped += s
                            }
                            currentOffset += skip
                        }

                        val defBytes = ByteArray(entry.size.toInt())
                        var read = 0
                        while (read < defBytes.size) {
                            val c = input.read(defBytes, read, defBytes.size - read)
                            if (c == -1) break
                            read += c
                        }
                        currentOffset += read

                        val definition = String(defBytes, Charsets.UTF_8)
                        dbEntries.add(DictionaryEntry(word = entry.word, definition = definition, dictName = dictName))
                        count++

                        if (dbEntries.size >= batchSize) {
                            dao.insertAll(dbEntries)
                            dbEntries.clear()
                            
                            val progress = count.toFloat() / total
                            withContext(Dispatchers.Main) {
                                importProgress = progress
                                importStatus = "Importing: $count / $total"
                            }
                        }
                    }
                    
                    if (dbEntries.isNotEmpty()) {
                        dao.insertAll(dbEntries)
                    }
                }

                withContext(Dispatchers.Main) {
                    importProgress = 1f
                    importStatus = "Import complete! $total words imported."
                    Toast.makeText(this@DictionaryImportActivity, "Import complete!", Toast.LENGTH_SHORT).show()
                    isImporting = false
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

    data class IdxEntry(val word: String, val offset: Long, val size: Long)
}
