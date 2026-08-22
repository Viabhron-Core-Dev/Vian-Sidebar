package com.example.feature.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.core.LogKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LogKeeperActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    LogKeeperScreen(onClose = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogKeeperScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Normal Logs, 1 = Crash Logs
    var normalLogContent by remember { mutableStateOf("") }
    var crashLogContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun loadLogs() {
        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            val normalText = readLogFile(context, "LiteReader_Log.txt")
            val crashText = readLogFile(context, "LiteReader_CrashLog.txt")
            withContext(Dispatchers.Main) {
                normalLogContent = normalText
                crashLogContent = crashText
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLogs()
    }

    val currentContent = if (selectedTab == 0) normalLogContent else crashLogContent
    val currentFileName = if (selectedTab == 0) "LiteReader_Log.txt" else "LiteReader_CrashLog.txt"

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = {
                    Text("Log Keeper", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Refresh
                    IconButton(onClick = { loadLogs() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    // Copy
                    IconButton(
                        onClick = {
                            if (currentContent.isNotBlank()) {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(currentFileName, currentContent)
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "No logs to copy", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Color.White)
                    }
                    // Share
                    IconButton(
                        onClick = {
                            if (currentContent.isNotBlank()) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, currentFileName)
                                    putExtra(Intent.EXTRA_TEXT, currentContent)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
                            } else {
                                Toast.makeText(context, "No logs to share", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                    }
                    // Clear / Delete
                    IconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                clearLogFile(context, currentFileName)
                                withContext(Dispatchers.Main) {
                                    loadLogs()
                                    Toast.makeText(context, "$currentFileName cleared", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear", tint = Color(0xFFFF5252))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color(0xFF00E676)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("System Logs")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (crashLogContent.isNotBlank()) Color(0xFFFF5252) else Color.Gray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crash Logs", color = if (crashLogContent.isNotBlank()) Color(0xFFFF5252) else Color.White)
                        }
                    }
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E676))
                }
            } else if (currentContent.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Info else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color.Gray else Color(0xFF00E676),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "No activity logs recorded yet." else "No crashes detected! Everything running smoothly.",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                val lines = remember(currentContent) { currentContent.lines() }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0D0D0D))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(lines) { line ->
                        val textColor = when {
                            line.startsWith("===") || line.startsWith("---") -> Color(0xFF00E676)
                            line.contains("Exception") || line.contains("Error") || line.contains("FATAL") -> Color(0xFFFF5252)
                            line.contains("Timestamp:") || line.contains("Thread:") -> Color(0xFF81D4FA)
                            line.trim().startsWith("at ") -> Color(0xFFB0BEC5)
                            else -> Color(0xFFE0E0E0)
                        }

                        Text(
                            text = line,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getTargetLogDirectory(context: Context): File? {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (downloadsDir != null && downloadsDir.exists()) {
        return downloadsDir
    }
    val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    if (fallbackDir != null && fallbackDir.exists()) {
        return fallbackDir
    }
    return context.filesDir
}

private fun readLogFile(context: Context, fileName: String): String {
    return try {
        val dir = getTargetLogDirectory(context) ?: return ""
        val file = File(dir, fileName)
        if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}

private fun clearLogFile(context: Context, fileName: String) {
    try {
        val dir = getTargetLogDirectory(context) ?: return
        val file = File(dir, fileName)
        if (file.exists()) {
            file.delete()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
