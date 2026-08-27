package com.example.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat

/**
 * Dynamic Status Bar Text & Battery/Metric Icon Generator
 *
 * Adapted from the dynamic status bar rendering patterns in Battery-Indicator-Pro (BatteryBot).
 * Dynamically draws numeric levels, percentages, temperatures, or custom glyphs onto
 * an in-memory Canvas Bitmap to serve as an IconCompat / Notification small icon.
 */
object DynamicStatusIconHelper {

    enum class IconStyle {
        CLASSIC_TEXT_ONLY,
        TEXT_WITH_BORDER,
        CIRCULAR_FILL,
        BATTERY_SHAPE
    }

    fun createDynamicIcon(
        text: String,
        textColor: Int = Color.WHITE,
        bgColor: Int? = null,
        style: IconStyle = IconStyle.CLASSIC_TEXT_ONLY,
        sizePx: Int = 72,
        bold: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val half = sizePx / 2f
        val padding = sizePx * 0.05f

        when (style) {
            IconStyle.CIRCULAR_FILL -> {
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgColor ?: Color.argb(180, 40, 40, 40)
                    this.style = Paint.Style.FILL
                }
                canvas.drawCircle(half, half, half - padding, fillPaint)
            }
            IconStyle.TEXT_WITH_BORDER -> {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    this.style = Paint.Style.STROKE
                    strokeWidth = sizePx * 0.05f
                }
                canvas.drawCircle(half, half, half - padding - (borderPaint.strokeWidth / 2f), borderPaint)
            }
            IconStyle.BATTERY_SHAPE -> {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    this.style = Paint.Style.STROKE
                    strokeWidth = sizePx * 0.05f
                }
                val rect = RectF(padding, padding + sizePx * 0.08f, sizePx - padding, sizePx - padding)
                canvas.drawRoundRect(rect, sizePx * 0.15f, sizePx * 0.15f, strokePaint)
            }
            IconStyle.CLASSIC_TEXT_ONLY -> {
                // Transparent background
            }
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
            textSize = when {
                text.length >= 4 -> sizePx * 0.36f
                text.length == 3 -> sizePx * 0.44f
                text.length == 2 -> sizePx * 0.58f
                else -> sizePx * 0.68f
            }
        }

        val fm = textPaint.fontMetrics
        val yOffset = half - ((fm.ascent + fm.descent) / 2f)
        canvas.drawText(text, half, yOffset, textPaint)

        return bitmap
    }

    fun createIconCompat(
        text: String,
        textColor: Int = Color.WHITE,
        bgColor: Int? = null,
        style: IconStyle = IconStyle.CLASSIC_TEXT_ONLY,
        sizePx: Int = 72
    ): IconCompat {
        val bmp = createDynamicIcon(text, textColor, bgColor, style, sizePx)
        return IconCompat.createWithBitmap(bmp)
    }

    fun getBatteryColor(percent: Int, isCharging: Boolean = false): Int {
        return when {
            isCharging -> Color.rgb(66, 165, 245)
            percent > 20 -> Color.rgb(102, 187, 106)
            percent > 10 -> Color.rgb(255, 167, 38)
            else -> Color.rgb(239, 83, 80)
        }
    }

    data class SpeedDisplay(val number: String, val unit: String)

    fun formatSpeed(bytesPerSec: Long, forcedUnit: String? = null): SpeedDisplay {
        if (bytesPerSec <= 0) {
            return SpeedDisplay("0", if (forcedUnit == "MB/s") "MB/s" else "kB/s")
        }

        val kbps = bytesPerSec / 1024.0
        val mbps = kbps / 1024.0

        return when (forcedUnit) {
            "KB/s", "kB/s" -> {
                val kb = ((bytesPerSec + 512) / 1024).toInt()
                SpeedDisplay(kb.toString(), "kB/s")
            }
            "MB/s" -> {
                val str = if (mbps < 10.0) String.format(java.util.Locale.US, "%.1f", mbps) else String.format(java.util.Locale.US, "%.0f", mbps)
                SpeedDisplay(str, "MB/s")
            }
            else -> {
                when {
                    bytesPerSec < 1000 -> {
                        SpeedDisplay("0", "kB/s")
                    }
                    bytesPerSec < 1000 * 1024 -> {
                        val kb = ((bytesPerSec + 512) / 1024).toInt()
                        SpeedDisplay(kb.toString(), "kB/s")
                    }
                    bytesPerSec < 100L * 1024 * 1024 -> {
                        val str = if (mbps < 10.0) String.format(java.util.Locale.US, "%.1f", mbps) else mbps.toInt().toString()
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
     * Generates a 70/30 split dynamic status bar icon for Internet Speed:
     * - Top 70%: Speed number (prominent, bold, auto-scaled to prevent clipping)
     * - Bottom 30%: Speed unit (kB/s, MB/s)
     */
    fun createSpeedIcon(
        speedValue: String,
        speedUnit: String,
        textColor: Int = Color.WHITE,
        sizePx: Int = 96
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerX = sizePx / 2f

        // 1. Top Section (70% height) -> Speed Number
        val topAreaHeight = sizePx * 0.70f
        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            val len = speedValue.length
            textSize = when {
                len >= 4 -> topAreaHeight * 0.58f
                len == 3 -> topAreaHeight * 0.72f
                len == 2 -> topAreaHeight * 0.88f
                else -> topAreaHeight * 0.96f
            }
        }
        val numFm = numberPaint.fontMetrics
        val numY = (topAreaHeight / 2f) - ((numFm.ascent + numFm.descent) / 2f)
        canvas.drawText(speedValue, centerX, numY, numberPaint)

        // 2. Bottom Section (30% height) -> Speed Unit (e.g. kB/s or MB/s)
        val bottomAreaHeight = sizePx * 0.30f
        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textSize = bottomAreaHeight * 0.75f
        }
        val unitFm = unitPaint.fontMetrics
        val unitCenterY = topAreaHeight + (bottomAreaHeight / 2f)
        val unitY = unitCenterY - ((unitFm.ascent + unitFm.descent) / 2f)
        canvas.drawText(speedUnit, centerX, unitY, unitPaint)

        return bitmap
    }

    fun createSpeedIconCompat(
        bytesPerSec: Long,
        forcedUnit: String? = null,
        textColor: Int = Color.WHITE,
        sizePx: Int = 96
    ): IconCompat {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        val bmp = createSpeedIcon(display.number, display.unit, textColor, sizePx)
        return IconCompat.createWithBitmap(bmp)
    }
}
