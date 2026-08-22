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
    // Notification large icon circle badge size (128x128 px)
    private const val NOTIF_LARGE_ICON_SIZE = 128

    // Crisp condensed bold typeface for numbers and units
    private val boldCondensedTypeface: Typeface =
        Typeface.create("sans-serif-condensed", Typeface.BOLD)

    private val statusNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        typeface = boldCondensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val statusUnitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        typeface = boldCondensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // Paints for Notification Large Icon Circle Badge (like the NetSpeed Indicator screenshot)
    private val circleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = 0xFFB0C97F.toInt() // Crisp olive green circle badge
        style = Paint.Style.FILL
    }

    private val darkNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = 0xFF1C2D00.toInt()
        typeface = boldCondensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val darkUnitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = 0xFF1C2D00.toInt()
        typeface = boldCondensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    data class SpeedDisplay(val number: String, val unit: String)

    fun formatSpeed(bytesPerSec: Long, forcedUnit: String? = null): SpeedDisplay {
        if (bytesPerSec <= 0) {
            return SpeedDisplay("0", if (forcedUnit == "MB/s") "MB/s" else "kB/s")
        }

        return when (forcedUnit) {
            "KB/s" -> {
                val kb = (bytesPerSec + 512) / 1024
                SpeedDisplay(kb.toString(), "kB/s")
            }
            "MB/s" -> {
                val mb = bytesPerSec / (1024.0 * 1024.0)
                val str = String.format(Locale.US, "%.1f", mb)
                SpeedDisplay(if (str.endsWith(".0")) str.substringBefore(".0") else str, "MB/s")
            }
            else -> {
                // Auto formatting: matches standard NetSpeed Indicator format
                when {
                    bytesPerSec < 1000 -> {
                        SpeedDisplay("0", "kB/s")
                    }
                    bytesPerSec < 1000 * 1024 -> {
                        val kb = (bytesPerSec + 512) / 1024
                        SpeedDisplay(kb.toString(), "kB/s")
                    }
                    bytesPerSec < 100L * 1024 * 1024 -> {
                        val mb = bytesPerSec / (1024.0 * 1024.0)
                        if (mb < 10.0) {
                            val str = String.format(Locale.US, "%.1f", mb)
                            SpeedDisplay(if (str.endsWith(".0")) str.substringBefore(".0") else str, "MB/s")
                        } else {
                            SpeedDisplay(mb.toInt().toString(), "MB/s")
                        }
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
     * Generates a crystal clear, transparent-background status bar icon bitmap.
     * Android status bar renders only the alpha mask / monochrome content.
     */
    fun generateStatusBarBitmap(bytesPerSec: Long, forcedUnit: String? = null): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        val bitmap = Bitmap.createBitmap(STATUS_BAR_SIZE, STATUS_BAR_SIZE, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)

        val numberText = display.number
        val unitText = display.unit

        // Sizing dynamically scaled to be crisp, bold and prevent cropping
        val numSize = when (numberText.length) {
            1 -> 58f
            2 -> 54f
            3 -> 48f
            else -> 40f
        }
        statusNumberPaint.textSize = numSize

        val unitSize = when (unitText.length) {
            3 -> 32f   // "B/s"
            4 -> 28f   // "kB/s", "MB/s"
            else -> 24f
        }
        statusUnitPaint.textSize = unitSize

        // Vertical positioning: baseline alignment for dual-line text in status bar slot
        val numY = STATUS_BAR_SIZE * 0.52f
        val unitY = STATUS_BAR_SIZE * 0.88f

        canvas.drawText(numberText, STATUS_BAR_SIZE / 2f, numY, statusNumberPaint)
        canvas.drawText(unitText, STATUS_BAR_SIZE / 2f, unitY, statusUnitPaint)

        return bitmap
    }

    /**
     * Generates a circular badge bitmap with green background (identical to NetSpeed Indicator notification icon).
     */
    fun generateLargeCircleBitmap(bytesPerSec: Long, forcedUnit: String? = null): Bitmap {
        val display = formatSpeed(bytesPerSec, forcedUnit)
        val bitmap = Bitmap.createBitmap(NOTIF_LARGE_ICON_SIZE, NOTIF_LARGE_ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val size = NOTIF_LARGE_ICON_SIZE.toFloat()
        // Draw solid anti-aliased circle
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, circleBgPaint)

        val numberText = display.number
        val unitText = display.unit

        val numSize = when (numberText.length) {
            1 -> 68f
            2 -> 62f
            3 -> 54f
            else -> 46f
        }
        darkNumberPaint.textSize = numSize

        val unitSize = when (unitText.length) {
            3 -> 36f
            4 -> 32f
            else -> 26f
        }
        darkUnitPaint.textSize = unitSize

        val numY = size * 0.52f
        val unitY = size * 0.86f

        canvas.drawText(numberText, size / 2f, numY, darkNumberPaint)
        canvas.drawText(unitText, size / 2f, unitY, darkUnitPaint)

        return bitmap
    }

    fun generateIconCompat(bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
