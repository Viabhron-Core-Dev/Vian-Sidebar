package com.example.core

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import java.util.Locale

/**
 * Clean, lightweight, high-contrast dynamic status bar speed icon generator.
 * Uses a standardized high-density supersampled canvas (96x96 px) with clean native Paint rendering,
 * mirroring standard Android SystemUI clock/battery typography with zero downsampling blur.
 */
object DynamicSpeedIconGenerator {

    data class SpeedDisplay(val number: String, val unit: String)

    private const val CANVAS_SIZE = 96

    private var cachedBitmap: Bitmap? = null
    private var cachedCanvas: Canvas? = null
    private var cachedDensityDpi: Int = -1

    private val typeface: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    private val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = this@DynamicSpeedIconGenerator.typeface
        textAlign = Paint.Align.CENTER
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = this@DynamicSpeedIconGenerator.typeface
        textAlign = Paint.Align.CENTER
    }

    private val textBounds = Rect()

    fun loadConfig(prefs: SharedPreferences) {
        // Standardized clean rendering
    }

    fun formatSpeed(bytesPerSec: Long, forcedUnit: String? = null): SpeedDisplay {
        if (bytesPerSec <= 0) {
            return SpeedDisplay("0", if (forcedUnit == "MB/s") "MB/s" else "KB/s")
        }

        val kbps = bytesPerSec / 1024.0
        val mbps = kbps / 1024.0

        return when (forcedUnit) {
            "KB/s" -> {
                val kb = ((bytesPerSec + 512) / 1024).toInt()
                SpeedDisplay(kb.toString(), "KB/s")
            }
            "MB/s" -> {
                val str = if (mbps < 10.0) String.format(Locale.US, "%.1f", mbps) else String.format(Locale.US, "%.0f", mbps)
                SpeedDisplay(str, "MB/s")
            }
            else -> {
                when {
                    bytesPerSec < 1000 -> {
                        SpeedDisplay("0", "KB/s")
                    }
                    bytesPerSec < 1000 * 1024 -> {
                        val kb = ((bytesPerSec + 512) / 1024).toInt()
                        SpeedDisplay(kb.toString(), "KB/s")
                    }
                    bytesPerSec < 100L * 1024 * 1024 -> {
                        val str = if (mbps < 10.0) String.format(Locale.US, "%.1f", mbps) else mbps.toInt().toString()
                        SpeedDisplay(str, "MB/s")
                    }
                    else -> {
                        val mb = (bytesPerSec / (1024 * 1024)).toInt()
                        SpeedDisplay(mb.toString(), "MB/s")
                    }
                }
            }
        }
    }

    /**
     * Generates a clean supersampled high-density bitmap for the status bar notification icon.
     */
    fun generateStatusBarBitmap(
        context: Context,
        bytesPerSec: Long,
        forcedUnit: String? = null
    ): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        val densityDpi = context.resources.displayMetrics.densityDpi

        var bitmap = cachedBitmap
        var canvas = cachedCanvas

        if (bitmap == null || bitmap.isRecycled || cachedDensityDpi != densityDpi) {
            bitmap = Bitmap.createBitmap(CANVAS_SIZE, CANVAS_SIZE, Bitmap.Config.ARGB_8888)
            bitmap.density = densityDpi
            canvas = Canvas(bitmap)
            cachedBitmap = bitmap
            cachedCanvas = canvas
            cachedDensityDpi = densityDpi
        }

        bitmap.eraseColor(Color.TRANSPARENT)
        renderIconToCanvas(canvas!!, CANVAS_SIZE, 1f, display)

        return bitmap
    }

    fun renderIconToCanvas(
        canvas: Canvas,
        sizePx: Int,
        density: Float,
        display: SpeedDisplay
    ) {
        val centerX = sizePx / 2f

        // High-contrast clean font sizing on the 96x96 canvas
        // 1-2 digits: size 64; 3 digits: size 54; 4+ digits: size 46
        val numLen = display.number.length
        val numTextSize = when {
            numLen <= 2 -> 62f
            numLen == 3 -> 52f
            else -> 42f
        }
        numPaint.textSize = numTextSize
        numPaint.getTextBounds(display.number, 0, display.number.length, textBounds)
        
        // Center the number in the upper 70% portion
        val numY = 52f - (textBounds.top + textBounds.bottom) / 2f
        canvas.drawText(display.number, centerX, numY, numPaint)

        // Clean unit string in the lower 30% portion
        unitPaint.textSize = 26f
        unitPaint.getTextBounds(display.unit, 0, display.unit.length, textBounds)
        val unitY = 82f - (textBounds.top + textBounds.bottom) / 2f
        canvas.drawText(display.unit, centerX, unitY, unitPaint)
    }

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
