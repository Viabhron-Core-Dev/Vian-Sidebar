package com.example.feature.miniapps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.core.LogKeeper
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.example.R

class CompassPageView(context: Context) : FrameLayout(context), SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private val compassView: CompassDrawView
    private val tvAzimuth: TextView
    private val tvDirection: TextView

    init {
        com.example.core.LogKeeper.writeLog("Compass", "Opened compass page")
        LayoutInflater.from(context).inflate(R.layout.page_compass, this, true)
        
        compassView = findViewById(R.id.compass_view)
        tvAzimuth = findViewById(R.id.tv_azimuth)
        tvDirection = findViewById(R.id.tv_direction)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        }
        
        if (gravity != null && geomagnetic != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                var azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360f
                }
                updateCompass(azimuthInDegrees)
            }
        }
    }

    private fun updateCompass(azimuth: Float) {
        compassView.setAzimuth(azimuth)
        tvAzimuth.text = "${azimuth.toInt()}°"

        val direction = when {
            azimuth >= 337.5 || azimuth < 22.5 -> "N"
            azimuth >= 22.5 && azimuth < 67.5 -> "NE"
            azimuth >= 67.5 && azimuth < 112.5 -> "E"
            azimuth >= 112.5 && azimuth < 157.5 -> "SE"
            azimuth >= 157.5 && azimuth < 202.5 -> "S"
            azimuth >= 202.5 && azimuth < 247.5 -> "SW"
            azimuth >= 247.5 && azimuth < 292.5 -> "W"
            else -> "NW"
        }
        tvDirection.text = direction
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
