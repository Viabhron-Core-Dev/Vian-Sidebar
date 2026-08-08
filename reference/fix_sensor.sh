#!/bin/bash
sed -i '/import android.widget.TextView/a\
import android.hardware.Sensor\
import android.hardware.SensorEvent\
import android.hardware.SensorEventListener\
import android.hardware.SensorManager\
' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i '/private var port: Int = 0/a\
    private var sensorManager: SensorManager? = null\
    private var sensorListener: SensorEventListener? = null\
' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i '/pwaServer?.start()/a\
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager\
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)\
        if (rotationSensor != null) {\
            sensorListener = object : SensorEventListener {\
                override fun onSensorChanged(event: SensorEvent?) {\
                    if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {\
                        val rotationMatrix = FloatArray(9)\
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)\
                        val orientation = FloatArray(3)\
                        SensorManager.getOrientation(rotationMatrix, orientation)\
                        val heading = Math.toDegrees(orientation[0].toDouble()).toFloat()\
                        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()\
                        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()\
                        floatingView?.findViewById<WebView>(R.id.webview)?.post {\
                            floatingView?.findViewById<WebView>(R.id.webview)?.evaluateJavascript(\
                                "if(window.onNativeSensorUpdate) { window.onNativeSensorUpdate($heading, $pitch, $roll); }", null\
                            )\
                        }\
                    }\
                }\
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}\
            }\
            sensorManager?.registerListener(sensorListener, rotationSensor, SensorManager.SENSOR_DELAY_UI)\
        }\
' app/src/main/java/com/example/service/PwaWindowManager.kt

sed -i '/sidebarBridge?.destroy()/a\
        if (sensorListener != null) {\
            sensorManager?.unregisterListener(sensorListener)\
        }\
' app/src/main/java/com/example/service/PwaWindowManager.kt
