package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VianSideAccessibilityService : AccessibilityService() {
    private var autoScrollManager: AutoScrollManager? = null
    private var cursorManager: CursorManager? = null
    private var longScreenshotManager: LongScreenshotManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        autoScrollManager = AutoScrollManager(this)
        cursorManager = CursorManager(this)
        longScreenshotManager = LongScreenshotManager(this)
        com.example.LogKeeper.writeLog("VianSideAccessibility", "Service connected")
        android.util.Log.d("VianSideAccessibility", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        if (isForceStopping) {
            val rootNode = rootInActiveWindow ?: return
            
            // We just watch for the "Force stop" button to be disabled.
            // When it becomes disabled (meaning the user clicked it and confirmed it),
            // or if it was already disabled, we perform the BACK action to go to the next app.
            val forceStopNodes = rootNode.findAccessibilityNodeInfosByText("Force stop")
            var buttonIsDisabled = false
            for (node in forceStopNodes) {
                if (node.isClickable && !node.isEnabled) {
                    buttonIsDisabled = true
                    break
                } else if (node.parent?.isClickable == true && node.parent?.isEnabled == false) {
                    buttonIsDisabled = true
                    break
                }
            }
            
            if (buttonIsDisabled) {
                // If the app is already force stopped (or just got stopped), go back to process next app
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    override fun onInterrupt() {
        // Not used
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        com.example.LogKeeper.writeLog("VianSideAccessibility", "Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        com.example.LogKeeper.writeLog("VianSideAccessibility", "Service destroyed")
    }

    fun performAction(action: String): Boolean {
        com.example.LogKeeper.writeLog("VianSideAccessibility", "Performing action: $action")
        
        if (action == "cursor") {
            if (cursorManager?.isRunning == true) cursorManager?.stop() else cursorManager?.start()
            return true
        }
        if (action == "auto_scroll") {
            if (autoScrollManager?.isRunning == true) autoScrollManager?.stop() else autoScrollManager?.start()
            return true
        }
        if (action == "long_screenshot") {
            longScreenshotManager?.start()
            return true
        }

        if (action == "screenshot") {
            handleScreenshotWithDelay()
            return true
        }
        if (action == "barcode_scanner") {
            val intent = Intent(this, com.example.service.BarcodeScannerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return true
        }
        if (action == "qr_scan") {
            handleQRScan()
            return true
        }

        return when (action) {
            "back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            "recents" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            "notifications" -> performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            "quick_settings" -> performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            "lock_screen" -> performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            "splitscreen" -> performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
            else -> false
        }
    }

    private fun handleScreenshotWithDelay() {
        val prefs = getSharedPreferences("ScreenCapPrefs", Context.MODE_PRIVATE)
        val delaySec = prefs.getInt("screenshot_delay", 0)
        if (delaySec > 0) {
            Toast.makeText(this, "Screenshot in $delaySec seconds", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                takeCustomScreenshot(prefs)
            }, delaySec * 1000L)
        } else {
            takeCustomScreenshot(prefs)
        }
    }

    private fun takeCustomScreenshot(prefs: android.content.SharedPreferences) {
        val saveLocation = prefs.getString("save_location", "Default (Pictures/Screenshots)") ?: "Default (Pictures/Screenshots)"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && saveLocation != "Default (Pictures/Screenshots)") {
            takeScreenshot(android.view.Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    try {
                        val hwBuffer = screenshotResult.hardwareBuffer
                        val colorSpace = screenshotResult.colorSpace
                        val bitmap = Bitmap.wrapHardwareBuffer(hwBuffer, colorSpace)
                        if (bitmap != null) {
                            saveBitmapToCustomLocation(bitmap, saveLocation)
                        } else {
                            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                        }
                        hwBuffer.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                    }
                }

                override fun onFailure(errorCode: Int) {
                    performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
                }
            })
        } else {
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        }
    }

    private fun saveBitmapToCustomLocation(bitmap: Bitmap, locationUriStr: String) {
        try {
            val uri = Uri.parse(locationUriStr)
            val dir = DocumentFile.fromTreeUri(this, uri)
            if (dir != null && dir.isDirectory) {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "Screenshot_$timestamp.png"
                val file = dir.createFile("image/png", fileName)
                if (file != null) {
                    val out: OutputStream? = contentResolver.openOutputStream(file.uri)
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this, "Screenshot saved to custom location", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, "Failed to save to custom location, using default", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
        }
    }


    private fun handleQRScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(this, "Preparing Screen Capture...", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                takeScreenshot(android.view.Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    try {
                        val hwBuffer = screenshotResult.hardwareBuffer
                        val colorSpace = screenshotResult.colorSpace
                        val bitmap = Bitmap.wrapHardwareBuffer(hwBuffer, colorSpace)
                        if (bitmap != null) {
                            val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                            launchCropActivity(softwareBitmap)
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(this@VianSideAccessibilityService, "Failed to get screenshot", Toast.LENGTH_SHORT).show()
                            }
                        }
                        hwBuffer.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this@VianSideAccessibilityService, "Error reading screenshot", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(errorCode: Int) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this@VianSideAccessibilityService, "Failed to take screenshot", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            }, 400) // Delay to let sidebar close
        } else {
            Toast.makeText(this, "Screen QR Scanner requires Android 11+", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchCropActivity(bitmap: Bitmap) {
        Thread {
            try {
                val cacheFile = java.io.File(cacheDir, "temp_qr_screenshot.jpg")
                java.io.FileOutputStream(cacheFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                val intent = Intent(this@VianSideAccessibilityService, QRCropActivity::class.java).apply {
                    putExtra("IMAGE_PATH", cacheFile.absolutePath)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this@VianSideAccessibilityService, "Failed to prepare screenshot", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun scanBitmapForQRCode(bitmap: Bitmap) {
        Thread {
            try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                
                val source = com.google.zxing.RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
                
                val reader = com.google.zxing.MultiFormatReader()
                val result = reader.decode(binaryBitmap)
                
                val text = result.text
                if (text.isNullOrEmpty()) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "No QR Code found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "QR Code found!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val chooser = Intent.createChooser(intent, "QR Code Result")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(chooser)
                    }
                }
            } catch (e: com.google.zxing.NotFoundException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "No QR Code found on screen", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Error scanning QR Code", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    companion object {
        var instance: VianSideAccessibilityService? = null
            private set
        var isForceStopping = false
    }
}
