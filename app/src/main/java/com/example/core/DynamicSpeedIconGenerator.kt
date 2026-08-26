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
        val layoutMode: String = "Stacked", // "Stacked", "Compact", "NumberOnly"
        // Blurriness reduction & clarity options (Battery-Indicator-Pro style)
        val resScale: Float = 1.0f, // 1.0x exact density avoids bilinear OS resampling blur
        val aaMode: String = "Crisp", // "Crisp" (No anti-alias, 1:1 binary pixels), "Smooth", "HighContrast"
        val letterSpacing: Float = -0.02f, // Slight negative tracking prevents wide stem blur
        val strokeWidthDp: Float = 0f // 0 to 1.5 dp extra stroke sharpness
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
    private var cachedStrokePaint: Paint? = null

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
            layoutMode = prefs.getString("speed_icon_layout", "Stacked") ?: "Stacked",
            resScale = prefs.getFloat("speed_icon_res_scale", 1.0f),
            aaMode = prefs.getString("speed_icon_aa_mode", "Crisp") ?: "Crisp",
            letterSpacing = prefs.getFloat("speed_icon_letter_spacing", -0.02f),
            strokeWidthDp = prefs.getFloat("speed_icon_stroke_width", 0f)
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
        cachedStrokePaint = null
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
        val baseSizePx = if (systemSize > 0) systemSize else Math.round(24f * density).coerceAtLeast(16)
        val sizePx = Math.round(baseSizePx * config.resScale).coerceIn(16, 256)

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

        renderIconToCanvas(canvas!!, sizePx, density * config.resScale, display, config)

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

        val paintFlags = when (config.aaMode) {
            "Crisp" -> Paint.SUBPIXEL_TEXT_FLAG
            "HighContrast" -> Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG
            else -> Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG
        }

        val numPaint = (if (config == activeConfig) cachedNumPaint else null) ?: Paint(paintFlags).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
            letterSpacing = config.letterSpacing
            isDither = false
            isFilterBitmap = false
            if (config.strokeWidthDp > 0f) {
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = config.strokeWidthDp * density
            } else {
                style = Paint.Style.FILL
            }
        }.also { if (config == activeConfig) cachedNumPaint = it }

        // Update dynamic mutable paint properties
        numPaint.letterSpacing = config.letterSpacing
        numPaint.isDither = false
        numPaint.isFilterBitmap = false
        if (config.strokeWidthDp > 0f) {
            numPaint.style = Paint.Style.FILL_AND_STROKE
            numPaint.strokeWidth = config.strokeWidthDp * density
        } else {
            numPaint.style = Paint.Style.FILL
        }

        val unitPaint = (if (config == activeConfig) cachedUnitPaint else null) ?: Paint(paintFlags).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
            letterSpacing = config.letterSpacing
            isDither = false
            isFilterBitmap = false
            if (config.strokeWidthDp > 0f) {
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = (config.strokeWidthDp * 0.75f) * density
            } else {
                style = Paint.Style.FILL
            }
        }.also { if (config == activeConfig) cachedUnitPaint = it }

        unitPaint.letterSpacing = config.letterSpacing
        unitPaint.isDither = false
        unitPaint.isFilterBitmap = false
        if (config.strokeWidthDp > 0f) {
            unitPaint.style = Paint.Style.FILL_AND_STROKE
            unitPaint.strokeWidth = (config.strokeWidthDp * 0.75f) * density
        } else {
            unitPaint.style = Paint.Style.FILL
        }

        // Optional High-Contrast dark shadow/outline behind text
        val outlinePaint = if (config.aaMode == "HighContrast") {
            (if (config == activeConfig) cachedStrokePaint else null) ?: Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                color = Color.argb(180, 0, 0, 0)
                typeface = tf
                textAlign = Paint.Align.CENTER
                style = Paint.Style.STROKE
                strokeWidth = 2f * density
                letterSpacing = config.letterSpacing
                isDither = false
                isFilterBitmap = false
            }.also { if (config == activeConfig) cachedStrokePaint = it }
        } else null

        outlinePaint?.letterSpacing = config.letterSpacing

        val centerX = Math.round(sizePx / 2f).toFloat()
        val textBounds = android.graphics.Rect()

        when (config.layoutMode) {
            "Compact" -> {
                // Battery-Indicator-Pro single-glyph compact style: e.g. "45K" or "1.2M" taking full icon height
                val shortUnit = if (display.unit.startsWith("M")) "M" else "K"
                val compactText = "${display.number}$shortUnit"
                val maxH = sizePx * 0.78f * config.numScale
                val maxW = sizePx * 0.94f
                numPaint.textSize = maxH
                val textW = numPaint.measureText(compactText)
                val scale = minOf(if (textW > 0f) maxW / textW else 1f, 1.0f)
                numPaint.textSize = maxH * scale

                // Battery-Indicator-Pro exact bounds-based vertical centering
                numPaint.getTextBounds(compactText, 0, compactText.length, textBounds)
                val baselineY = Math.round((sizePx / 2f) + (textBounds.height() / 2f) - textBounds.bottom + (config.numYOffsetDp * density)).toFloat()
                
                if (outlinePaint != null) {
                    outlinePaint.textSize = numPaint.textSize
                    canvas.drawText(compactText, centerX, baselineY, outlinePaint)
                }
                canvas.drawText(compactText, centerX, baselineY, numPaint)
            }
            "NumberOnly" -> {
                // Number only centered across full height using exact bounds
                val maxH = sizePx * 0.85f * config.numScale
                val maxW = sizePx * 0.94f
                numPaint.textSize = maxH
                val textW = numPaint.measureText(display.number)
                val scale = minOf(if (textW > 0f) maxW / textW else 1f, 1.0f)
                numPaint.textSize = maxH * scale

                numPaint.getTextBounds(display.number, 0, display.number.length, textBounds)
                val baselineY = Math.round((sizePx / 2f) + (textBounds.height() / 2f) - textBounds.bottom + (config.numYOffsetDp * density)).toFloat()

                if (outlinePaint != null) {
                    outlinePaint.textSize = numPaint.textSize
                    canvas.drawText(display.number, centerX, baselineY, outlinePaint)
                }
                canvas.drawText(display.number, centerX, baselineY, numPaint)
            }
            else -> {
                // Stacked Mode with exact bounds-based integer baseline centering
                val numberSectionHeight = Math.round(sizePx * 0.60f).toFloat()
                val unitSectionHeight = sizePx - numberSectionHeight

                // Number
                val maxNumH = numberSectionHeight * 0.95f * config.numScale
                val maxNumW = sizePx * 0.96f
                numPaint.textSize = maxNumH
                val numTextW = numPaint.measureText(display.number)
                val scaleNum = minOf(if (numTextW > 0f) maxNumW / numTextW else 1f, 1.0f)
                numPaint.textSize = maxNumH * scaleNum

                val numBounds = android.graphics.Rect()
                numPaint.getTextBounds(display.number, 0, display.number.length, numBounds)
                val numBaselineY = Math.round((numberSectionHeight / 2f) + (numBounds.height() / 2f) - numBounds.bottom + (config.numYOffsetDp * density)).toFloat()

                // Unit
                val maxUnitH = unitSectionHeight * 0.90f * config.unitScale
                val maxUnitW = sizePx * 0.96f
                unitPaint.textSize = maxUnitH
                val unitTextW = unitPaint.measureText(display.unit)
                val scaleUnit = minOf(if (unitTextW > 0f) maxUnitW / unitTextW else 1f, 1.0f)
                unitPaint.textSize = maxUnitH * scaleUnit

                val unitBounds = android.graphics.Rect()
                unitPaint.getTextBounds(display.unit, 0, display.unit.length, unitBounds)
                val unitBaselineY = Math.round(numberSectionHeight + (unitSectionHeight / 2f) + (unitBounds.height() / 2f) - unitBounds.bottom + (config.unitYOffsetDp * density)).toFloat()

                if (outlinePaint != null) {
                    outlinePaint.textSize = numPaint.textSize
                    canvas.drawText(display.number, centerX, numBaselineY, outlinePaint)
                    outlinePaint.textSize = unitPaint.textSize
                    canvas.drawText(display.unit, centerX, unitBaselineY, outlinePaint)
                }

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

