package com.example.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import java.util.Locale

object DynamicSpeedIconGenerator {

    // Status bar icon canvas size (96x96 px transparent bitmap)
    private const val STATUS_BAR_SIZE = 96

    // NetSpeed Indicator typography: sans-serif-condensed BOLD with fake bold for thick stems
    private val boldCondensedTypeface: Typeface =
        Typeface.create("sans-serif-condensed", Typeface.BOLD)

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        typeface = boldCondensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        typeface = boldCondensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

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
     * Generates a crystal clear, transparent-background status bar icon bitmap matching NetSpeed Indicator.
     */
    fun generateStatusBarBitmap(bytesPerSec: Long, forcedUnit: String? = null): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        val size = STATUS_BAR_SIZE
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)

        val numberText = display.number
        val unitText = display.unit

        // Sizing strictly optimized to maximize legibility and stroke density
        val numSize = when (numberText.length) {
            1 -> 58f
            2 -> 54f
            3 -> 46f
            4 -> 38f
            else -> 32f
        }
        numberPaint.textSize = numSize

        val unitSize = when (unitText.length) {
            3 -> 30f   // "B/s"
            4 -> 28f   // "KB/s", "MB/s"
            else -> 24f
        }
        unitPaint.textSize = unitSize

        // NetSpeed Indicator vertical baseline alignment (upper 54% / lower 88%)
        val numY = size * 0.54f
        val unitY = size * 0.88f

        canvas.drawText(numberText, size / 2f, numY, numberPaint)
        canvas.drawText(unitText, size / 2f, unitY, unitPaint)

        return bitmap
    }

    fun generateIconCompat(bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
