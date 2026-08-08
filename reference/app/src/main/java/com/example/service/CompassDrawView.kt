package com.example.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CompassDrawView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var azimuth: Float = 0f

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private val northPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    private val southPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BB86FC")
        style = Paint.Style.FILL
    }

    fun setAzimuth(azimuth: Float) {
        this.azimuth = azimuth
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = Math.min(cx, cy) - 10f

        canvas.drawCircle(cx, cy, radius, outerRingPaint)

        canvas.save()
        canvas.rotate(-azimuth, cx, cy)

        // North pointer
        canvas.drawLine(cx, cy, cx, cy - radius + 20f, northPaint)
        // South pointer
        canvas.drawLine(cx, cy, cx, cy + radius - 20f, southPaint)

        for (i in 0 until 360 step 30) {
            canvas.save()
            canvas.rotate(i.toFloat(), cx, cy)
            val isMajor = (i % 90 == 0)
            tickPaint.strokeWidth = if (isMajor) 10f else 5f
            val tickLength = if (isMajor) 40f else 20f
            canvas.drawLine(cx, cy - radius, cx, cy - radius + tickLength, tickPaint)
            canvas.restore()
        }
        canvas.restore()

        canvas.drawCircle(cx, cy, 20f, centerDotPaint)
    }
}
