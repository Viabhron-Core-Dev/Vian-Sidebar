package com.example.core

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import androidx.core.graphics.drawable.IconCompat
import com.example.util.AppLogger
import java.util.Locale

object DynamicSpeedIconGenerator {

    data class SpeedDisplay(val number: String, val unit: String)

    data class IconConfig(
        val font: String = "sans-serif-condensed",
        val isFakeBold: Boolean = true,
        val numScale: Float = 0.88f, // Calibrated for Xiaomi/Redmi 24dp (48px) status bar canvas to prevent edge clipping on 3-digit & decimal numbers
        val unitScale: Float = 0.95f,
        val numYOffsetDp: Float = 0f,
        val unitYOffsetDp: Float = 0f,
        val bgShape: String = "None",
        val bgRadiusDp: Float = 4f,
        val bgAlpha: Int = 0,
        val layoutMode: String = "Stacked",
        val resScale: Float = 2.0f,
        val aaMode: String = "Smooth",
        val letterSpacing: Float = -0.02f, // Slight condensation for optimal 3-digit and decimal clarity
        val strokeWidthDp: Float = 0f
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

    fun getNotificationIconSize(context: Context): Int {
        val density = context.resources.displayMetrics.density
        return Math.round(24f * density).coerceAtLeast(24)
    }

    /**
     * Generates a crisp, pixel-perfect status bar icon bitmap directly at the 24dp notification small-icon dimension.
     * Snaps coordinates to integer physical pixels with no downsampling blur.
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

        // Determine exact small icon dimension directly from 24dp * device display density
        val sizePx = getNotificationIconSize(context)

        val isLive = (overrideConfig == null)
        var bitmap = if (isLive) cachedBitmap else null
        var canvas = if (isLive) cachedCanvas else null

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
        } else {
            bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
                this.density = densityDpi
            }
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
                isFilterBitmap = false
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
            if (config.font.isNotEmpty()) {
                Typeface.create(config.font, if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
            } else {
                Typeface.create("sans-serif-condensed", if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
            }
        } catch (e: Exception) {
            if (config.isFakeBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }.also { if (config == activeConfig) cachedTypeface = it }

        val paintFlags = when (config.aaMode) {
            "Crisp" -> Paint.ANTI_ALIAS_FLAG
            "Smooth" -> Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG
            else -> Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG
        }

        val numPaint = (if (config == activeConfig) cachedNumPaint else null) ?: Paint(paintFlags).apply {
            color = Color.WHITE
            typeface = tf
            textAlign = Paint.Align.CENTER
            isFakeBoldText = config.isFakeBold
            isFilterBitmap = false
            letterSpacing = config.letterSpacing
            if (config.strokeWidthDp > 0f) {
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = config.strokeWidthDp * density
            } else {
                style = Paint.Style.FILL
            }
        }.also { if (config == activeConfig) cachedNumPaint = it }

        // Update dynamic mutable paint properties
        numPaint.letterSpacing = config.letterSpacing
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
            isFilterBitmap = false
            letterSpacing = config.letterSpacing
            if (config.strokeWidthDp > 0f) {
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = (config.strokeWidthDp * 0.75f) * density
            } else {
                style = Paint.Style.FILL
            }
        }.also { if (config == activeConfig) cachedUnitPaint = it }

        unitPaint.letterSpacing = config.letterSpacing
        unitPaint.isFilterBitmap = false
        if (config.strokeWidthDp > 0f) {
            unitPaint.style = Paint.Style.FILL_AND_STROKE
            unitPaint.strokeWidth = (config.strokeWidthDp * 0.75f) * density
        } else {
            unitPaint.style = Paint.Style.FILL
        }

        // Optional High-Contrast dark shadow/outline behind text
        val outlinePaint = if (config.aaMode == "HighContrast") {
            (if (config == activeConfig) cachedStrokePaint else null) ?: Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(180, 0, 0, 0)
                typeface = tf
                textAlign = Paint.Align.CENTER
                style = Paint.Style.STROKE
                strokeWidth = 1.5f * density
                isFilterBitmap = false
                letterSpacing = config.letterSpacing
            }.also { if (config == activeConfig) cachedStrokePaint = it }
        } else null

        outlinePaint?.letterSpacing = config.letterSpacing
        outlinePaint?.isFilterBitmap = false

        val centerX = Math.round(sizePx / 2f).toFloat()

        when (config.layoutMode) {
            "Compact" -> {
                // Compact: e.g. "45K" or "1.2M" on a single centered line
                val shortUnit = if (display.unit.startsWith("M")) "M" else "K"
                val compactText = "${display.number}$shortUnit"
                val maxH = sizePx * 0.75f * config.numScale
                val maxW = sizePx * 0.94f
                numPaint.textSize = maxH
                val textW = numPaint.measureText(compactText)
                val scale = minOf(if (textW > 0f) maxW / textW else 1f, 1.0f)
                numPaint.textSize = maxH * scale

                val finalMetrics = numPaint.fontMetrics
                val centerY = (sizePx / 2f) + (config.numYOffsetDp * density)
                val baselineY = Math.round(centerY - (finalMetrics.ascent + finalMetrics.descent) / 2f).toFloat()
                if (outlinePaint != null) {
                    outlinePaint.textSize = numPaint.textSize
                    canvas.drawText(compactText, centerX, baselineY, outlinePaint)
                }
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
                if (outlinePaint != null) {
                    outlinePaint.textSize = numPaint.textSize
                    canvas.drawText(display.number, centerX, baselineY, outlinePaint)
                }
                canvas.drawText(display.number, centerX, baselineY, numPaint)
            }
            else -> {
                // Stacked (Default 70/30 Split)
                // Top Speed Value: textSize = sizePx * 0.58f (bold, centered in top 70%)
                val maxNumH = sizePx * 0.58f * config.numScale
                val maxNumW = sizePx * 0.96f
                numPaint.textSize = maxNumH
                val numTextW = numPaint.measureText(display.number)
                val scaleNum = minOf(if (numTextW > 0f) maxNumW / numTextW else 1f, 1.0f)
                numPaint.textSize = maxNumH * scaleNum

                val numFm = numPaint.fontMetrics
                val topCenterY = (sizePx * 0.36f) + (config.numYOffsetDp * density)
                val numY = Math.round(topCenterY - ((numFm.ascent + numFm.descent) / 2f)).toFloat()

                // Bottom Unit (kB/s): textSize = sizePx * 0.28f (bold, centered in bottom 30%)
                val maxUnitH = sizePx * 0.28f * config.unitScale
                val maxUnitW = sizePx * 0.96f
                unitPaint.textSize = maxUnitH
                val unitTextW = unitPaint.measureText(display.unit)
                val scaleUnit = minOf(if (unitTextW > 0f) maxUnitW / unitTextW else 1f, 1.0f)
                unitPaint.textSize = maxUnitH * scaleUnit

                val unitFm = unitPaint.fontMetrics
                val bottomCenterY = (sizePx * 0.82f) + (config.unitYOffsetDp * density)
                val unitY = Math.round(bottomCenterY - ((unitFm.ascent + unitFm.descent) / 2f)).toFloat()

                if (outlinePaint != null) {
                    outlinePaint.textSize = numPaint.textSize
                    canvas.drawText(display.number, centerX, numY, outlinePaint)
                    outlinePaint.textSize = unitPaint.textSize
                    canvas.drawText(display.unit, centerX, unitY, outlinePaint)
                }

                canvas.drawText(display.number, centerX, numY, numPaint)
                canvas.drawText(display.unit, centerX, unitY, unitPaint)
            }
        }
    }

    fun generateIconCompat(context: Context, bytesPerSec: Long, forcedUnit: String? = null): IconCompat {
        val bitmap = generateStatusBarBitmap(context, bytesPerSec, forcedUnit)
        return IconCompat.createWithBitmap(bitmap)
    }

    /**
     * Measures the host device display characteristics, status bar metrics,
     * exact font bounds, and renders baseline calibration diagnostics.
     */
    fun generateDeviceCalibrationReport(context: Context, customConfig: IconConfig? = null): String {
        val config = customConfig ?: activeConfig
        val dm = context.resources.displayMetrics
        val density = dm.density
        val densityDpi = dm.densityDpi
        val scaledDensity = dm.scaledDensity
        val widthPx = dm.widthPixels
        val heightPx = dm.heightPixels
        val xdpi = dm.xdpi
        val ydpi = dm.ydpi

        val smallIconPx = getNotificationIconSize(context)
        
        // System status bar dimensions
        val res = context.resources
        val statusBarHeightId = res.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeightPx = if (statusBarHeightId > 0) res.getDimensionPixelSize(statusBarHeightId) else -1
        val statusBarHeightDp = if (statusBarHeightPx > 0) statusBarHeightPx / density else -1f

        val statusBarIconSizeId = res.getIdentifier("status_bar_icon_size", "dimen", "android")
        val statusBarIconSizePx = if (statusBarIconSizeId > 0) res.getDimensionPixelSize(statusBarIconSizeId) else -1

        // Font metric measurements on test strings
        val tf = try {
            if (config.font.isNotEmpty()) {
                Typeface.create(config.font, if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
            } else {
                Typeface.create("sans-serif-condensed", if (config.isFakeBold) Typeface.BOLD else Typeface.NORMAL)
            }
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val testPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            typeface = tf
            isFakeBoldText = config.isFakeBold
            letterSpacing = config.letterSpacing
        }

        val sampleSizes = listOf(8f, 10f, 12f, 14f, 16f, 18f, 20f, 24f)
        val fontMetricsSummary = StringBuilder()
        for (sp in sampleSizes) {
            testPaint.textSize = sp * scaledDensity
            val fm = testPaint.fontMetrics
            val bounds = Rect()
            testPaint.getTextBounds("99.9", 0, 4, bounds)
            fontMetricsSummary.appendLine("  - Size ${sp}sp (${testPaint.textSize.toInt()}px): Ascent=${fm.ascent.toInt()}px, Descent=${fm.descent.toInt()}px, Top=${fm.top.toInt()}px, Bottom=${fm.bottom.toInt()}px, Bounds[99.9]=${bounds.width()}x${bounds.height()}px")
        }

        // Test specific sample strings at current config scale
        val maxNumH = smallIconPx * 0.58f * config.numScale
        testPaint.textSize = maxNumH
        val b0 = Rect(); testPaint.getTextBounds("0", 0, 1, b0)
        val b450 = Rect(); testPaint.getTextBounds("450", 0, 3, b450)
        val b148 = Rect(); testPaint.getTextBounds("14.8", 0, 4, b148)

        val maxUnitH = smallIconPx * 0.28f * config.unitScale
        val unitPaint = Paint(testPaint).apply { textSize = maxUnitH }
        val bKb = Rect(); unitPaint.getTextBounds("KB/s", 0, 4, bKb)
        val bMb = Rect(); unitPaint.getTextBounds("MB/s", 0, 4, bMb)

        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("[NET_SPEED_DIAGNOSTIC] DEVICE STATUS BAR CALIBRATION")
        sb.appendLine("==================================================")
        sb.appendLine("HARDWARE & OS:")
        sb.appendLine("  - Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})")
        sb.appendLine("  - Brand / Product: ${Build.BRAND} / ${Build.PRODUCT}")
        sb.appendLine("  - Android OS: Version ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}, Build ${Build.DISPLAY})")
        sb.appendLine("")
        sb.appendLine("DISPLAY METRICS:")
        sb.appendLine("  - Resolution: ${widthPx} x ${heightPx} px")
        sb.appendLine("  - Density: ${density}x (DensityDpi: ${densityDpi} dpi, ScaledDensity: ${scaledDensity})")
        sb.appendLine("  - Exact Physical DPI: xdpi=${String.format(Locale.US, "%.1f", xdpi)}, ydpi=${String.format(Locale.US, "%.1f", ydpi)}")
        sb.appendLine("  - System Status Bar Height: ${statusBarHeightPx}px (${String.format(Locale.US, "%.1f", statusBarHeightDp)}dp)")
        sb.appendLine("  - System Status Bar Icon Dimen: ${statusBarIconSizePx}px")
        sb.appendLine("  - Computed SmallIcon Size (24dp native): ${smallIconPx} x ${smallIconPx} px")
        sb.appendLine("")
        sb.appendLine("ACTIVE ICON CONFIGURATION:")
        sb.appendLine("  - Layout Mode: ${config.layoutMode}")
        sb.appendLine("  - Font: ${config.font} (Bold: ${config.isFakeBold})")
        sb.appendLine("  - Number Scale: ${config.numScale}x | Unit Scale: ${config.unitScale}x")
        sb.appendLine("  - Y-Offsets: Number=${config.numYOffsetDp}dp, Unit=${config.unitYOffsetDp}dp")
        sb.appendLine("  - Supersampling Multiplier: ${config.resScale}x | AA Mode: ${config.aaMode}")
        sb.appendLine("  - Letter Spacing: ${config.letterSpacing} | Extra Stroke: ${config.strokeWidthDp}dp")
        sb.appendLine("  - Background: Shape=${config.bgShape}, Radius=${config.bgRadiusDp}dp, Opacity=${config.bgAlpha}/255")
        sb.appendLine("")
        sb.appendLine("TEXT BOUNDS & RENDER MEASUREMENTS:")
        sb.appendLine("  - Target Number TextSize: ${maxNumH.toInt()}px (Top 70% slot)")
        sb.appendLine("    * '0' bounds: ${b0.width()}x${b0.height()}px (W/Icon=${String.format(Locale.US, "%.1f", (b0.width().toFloat()/smallIconPx)*100)}%)")
        sb.appendLine("    * '450' bounds: ${b450.width()}x${b450.height()}px (W/Icon=${String.format(Locale.US, "%.1f", (b450.width().toFloat()/smallIconPx)*100)}%)")
        sb.appendLine("    * '14.8' bounds: ${b148.width()}x${b148.height()}px (W/Icon=${String.format(Locale.US, "%.1f", (b148.width().toFloat()/smallIconPx)*100)}%)")
        sb.appendLine("  - Target Unit TextSize: ${maxUnitH.toInt()}px (Bottom 30% slot)")
        sb.appendLine("    * 'KB/s' bounds: ${bKb.width()}x${bKb.height()}px (W/Icon=${String.format(Locale.US, "%.1f", (bKb.width().toFloat()/smallIconPx)*100)}%)")
        sb.appendLine("    * 'MB/s' bounds: ${bMb.width()}x${bMb.height()}px (W/Icon=${String.format(Locale.US, "%.1f", (bMb.width().toFloat()/smallIconPx)*100)}%)")
        sb.appendLine("")
        sb.appendLine("FONT SCALE METRICS REFERENCE:")
        sb.append(fontMetricsSummary.toString())
        sb.appendLine("==================================================")
        return sb.toString()
    }

    /**
     * Logs the device status bar calibration data to LogKeeper and system log.
     */
    fun logDeviceCalibration(context: Context, customConfig: IconConfig? = null): String {
        val report = generateDeviceCalibrationReport(context, customConfig)
        LogKeeper.writeLog("NET_SPEED_DIAGNOSTIC", report)
        AppLogger.d("NetSpeedDiagnostic", "Status bar calibration report written to LogKeeper")
        return report
    }
}

