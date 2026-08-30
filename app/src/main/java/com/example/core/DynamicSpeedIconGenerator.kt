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

object DynamicSpeedIconGenerator {

    data class SpeedDisplay(val number: String, val unit: String)

    data class IconConfig(
        val font: String = "sans-serif-condensed",
        val isFakeBold: Boolean = true,
        val numScale: Float = 1.0f,
        val unitScale: Float = 1.0f,
        val letterSpacing: Float = -0.04f
    )

    private var activeConfig = IconConfig()

    private var cachedBitmap: Bitmap? = null
    private var cachedCanvas: Canvas? = null
    private var cachedDensityDpi: Int = -1
    private var cachedSizePx: Int = -1
    private var cachedTypeface: Typeface? = null
    private var cachedNumPaint: Paint? = null
    private var cachedUnitPaint: Paint? = null

    fun loadConfig(prefs: SharedPreferences): IconConfig {
        activeConfig = IconConfig()
        invalidatePaints()
        return activeConfig
    }

    fun updateActiveConfig(config: IconConfig) {
        activeConfig = config
        invalidatePaints()
    }

    private fun invalidatePaints() {
        cachedTypeface = null
        cachedNumPaint = null
        cachedUnitPaint = null
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

    fun getNotificationIconSize(context: Context): Int {
        val res = context.resources
        val resId = res.getIdentifier("status_bar_icon_size", "dimen", "android")
        if (resId > 0) {
            try {
                val size = res.getDimensionPixelSize(resId)
                if (size in 24..128) return size
            } catch (e: Exception) {}
        }
        val density = res.displayMetrics.density
        return Math.round(24f * density).coerceAtLeast(24)
    }

    private var tickCounter = 0

    fun generateStatusBarBitmap(
        context: Context,
        bytesPerSec: Long,
        forcedUnit: String? = null,
        overrideConfig: IconConfig? = null
    ): Bitmap {
        val startTime = System.nanoTime()
        val config = overrideConfig ?: activeConfig
        val display = formatSpeed(bytesPerSec, forcedUnit)
        
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val densityDpi = displayMetrics.densityDpi

        val sizePx = getNotificationIconSize(context)

        if (tickCounter == 0) {
            SpeedIconDiagnostics.captureInitialMetrics(context, sizePx)
        }

        val isLive = (overrideConfig == null)
        var bitmap = if (isLive) cachedBitmap else null
        var canvas = if (isLive) cachedCanvas else null
        var eraseCalled = false

        if (isLive) {
            if (bitmap == null || bitmap.isRecycled || cachedSizePx != sizePx || cachedDensityDpi != densityDpi) {
                bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
                    this.density = densityDpi
                }
                canvas = Canvas(bitmap)
                cachedBitmap = bitmap
                cachedCanvas = canvas
                cachedSizePx = sizePx
                cachedDensityDpi = densityDpi
            }
            bitmap.eraseColor(Color.TRANSPARENT)
            eraseCalled = true
        } else {
            bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
                this.density = densityDpi
            }
            canvas = Canvas(bitmap)
            bitmap.eraseColor(Color.TRANSPARENT)
            eraseCalled = true
        }

        val (numBounds, unitBounds, numPaint, unitPaint) = renderIconToCanvas(canvas!!, sizePx, display, config)

        val durationUs = (System.nanoTime() - startTime) / 1000
        if (isLive) {
            SpeedIconDiagnostics.recordTick(
                tickIndex = tickCounter++,
                bytesPerSec = bytesPerSec,
                formattedNumber = display.number,
                formattedUnit = display.unit,
                bitmap = bitmap,
                numPaint = numPaint,
                unitPaint = unitPaint,
                numBounds = numBounds,
                unitBounds = unitBounds,
                eraseColorCalled = eraseCalled,
                renderDurationUs = durationUs
            )
        }

        return bitmap
    }

    fun renderIconToCanvas(
        canvas: Canvas,
        sizePx: Int,
        display: SpeedDisplay,
        config: IconConfig
    ): Quadruple<Rect, Rect, Paint, Paint> {
        val tf = cachedTypeface ?: try {
            if (config.font.isNotEmpty()) {
                Typeface.create(config.font, if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
            } else {
                Typeface.create("sans-serif-condensed", if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
            }
        } catch (e: Exception) {
            if (config.isFakeBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }.also { if (config == activeConfig) cachedTypeface = it }

        val numPaint = (if (config == activeConfig) cachedNumPaint else null) ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
            isFilterBitmap = true
            isDither = false
            hinting = Paint.HINTING_ON
            isSubpixelText = true
            style = Paint.Style.FILL
            letterSpacing = config.letterSpacing
        }.also { if (config == activeConfig) cachedNumPaint = it }

        numPaint.letterSpacing = config.letterSpacing
        numPaint.isFakeBoldText = config.isFakeBold
        numPaint.isSubpixelText = true
        numPaint.hinting = Paint.HINTING_ON

        val unitPaint = (if (config == activeConfig) cachedUnitPaint else null) ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
            isFilterBitmap = true
            isDither = false
            hinting = Paint.HINTING_ON
            isSubpixelText = true
            style = Paint.Style.FILL
            letterSpacing = config.letterSpacing
        }.also { if (config == activeConfig) cachedUnitPaint = it }

        unitPaint.letterSpacing = config.letterSpacing
        unitPaint.isFakeBoldText = config.isFakeBold
        unitPaint.isSubpixelText = true
        unitPaint.hinting = Paint.HINTING_ON

        val centerX = (sizePx / 2f)

        // Top Slot: Number text (Takes top ~62% of icon height, maximizing legibility with tight bounds)
        val maxNumW = (sizePx - 2f).coerceAtLeast(10f)
        val maxNumH = sizePx * 0.62f * config.numScale
        numPaint.textSize = maxNumH
        val numTextW = numPaint.measureText(display.number)
        val scaleNum = minOf(if (numTextW > 0f) maxNumW / numTextW else 1f, 1.0f)
        numPaint.textSize = (maxNumH * scaleNum).coerceAtLeast(8f)

        val numBounds = Rect()
        numPaint.getTextBounds(display.number, 0, display.number.length, numBounds)
        val numCenterY = (sizePx * 0.34f)
        val numBaseline = numCenterY + (numBounds.height() / 2f) - numBounds.bottom

        // Bottom Slot: Unit text (kB/s, MB/s)
        val maxUnitW = (sizePx - 2f).coerceAtLeast(10f)
        val maxUnitH = sizePx * 0.34f * config.unitScale
        unitPaint.textSize = maxUnitH
        val unitTextW = unitPaint.measureText(display.unit)
        val scaleUnit = minOf(if (unitTextW > 0f) maxUnitW / unitTextW else 1f, 1.0f)
        unitPaint.textSize = (maxUnitH * scaleUnit).coerceAtLeast(6f)

        val unitBounds = Rect()
        unitPaint.getTextBounds(display.unit, 0, display.unit.length, unitBounds)
        val unitCenterY = (sizePx * 0.81f)
        val unitBaseline = unitCenterY + (unitBounds.height() / 2f) - unitBounds.bottom

        canvas.drawText(display.number, centerX, numBaseline, numPaint)
        canvas.drawText(display.unit, centerX, unitBaseline, unitPaint)

        return Quadruple(numBounds, unitBounds, numPaint, unitPaint)
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}
