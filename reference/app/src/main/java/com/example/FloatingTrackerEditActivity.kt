package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.AppDatabase
import com.example.data.TrackerBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FloatingTrackerEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val bookTitle = intent.getStringExtra("book_title") ?: ""
        
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val db = remember { AppDatabase.getDatabase(this) }
                val scope = rememberCoroutineScope()
                var book by remember { mutableStateOf<TrackerBook?>(null) }
                var loading by remember { mutableStateOf(true) }

                LaunchedEffect(bookTitle) {
                    val books = db.trackerDao().getAllBooks()
                    val existing = books.find { it.title.equals(bookTitle, ignoreCase = true) }
                    if (existing != null) {
                        book = existing
                    } else {
                        book = TrackerBook(title = bookTitle)
                    }
                    loading = false
                }

                if (!loading && book != null) {
                    var title by remember { mutableStateOf(book!!.title) }
                    var author by remember { mutableStateOf(book!!.author) }
                    var totalChapters by remember { mutableStateOf(if (book!!.totalChapters > 0) book!!.totalChapters.toString() else "") }
                    var readChapters by remember { mutableStateOf(if (book!!.readChapters > 0) book!!.readChapters.toString() else "") }
                    var isFinished by remember { mutableStateOf(book!!.isFinished) }
                    var isWebNovel by remember { mutableStateOf(book!!.isWebNovel) }
                    var genres by remember { mutableStateOf(book!!.genres) }
                    var rating by remember { mutableStateOf(if (book!!.rating > 0) book!!.rating.toString() else "") }
                    var comment by remember { mutableStateOf(book!!.comment) }

                    AlertDialog(
                        onDismissRequest = { finish() },
                        title = { Text(if (book!!.id == 0) "Add Book" else "Edit Book") },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, singleLine = true)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = readChapters, onValueChange = { readChapters = it }, label = { Text("Read Chap") }, modifier = Modifier.weight(1f), singleLine = true)
                                    OutlinedTextField(value = totalChapters, onValueChange = { totalChapters = it }, label = { Text("Total Chap") }, modifier = Modifier.weight(1f), singleLine = true)
                                }
                                OutlinedTextField(value = genres, onValueChange = { genres = it }, label = { Text("Genre Tags (comma seq)") }, singleLine = true)
                                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Comment") })
                                OutlinedTextField(value = rating, onValueChange = { rating = it }, label = { Text("Rating (1-5)") }, singleLine = true)
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = isFinished, onCheckedChange = { isFinished = it })
                                    Text("Finished")
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Checkbox(checked = isWebNovel, onCheckedChange = { isWebNovel = it })
                                    Text("Web Novel")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val updated = TrackerBook(
                                    id = book!!.id,
                                    title = title,
                                    author = author,
                                    readChapters = readChapters.toIntOrNull() ?: 0,
                                    totalChapters = totalChapters.toIntOrNull() ?: 0,
                                    genres = genres,
                                    comment = comment,
                                    isFinished = isFinished,
                                    isWebNovel = isWebNovel,
                                    rating = rating.toFloatOrNull() ?: 0f,
                                    addedTimestamp = if (book!!.id == 0) System.currentTimeMillis() else book!!.addedTimestamp,
                                    lastUpdatedTimestamp = System.currentTimeMillis()
                                )
                                scope.launch {
                                    if (updated.id == 0) {
                                        db.trackerDao().insertBook(updated)
                                    } else {
                                        db.trackerDao().updateBook(updated)
                                    }
                                    withContext(Dispatchers.Main) {
                                        finish()
                                    }
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { finish() }) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}
