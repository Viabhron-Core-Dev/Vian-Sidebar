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
        val numYOffsetDp: Float = 0f,
        val unitYOffsetDp: Float = 0f,
        val bgShape: String = "None", // "None", "Rounded", "Pill", "Square"
        val bgRadiusDp: Float = 4f,
        val bgAlpha: Int = 0, // 0 to 255
        val layoutMode: String = "Stacked" // "Stacked", "Compact", "NumberOnly"
    )

    private var activeConfig = IconConfig()

    private var cachedBitmap: Bitmap? = null
    private var cachedCanvas: Canvas? = null
    private var cachedDensityDpi: Int = -1
    private var cachedSizePx: Int = -1
    private var cachedTypeface: Typeface? = null
    private var cachedNumPaint: Paint? = null
    private var cachedUnitPaint: Paint? = null
    private var cachedBgPaint: Paint? = null

    fun loadConfig(prefs: SharedPreferences): IconConfig {
        val config = IconConfig(
            font = prefs.getString("speed_icon_font", "sans-serif-condensed") ?: "sans-serif-condensed",
            isFakeBold = prefs.getBoolean("speed_icon_bold", true),
            numScale = prefs.getFloat("speed_icon_num_scale", 1.0f),
            unitScale = prefs.getFloat("speed_icon_unit_scale", 1.0f),
            numYOffsetDp = prefs.getFloat("speed_icon_num_y_offset", 0f),
            unitYOffsetDp = prefs.getFloat("speed_icon_unit_y_offset", 0f),
            bgShape = prefs.getString("speed_icon_bg_shape", "None") ?: "None",
            bgRadiusDp = prefs.getFloat("speed_icon_bg_radius", 4f),
            bgAlpha = prefs.getInt("speed_icon_bg_alpha", 0),
            layoutMode = prefs.getString("speed_icon_layout", "Stacked") ?: "Stacked"
        )
        activeConfig = config
        invalidatePaints()
        return config
    }

    fun updateActiveConfig(config: IconConfig) {
        activeConfig = config
        invalidatePaints()
    }

    private fun invalidatePaints() {
        cachedTypeface = null
        cachedNumPaint = null
        cachedUnitPaint = null
        cachedBgPaint = null
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
    fun generateStatusBarBitmap(
        context: Context,
        bytesPerSec: Long,
        forcedUnit: String? = null,
        overrideConfig: IconConfig? = null
    ): Bitmap {
        val config = overrideConfig ?: activeConfig
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

        val isLive = (overrideConfig == null)
        var bitmap = if (isLive) cachedBitmap else null
        var canvas = if (isLive) cachedCanvas else null

        if (isLive) {
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
        } else {
            bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            bitmap.density = densityDpi
            canvas = Canvas(bitmap)
        }

        renderIconToCanvas(canvas!!, sizePx, density, display, config)

        return bitmap
    }

    fun renderIconToCanvas(
        canvas: Canvas,
        sizePx: Int,
        density: Float,
        display: SpeedDisplay,
        config: IconConfig
    ) {
        // Draw background if configured
        if (config.bgAlpha > 0 && config.bgShape != "None") {
            val bgPaint = cachedBgPaint ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }.also { if (config == activeConfig) cachedBgPaint = it }
            bgPaint.color = Color.argb(config.bgAlpha, 0, 0, 0)
            
            val pad = 1f * density
            val left = pad
            val top = pad
            val right = sizePx - pad
            val bottom = sizePx - pad
            val rad = when (config.bgShape) {
                "Pill" -> sizePx / 2f
                "Square" -> 0f
                else -> config.bgRadiusDp * density
            }
            canvas.drawRoundRect(left, top, right, bottom, rad, rad, bgPaint)
        }

        val tf = cachedTypeface ?: try {
            Typeface.create(config.font, if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
        } catch (e: Exception) {
            Typeface.create(Typeface.DEFAULT, if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
        }.also { if (config == activeConfig) cachedTypeface = it }

        val numPaint = (if (config == activeConfig) cachedNumPaint else null) ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
        }.also { if (config == activeConfig) cachedNumPaint = it }

        val unitPaint = (if (config == activeConfig) cachedUnitPaint else null) ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
        }.also { if (config == activeConfig) cachedUnitPaint = it }

        val centerX = Math.round(sizePx / 2f).toFloat()

        when (config.layoutMode) {
            "Compact" -> {
                // Compact: e.g. "45K" or "1.2M" on a single centered line
                val shortUnit = if (display.unit.startsWith("M")) "M" else "K"
                val compactText = "${display.number}$shortUnit"
                val maxH = sizePx * 0.75f * config.numScale
                val maxW = sizePx * 0.94f
                numPaint.textSize = maxH
                val metrics = numPaint.fontMetrics
                val textW = numPaint.measureText(compactText)
                val scale = minOf(if (textW > 0f) maxW / textW else 1f, 1.0f)
                numPaint.textSize = maxH * scale

                val finalMetrics = numPaint.fontMetrics
                val centerY = (sizePx / 2f) + (config.numYOffsetDp * density)
                val baselineY = Math.round(centerY - (finalMetrics.ascent + finalMetrics.descent) / 2f).toFloat()
                canvas.drawText(compactText, centerX, baselineY, numPaint)
            }
            "NumberOnly" -> {
                // Number only centered across full height
                val maxH = sizePx * 0.85f * config.numScale
                val maxW = sizePx * 0.94f
                numPaint.textSize = maxH
                val textW = numPaint.measureText(display.number)
                val scale = minOf(if (textW > 0f) maxW / textW else 1f, 1.0f)
                numPaint.textSize = maxH * scale

                val finalMetrics = numPaint.fontMetrics
                val centerY = (sizePx / 2f) + (config.numYOffsetDp * density)
                val baselineY = Math.round(centerY - (finalMetrics.ascent + finalMetrics.descent) / 2f).toFloat()
                canvas.drawText(display.number, centerX, baselineY, numPaint)
            }
            else -> {
                // Stacked (Default)
                val numberSectionHeight = Math.round(sizePx * 0.62f).toFloat()
                val unitSectionHeight = sizePx - numberSectionHeight

                // Number
                val maxNumH = numberSectionHeight * 0.95f * config.numScale
                val maxNumW = sizePx * 0.96f
                numPaint.textSize = maxNumH
                val numMetricsInit = numPaint.fontMetrics
                val numFontH = numMetricsInit.descent - numMetricsInit.ascent
                val numTextW = numPaint.measureText(display.number)
                val scaleNumW = if (numTextW > 0f) maxNumW / numTextW else 1f
                val scaleNumH = if (numFontH > 0f) maxNumH / numFontH else 1f
                val scaleNum = minOf(scaleNumW, scaleNumH, 1.0f)
                numPaint.textSize = maxNumH * scaleNum

                val finalNumMetrics = numPaint.fontMetrics
                val numCenterY = (numberSectionHeight / 2f) + (config.numYOffsetDp * density)
                val numBaselineY = Math.round(numCenterY - (finalNumMetrics.ascent + finalNumMetrics.descent) / 2f).toFloat()

                // Unit
                val maxUnitH = unitSectionHeight * 0.90f * config.unitScale
                val maxUnitW = sizePx * 0.96f
                unitPaint.textSize = maxUnitH
                val unitMetricsInit = unitPaint.fontMetrics
                val unitFontH = unitMetricsInit.descent - unitMetricsInit.ascent
                val unitTextW = unitPaint.measureText(display.unit)
                val scaleUnitW = if (unitTextW > 0f) maxUnitW / unitTextW else 1f
                val scaleUnitH = if (unitFontH > 0f) maxUnitH / unitFontH else 1f
                val scaleUnit = minOf(scaleUnitW, scaleUnitH, 1.0f)
                unitPaint.textSize = maxUnitH * scaleUnit

                val finalUnitMetrics = unitPaint.fontMetrics
                val unitCenterY = numberSectionHeight + (unitSectionHeight / 2f) + (config.unitYOffsetDp * density)
                val unitBaselineY = Math.round(unitCenterY - (finalUnitMetrics.ascent + finalUnitMetrics.descent) / 2f).toFloat()

                canvas.drawText(display.number, centerX, numBaselineY, numPaint)
                canvas.drawText(display.unit, centerX, unitBaselineY, unitPaint)
            }
        }
    }

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }
}

