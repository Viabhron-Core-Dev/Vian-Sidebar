import os

code = """package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.FileProvider

class FileExplorerWindowManager(
    private val context: Context,
    private val onClose: () -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: View? = null
    
    private val _currentPath = mutableStateOf(android.os.Environment.getExternalStorageDirectory().absolutePath)
    
    fun openPath(path: String) {
        val f = File(path)
        if (f.exists() && f.isDirectory) {
            _currentPath.value = path
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (view != null) return
        
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.CENTER
        
        val frameLayout = FrameLayout(context)
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
                    FileExplorerUI(
                        initialPath = _currentPath.value,
                        onClose = { close() },
                        context = context
                    )
                }
            }
        }
        
        frameLayout.addView(composeView)
        view = frameLayout
        windowManager.addView(view, layoutParams)
    }

    fun close() {
        try {
            if (view != null) {
                windowManager.removeView(view)
                view = null
                onClose()
            }
        } catch (e: Exception) {}
    }
}
"""
with open("app/src/main/java/com/example/service/FileExplorerWindowManager.kt", "w") as f:
    f.write(code)
