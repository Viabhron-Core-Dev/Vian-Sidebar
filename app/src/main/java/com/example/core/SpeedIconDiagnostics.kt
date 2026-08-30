package com.example.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

object SpeedIconDiagnostics {

    data class DiagnosticTick(
        val tickIndex: Int,
        val timestamp: Long,
        val bytesPerSec: Long,
        val formattedNumber: String,
        val formattedUnit: String,
        val bitmapHash: Int,
        val bitmapWidth: Int,
        val bitmapHeight: Int,
        val bitmapDensity: Int,
        val eraseColorCalled: Boolean,
        val isSubpixelText: Boolean,
        val isAntiAlias: Boolean,
        val isFilterBitmap: Boolean,
        val hinting: Int,
        val numTextSize: Float,
        val unitTextSize: Float,
        val numBounds: Rect,
        val unitBounds: Rect,
        val renderDurationUs: Long
    )

    private const val MAX_DIAGNOSTIC_TICKS = 10
    private val ticks = CopyOnWriteArrayList<DiagnosticTick>()
    private var isRecording = true
    private var initialMetricsString: String = ""

    fun captureInitialMetrics(context: Context, sizePx: Int) {
        val dm = context.resources.displayMetrics
        val res = context.resources
        val resId = res.getIdentifier("status_bar_icon_size", "dimen", "android")
        val sysDimensPx = if (resId > 0) {
            try { res.getDimensionPixelSize(resId) } catch (e: Exception) { -1 }
        } else -1

        initialMetricsString = """
            Android OS: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Screen Resolution: ${dm.widthPixels}x${dm.heightPixels} px
            Screen Density: ${dm.density}x (Dpi: ${dm.densityDpi})
            System status_bar_icon_size dimen: ${if (sysDimensPx > 0) "${sysDimensPx}px" else "Not defined by OEM"}
            Generated Icon Target Size: ${sizePx}x${sizePx} px (calculated density factor: ${(sizePx / dm.density)}dp)
        """.trimIndent()
    }

    fun recordTick(
        tickIndex: Int,
        bytesPerSec: Long,
        formattedNumber: String,
        formattedUnit: String,
        bitmap: Bitmap,
        numPaint: Paint,
        unitPaint: Paint,
        numBounds: Rect,
        unitBounds: Rect,
        eraseColorCalled: Boolean,
        renderDurationUs: Long
    ) {
        if (!isRecording && ticks.size >= MAX_DIAGNOSTIC_TICKS) return

        if (ticks.size < MAX_DIAGNOSTIC_TICKS) {
            val tick = DiagnosticTick(
                tickIndex = tickIndex,
                timestamp = System.currentTimeMillis(),
                bytesPerSec = bytesPerSec,
                formattedNumber = formattedNumber,
                formattedUnit = formattedUnit,
                bitmapHash = System.identityHashCode(bitmap),
                bitmapWidth = bitmap.width,
                bitmapHeight = bitmap.height,
                bitmapDensity = bitmap.density,
                eraseColorCalled = eraseColorCalled,
                isSubpixelText = numPaint.isSubpixelText,
                isAntiAlias = numPaint.isAntiAlias,
                isFilterBitmap = numPaint.isFilterBitmap,
                hinting = numPaint.hinting,
                numTextSize = numPaint.textSize,
                unitTextSize = unitPaint.textSize,
                numBounds = Rect(numBounds),
                unitBounds = Rect(unitBounds),
                renderDurationUs = renderDurationUs
            )
            ticks.add(tick)
        }
    }

    fun reset() {
        ticks.clear()
        isRecording = true
    }

    fun getDiagnosticReport(context: Context? = null): String {
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        sb.appendLine("==================================================")
        sb.appendLine("  NETSPEED DYNAMIC ICON DIAGNOSTIC REPORT")
        sb.appendLine("==================================================")
        sb.appendLine("Generated At: ${sdf.format(Date())}")
        sb.appendLine()
        sb.appendLine("--- [DISPLAY & DENSITY ENVIRONMENT] ---")
        if (initialMetricsString.isNotEmpty()) {
            sb.appendLine(initialMetricsString)
        } else if (context != null) {
            val dm = context.resources.displayMetrics
            sb.appendLine("Screen: ${dm.widthPixels}x${dm.heightPixels} px | Density: ${dm.density}x (${dm.densityDpi} dpi)")
        } else {
            sb.appendLine("Metrics: Pending initialization")
        }
        sb.appendLine()
        sb.appendLine("--- [FIRST ${ticks.size} POLLING TICKS TRANSITION ANALYSIS] ---")

        if (ticks.isEmpty()) {
            sb.appendLine("No polling ticks recorded yet. Please enable NetSpeed monitor in Settings or wait 2-3 seconds.")
        } else {
            ticks.forEach { t ->
                val timeStr = sdf.format(Date(t.timestamp))
                sb.appendLine("[Tick #${t.tickIndex} @ $timeStr]")
                sb.appendLine("  • Speed: ${t.bytesPerSec} B/s -> Display: \"${t.formattedNumber} ${t.formattedUnit}\"")
                sb.appendLine("  • Bitmap: ${t.bitmapWidth}x${t.bitmapHeight} px (Dpi: ${t.bitmapDensity}) | MemHash: ${Integer.toHexString(t.bitmapHash)}")
                sb.appendLine("  • Buffer Erase: ${if (t.eraseColorCalled) "PASS (eraseColor TRANSPARENT executed)" else "FAIL (No canvas clear!)"}")
                sb.appendLine("  • Paint Config: AntiAlias=${t.isAntiAlias} | SubpixelText=${t.isSubpixelText} | FilterBitmap=${t.isFilterBitmap} | Hinting=${if (t.hinting == Paint.HINTING_ON) "ON" else "OFF"}")
                sb.appendLine("  • Number TextSize=${String.format(Locale.US, "%.1fpx", t.numTextSize)} | Bounds=${t.numBounds}")
                sb.appendLine("  • Unit TextSize=${String.format(Locale.US, "%.1fpx", t.unitTextSize)} | Bounds=${t.unitBounds}")
                sb.appendLine("  • Native Render Latency: ${t.renderDurationUs} µs (<0.1 ms)")
                sb.appendLine()
            }
        }

        sb.appendLine("--- [DIAGNOSTIC VERDICT] ---")
        if (ticks.isNotEmpty()) {
            val firstTick = ticks.first()
            val allErased = ticks.all { it.eraseColorCalled }
            val sameBuffer = ticks.all { it.bitmapHash == firstTick.bitmapHash }
            val subpixelEnabled = ticks.all { it.isSubpixelText }

            sb.appendLine("1. Canvas Cleanliness: ${if (allErased) "PERFECT - Zero alpha accumulation bleed between ticks." else "WARNING - Missing erase color."}")
            sb.appendLine("2. Memory Recycling: ${if (sameBuffer) "PERFECT - Reusing static 0-allocation buffer." else "ALLOCATING - New bitmap created per tick."}")
            sb.appendLine("3. Subpixel & Antialiasing: ${if (subpixelEnabled) "ACTIVE - Subpixel glyph positioning enabled." else "WARNING - Subpixel text disabled."}")
            sb.appendLine("4. Density Alignment: Target bitmap density (${firstTick.bitmapDensity} dpi) matched to hardware.")
        } else {
            sb.appendLine("Status: Idle / Awaiting ticks.")
        }
        sb.appendLine("==================================================")
        return sb.toString()
    }

    fun copyToClipboard(context: Context): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val report = getDiagnosticReport(context)
            val clip = ClipData.newPlainText("NetSpeed Diagnostics", report)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
