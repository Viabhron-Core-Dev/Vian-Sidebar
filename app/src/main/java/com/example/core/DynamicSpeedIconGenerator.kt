package com.example.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.drawable.IconCompat
import java.util.Locale

object DynamicSpeedIconGenerator {

    private const val BITMAP_SIZE = 96

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    data class SpeedDisplay(val number: String, val unit: String)

    fun formatSpeed(bytesPerSec: Long): SpeedDisplay {
        return when {
            bytesPerSec <= 0 -> {
                SpeedDisplay("0", "kB/s")
            }
            bytesPerSec < 1024 -> {
                SpeedDisplay(bytesPerSec.toString(), "B/s")
            }
            bytesPerSec < 1000 * 1024 -> {
                val kb = bytesPerSec / 1024
                SpeedDisplay(kb.toString(), "kB/s")
            }
            bytesPerSec < 100L * 1024 * 1024 -> {
                val mb = bytesPerSec / (1024.0 * 1024.0)
                val str = String.format(Locale.US, "%.1f", mb)
                SpeedDisplay(if (str.endsWith(".0")) str.substringBefore(".0") else str, "MB/s")
            }
            else -> {
                val mb = (bytesPerSec / (1024 * 1024)).toInt()
                SpeedDisplay(mb.toString(), "MB/s")
            }
        }
    }

    fun generateBitmap(bytesPerSec: Long): Bitmap {
        val display = formatSpeed(bytesPerSec)
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Dynamic font size based on number length
        val numberText = display.number
        val unitText = display.unit

        val numSize = when (numberText.length) {
            1 -> 52f
            2 -> 48f
            3 -> 42f
            else -> 36f
        }
        numberPaint.textSize = numSize

        val unitSize = when (unitText.length) {
            3 -> 32f  // "B/s"
            4 -> 28f  // "kB/s", "MB/s"
            else -> 24f // "GiB/s" etc.
        }
        unitPaint.textSize = unitSize

        // Measure bounds for vertical centering in respective halves
        val numBounds = Rect()
        numberPaint.getTextBounds(numberText, 0, numberText.length, numBounds)
        val topHalfCenterY = BITMAP_SIZE * 0.28f
        val numY = topHalfCenterY + (numBounds.height() / 2f)

        val unitBounds = Rect()
        unitPaint.getTextBounds(unitText, 0, unitText.length, unitBounds)
        val bottomHalfCenterY = BITMAP_SIZE * 0.78f
        val unitY = bottomHalfCenterY + (unitBounds.height() / 2f)

        canvas.drawText(numberText, BITMAP_SIZE / 2f, numY, numberPaint)
        canvas.drawText(unitText, BITMAP_SIZE / 2f, unitY, unitPaint)

        return bitmap
    }

    fun generateIconCompat(bytesPerSec: Long): IconCompat {
        val bitmap = generateBitmap(bytesPerSec)
        return IconCompat.createWithBitmap(bitmap)
    }
}
