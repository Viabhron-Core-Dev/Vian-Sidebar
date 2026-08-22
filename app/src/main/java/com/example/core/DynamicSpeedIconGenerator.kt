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
     * Generates a pixel-perfect, crisp, transparent-background status bar icon bitmap matching NetSpeed Indicator.
     * Uses device screen density or fallback to ensure 0 blurriness and exact alignment.
     */
    fun generateStatusBarBitmap(context: Context, bytesPerSec: Long, forcedUnit: String? = null): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        
        // Exact 24dp standard status bar icon size calculated per device density
        val density = context.resources.displayMetrics.density
        val sizePx = (24 * density).toInt().coerceAtLeast(48) // e.g. 72px (xhdpi), 96px (xxhdpi), etc.

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        bitmap.density = context.resources.displayMetrics.densityDpi
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)

        val numberText = display.number
        val unitText = display.unit

        val boldCondensedTypeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            color = Color.WHITE
            typeface = boldCondensedTypeface
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            color = Color.WHITE
            typeface = boldCondensedTypeface
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        // Dynamically size fonts to fill maximum vertical and horizontal space without clipping
        // Speed number takes upper ~62% of height, Unit text takes lower ~35%
        val maxNumHeight = sizePx * 0.60f
        val maxUnitHeight = sizePx * 0.34f

        var numTextSize = when (numberText.length) {
            1 -> sizePx * 0.65f
            2 -> sizePx * 0.60f
            3 -> sizePx * 0.50f
            4 -> sizePx * 0.42f
            else -> sizePx * 0.36f
        }
        numPaint.textSize = numTextSize

        val numBounds = Rect()
        numPaint.getTextBounds(numberText, 0, numberText.length, numBounds)
        // Ensure number width does not exceed canvas
        if (numBounds.width() > sizePx * 0.96f) {
            numPaint.textSize = numTextSize * (sizePx * 0.96f / numBounds.width())
            numPaint.getTextBounds(numberText, 0, numberText.length, numBounds)
        }

        var unitTextSize = when (unitText.length) {
            3 -> sizePx * 0.32f // "B/s"
            4 -> sizePx * 0.30f // "KB/s", "MB/s"
            else -> sizePx * 0.26f
        }
        unitPaint.textSize = unitTextSize

        val unitBounds = Rect()
        unitPaint.getTextBounds(unitText, 0, unitText.length, unitBounds)
        if (unitBounds.width() > sizePx * 0.96f) {
            unitPaint.textSize = unitTextSize * (sizePx * 0.96f / unitBounds.width())
            unitPaint.getTextBounds(unitText, 0, unitText.length, unitBounds)
        }

        // Align baseline accurately: top number vertically centered in upper area, unit in bottom area
        val centerX = sizePx / 2f
        val numY = sizePx * 0.56f
        val unitY = sizePx * 0.93f

        canvas.drawText(numberText, centerX, numY, numPaint)
        canvas.drawText(unitText, centerX, unitY, unitPaint)

        return bitmap
    }

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
