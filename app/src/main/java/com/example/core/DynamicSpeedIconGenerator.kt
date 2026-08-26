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
 * Pure, lightweight status bar speed icon generator based on the Battery Indicator Pro (BatteryBot) method.
 * Renders pixel-snapped, ultra-sharp typography directly onto a 24dp status bar notification canvas.
 * Zero sliders, zero configuration overhead, 100% sharp text with zero bilinear downsampling blur.
 */
object DynamicSpeedIconGenerator {

    data class SpeedDisplay(val number: String, val unit: String)

    private var cachedBitmap: Bitmap? = null
    private var cachedCanvas: Canvas? = null
    private var cachedDensityDpi: Int = -1
    private var cachedSizePx: Int = -1
    
    private val typeface: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    private val numPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        color = Color.WHITE
        typeface = this@DynamicSpeedIconGenerator.typeface
        textAlign = Paint.Align.CENTER
        isDither = true
        isFilterBitmap = true
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        color = Color.WHITE
        typeface = this@DynamicSpeedIconGenerator.typeface
        textAlign = Paint.Align.CENTER
        isDither = true
        isFilterBitmap = true
    }

    private val textBounds = Rect()

    fun loadConfig(prefs: SharedPreferences) {
        // No-op: Battery Indicator Pro method uses standardized pixel-perfect geometry
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
     * Generates a 1:1 pixel-perfect bitmap for the status bar notification icon.
     */
    fun generateStatusBarBitmap(
        context: Context,
        bytesPerSec: Long,
        forcedUnit: String? = null
    ): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val densityDpi = displayMetrics.densityDpi

        // Standard status bar icon slot dimension (24dp)
        val statusBarResId = resources.getIdentifier("status_bar_icon_size", "dimen", "android")
        val systemSize = if (statusBarResId > 0) {
            try { resources.getDimensionPixelSize(statusBarResId) } catch (e: Exception) { 0 }
        } else {
            0
        }
        val sizePx = if (systemSize > 0) systemSize else Math.round(24f * density).coerceAtLeast(16)

        var bitmap = cachedBitmap
        var canvas = cachedCanvas

        if (bitmap == null || bitmap.isRecycled || cachedSizePx != sizePx || cachedDensityDpi != densityDpi) {
            bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            bitmap.density = densityDpi
            canvas = Canvas(bitmap)
            cachedBitmap = bitmap
            cachedCanvas = canvas
            cachedSizePx = sizePx
            cachedDensityDpi = densityDpi
        }

        bitmap.eraseColor(Color.TRANSPARENT)
        renderIconToCanvas(canvas!!, sizePx, density, display)

        return bitmap
    }

    fun renderIconToCanvas(
        canvas: Canvas,
        sizePx: Int,
        density: Float,
        display: SpeedDisplay
    ) {
        val centerX = Math.round(sizePx / 2f).toFloat()
        // 80/20 Stacked Split: 78-80% height dedicated to large prominent number, 20-22% to compact unit
        val numberSectionHeight = Math.round(sizePx * 0.78f).toFloat()
        val unitSectionHeight = sizePx - numberSectionHeight

        // Number: 16.5sp base size (prominent large digits), scaled if needed to fit width without clipping
        val baseNumSize = Math.round(16.5f * density).toFloat()
        val maxNumW = sizePx * 0.96f
        numPaint.textSize = baseNumSize
        val numTextW = numPaint.measureText(display.number)
        if (numTextW > maxNumW && numTextW > 0f) {
            numPaint.textSize = baseNumSize * (maxNumW / numTextW)
        }

        numPaint.getTextBounds(display.number, 0, display.number.length, textBounds)
        val numY = Math.round((numberSectionHeight / 2f) + (textBounds.height() / 2f) - textBounds.bottom).toFloat()

        // Unit: 6.5sp base size (compact crisp unit)
        val baseUnitSize = Math.round(6.5f * density).toFloat()
        val maxUnitW = sizePx * 0.96f
        unitPaint.textSize = baseUnitSize
        val unitTextW = unitPaint.measureText(display.unit)
        if (unitTextW > maxUnitW && unitTextW > 0f) {
            unitPaint.textSize = baseUnitSize * (maxUnitW / unitTextW)
        }

        unitPaint.getTextBounds(display.unit, 0, display.unit.length, textBounds)
        val unitY = Math.round(numberSectionHeight + (unitSectionHeight / 2f) + (textBounds.height() / 2f) - textBounds.bottom).toFloat()

        canvas.drawText(display.number, centerX, numY, numPaint)
        canvas.drawText(display.unit, centerX, unitY, unitPaint)
    }

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
