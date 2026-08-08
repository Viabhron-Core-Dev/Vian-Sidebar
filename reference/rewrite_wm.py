import re

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'r') as f:
    content = f.read()

# find the @Composable fun FileExplorerUI
compose_start = content.find("@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)")

compose_part = content[compose_start:]

new_top = """package com.example.service

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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.R
import com.example.utils.ActiveAppTracker
import kotlin.math.max

class FileExplorerWindowManager(
    private val context: Context,
    private val onClose: () -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: View? = null
    
    private val prefs = context.getSharedPreferences("FileExplorerPrefs", Context.MODE_PRIVATE)
    private var isMinimized = prefs.getBoolean("isMinimized", false)
    private var lastWidth = prefs.getInt("lastWidth", 800)
    private var lastHeight = prefs.getInt("lastHeight", 1000)
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private var initialX = prefs.getInt("lastX", 100)
    private var initialY = prefs.getInt("lastY", 200)
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private val _currentPath = mutableStateOf(android.os.Environment.getExternalStorageDirectory().absolutePath)
    
    fun openPath(path: String) {
        val f = File(path)
        if (f.exists() && f.isDirectory) {
            _currentPath.value = path
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun show() {
        if (floatingView != null) return
        val density = context.resources.displayMetrics.density
        lastWidth = (320 * density).toInt()
        lastHeight = (450 * density).toInt()

        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        windowLayoutParams = WindowManager.LayoutParams(
            if (isMinimized) (56 * density).toInt() else lastWidth,
            if (isMinimized) (56 * density).toInt() else lastHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("lastX", 100)
            y = prefs.getInt("lastY", 200)
        }
        
        val inflater = LayoutInflater.from(context)
        floatingView = inflater.inflate(R.layout.layout_file_explorer_floating, null)
        
        val expandedContainer = floatingView?.findViewById<LinearLayout>(R.id.file_explorer_expanded_container)
        val bubble = floatingView?.findViewById<FrameLayout>(R.id.file_explorer_bubble)
        val topbar = floatingView?.findViewById<LinearLayout>(R.id.file_explorer_topbar)
        
        if (isMinimized) {
            expandedContainer?.visibility = View.GONE
            bubble?.visibility = View.VISIBLE
        }
        
        val btnClose = floatingView?.findViewById<Button>(R.id.file_explorer_btn_close)
        val btnFold = floatingView?.findViewById<Button>(R.id.file_explorer_btn_fold)
        val btnResize = floatingView?.findViewById<ImageView>(R.id.file_explorer_btn_resize)
        val contentContainer = floatingView?.findViewById<FrameLayout>(R.id.file_explorer_content_container)

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
        
        contentContainer?.removeAllViews()
        contentContainer?.addView(composeView)
        
        ActiveAppTracker.addApp("fileexplorer_${this.hashCode()}", "File Explorer", "Files", 60)
        
        btnClose?.setOnClickListener { close() }
        btnFold?.setOnClickListener { toggleMinimize(expandedContainer, bubble, density) }
        bubble?.setOnClickListener { toggleMinimize(expandedContainer, bubble, density) }
        
        topbar?.setOnTouchListener { _, event -> handleDrag(event) }
        bubble?.setOnTouchListener { _, event -> handleDrag(event, true) { toggleMinimize(expandedContainer, bubble, density) } }
        btnResize?.setOnTouchListener { _, event -> handleResize(event, density) }
        
        try {
            windowManager.addView(floatingView, windowLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleMinimize(expandedContainer: View?, bubble: View?, density: Float) {
        isMinimized = !isMinimized
        prefs.edit().putBoolean("isMinimized", isMinimized).apply()
        if (isMinimized) {
            expandedContainer?.visibility = View.GONE
            bubble?.visibility = View.VISIBLE
            
            lastWidth = windowLayoutParams?.width ?: (320 * density).toInt()
            lastHeight = windowLayoutParams?.height ?: (450 * density).toInt()
            
            windowLayoutParams?.width = (56 * density).toInt()
            windowLayoutParams?.height = (56 * density).toInt()
        } else {
            expandedContainer?.visibility = View.VISIBLE
            bubble?.visibility = View.GONE
            
            windowLayoutParams?.width = lastWidth
            windowLayoutParams?.height = lastHeight
        }
        windowManager.updateViewLayout(floatingView, windowLayoutParams)
    }

    private fun handleDrag(event: MotionEvent, isBubble: Boolean = false, onClick: (() -> Unit)? = null): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = windowLayoutParams?.x ?: 0
                initialY = windowLayoutParams?.y ?: 0
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                windowLayoutParams?.x = initialX + (event.rawX - initialTouchX).toInt()
                windowLayoutParams?.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(floatingView, windowLayoutParams)
                return true
            }
            MotionEvent.ACTION_UP -> {
                prefs.edit().putInt("lastX", windowLayoutParams?.x ?: 100).putInt("lastY", windowLayoutParams?.y ?: 200).apply()
                val diffX = Math.abs(event.rawX - initialTouchX)
                val diffY = Math.abs(event.rawY - initialTouchY)
                if (diffX < 10 && diffY < 10) {
                    onClick?.invoke()
                }
                return true
            }
        }
        return false
    }

    private fun handleResize(event: MotionEvent, density: Float): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = windowLayoutParams?.width ?: (320 * density).toInt()
                initialY = windowLayoutParams?.height ?: (450 * density).toInt()
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val newWidth = initialX + (event.rawX - initialTouchX).toInt()
                val newHeight = initialY + (event.rawY - initialTouchY).toInt()
                windowLayoutParams?.width = max((200 * density).toInt(), newWidth)
                windowLayoutParams?.height = max((200 * density).toInt(), newHeight)
                lastWidth = windowLayoutParams?.width ?: 800
                lastHeight = windowLayoutParams?.height ?: 1000
                prefs.edit().putInt("lastWidth", lastWidth).putInt("lastHeight", lastHeight).apply()
                windowManager.updateViewLayout(floatingView, windowLayoutParams)
                return true
            }
        }
        return false
    }

    fun close() {
        try {
            if (floatingView != null) {
                windowManager.removeView(floatingView)
                floatingView = null
                onClose()
            }
        } catch (e: Exception) {}
    }
}

"""

with open('app/src/main/java/com/example/service/FileExplorerWindowManager.kt', 'w') as f:
    f.write(new_top + compose_part)

