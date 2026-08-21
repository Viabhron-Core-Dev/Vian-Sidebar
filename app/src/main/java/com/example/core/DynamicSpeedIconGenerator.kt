package com.example.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.DisplayMetrics
import androidx.core.graphics.drawable.IconCompat
import java.util.Locale

object DynamicSpeedIconGenerator {

    private const val BITMAP_SIZE = 96

    private val condensedTypeface: Typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        typeface = condensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        typeface = condensedTypeface
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
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

    fun generateBitmap(bytesPerSec: Long): Bitmap {
        val display = formatSpeed(bytesPerSec)
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        bitmap.density = DisplayMetrics.DENSITY_XXHIGH
        val canvas = Canvas(bitmap)

        val numberText = display.number
        val unitText = display.unit

        // Maximized font sizing to fill the status bar slot without clipping
        val numSize = when (numberText.length) {
            1 -> 56f
            2 -> 52f
            3 -> 48f
            else -> 40f
        }
        numberPaint.textSize = numSize

        val unitSize = when (unitText.length) {
            3 -> 30f   // "B/s"
            4 -> 28f   // "kB/s", "MB/s"
            else -> 22f
        }
        unitPaint.textSize = unitSize

        // Optimal baseline alignment filling ~90% of vertical status icon height
        val numY = BITMAP_SIZE * 0.52f
        val unitY = BITMAP_SIZE * 0.88f

        canvas.drawText(numberText, BITMAP_SIZE / 2f, numY, numberPaint)
        canvas.drawText(unitText, BITMAP_SIZE / 2f, unitY, unitPaint)

        return bitmap
    }

    fun generateIconCompat(bytesPerSec: Long): IconCompat {
        val bitmap = generateBitmap(bytesPerSec)
        return IconCompat.createWithBitmap(bitmap)
    }
}

