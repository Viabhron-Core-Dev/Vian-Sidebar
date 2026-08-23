package com.example.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import java.util.Locale

object DynamicSpeedIconGenerator {

    data class SpeedDisplay(val number: String, val unit: String)

    private var cachedBitmap: Bitmap? = null
    private var cachedCanvas: Canvas? = null
    private var cachedDensityDpi: Int = -1
    private var cachedSizePx: Int = -1
    private var cachedTypeface: Typeface? = null
    private var cachedNumPaint: Paint? = null
    private var cachedUnitPaint: Paint? = null

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
                // Auto formatting matching NetSpeed Indicator
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
     * Generates a crisp, pixel-perfect status bar icon bitmap directly at the notification small-icon dimensions.
     * Positions text baselines precisely using Paint.FontMetrics without unnecessary scaling/filtering.
     */
    fun generateStatusBarBitmap(context: Context, bytesPerSec: Long, forcedUnit: String? = null): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val densityDpi = displayMetrics.densityDpi

        // Determine exact small icon dimension for the current display density (24dp standard)
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

        val numberText = display.number
        val unitText = display.unit

        if (cachedTypeface == null) {
            cachedTypeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        }

        // Use anti-aliasing and subpixel text for sharp glyph rendering; omit bitmap filter flags
        val numPaint = cachedNumPaint ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.WHITE
            typeface = cachedTypeface
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }.also { cachedNumPaint = it }

        val unitPaint = cachedUnitPaint ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.WHITE
            typeface = cachedTypeface
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }.also { cachedUnitPaint = it }

        // Partition vertical space into top section (number) and bottom section (unit)
        val numberSectionHeight = Math.round(sizePx * 0.62f).toFloat()
        val unitSectionHeight = sizePx - numberSectionHeight

        // Fit number text within upper bounds
        val maxNumH = numberSectionHeight * 0.95f
        val maxNumW = sizePx * 0.96f
        numPaint.textSize = maxNumH
        val numMetricsInit = numPaint.fontMetrics
        val numFontH = numMetricsInit.descent - numMetricsInit.ascent
        val numTextW = numPaint.measureText(numberText)
        val scaleNumW = if (numTextW > 0f) maxNumW / numTextW else 1f
        val scaleNumH = if (numFontH > 0f) maxNumH / numFontH else 1f
        val scaleNum = minOf(scaleNumW, scaleNumH, 1.0f)
        numPaint.textSize = maxNumH * scaleNum

        // Calculate exact pixel-aligned baseline for the number
        val finalNumMetrics = numPaint.fontMetrics
        val numCenterY = numberSectionHeight / 2f
        val numBaselineY = Math.round(numCenterY - (finalNumMetrics.ascent + finalNumMetrics.descent) / 2f).toFloat()

        // Fit unit text within lower bounds
        val maxUnitH = unitSectionHeight * 0.90f
        val maxUnitW = sizePx * 0.96f
        unitPaint.textSize = maxUnitH
        val unitMetricsInit = unitPaint.fontMetrics
        val unitFontH = unitMetricsInit.descent - unitMetricsInit.ascent
        val unitTextW = unitPaint.measureText(unitText)
        val scaleUnitW = if (unitTextW > 0f) maxUnitW / unitTextW else 1f
        val scaleUnitH = if (unitFontH > 0f) maxUnitH / unitFontH else 1f
        val scaleUnit = minOf(scaleUnitW, scaleUnitH, 1.0f)
        unitPaint.textSize = maxUnitH * scaleUnit

        // Calculate exact pixel-aligned baseline for the unit
        val finalUnitMetrics = unitPaint.fontMetrics
        val unitCenterY = numberSectionHeight + (unitSectionHeight / 2f)
        val unitBaselineY = Math.round(unitCenterY - (finalUnitMetrics.ascent + finalUnitMetrics.descent) / 2f).toFloat()

        // Draw centered horizontally with integer pixel alignment
        val centerX = Math.round(sizePx / 2f).toFloat()

        canvas!!.drawText(numberText, centerX, numBaselineY, numPaint)
        canvas.drawText(unitText, centerX, unitBaselineY, unitPaint)

        return bitmap
    }

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
