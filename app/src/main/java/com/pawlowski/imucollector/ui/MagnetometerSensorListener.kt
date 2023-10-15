package com.pawlowski.imucollector.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class MagnetometerSensorListener(
    private val aggregator: SensorAggregator,
): SensorEventListener {

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            // Axis of the rotation sample, not normalized
            val axisX: Float = event.values[0]
            val axisY: Float = event.values[1]
            val axisZ: Float = event.values[2]

            aggregator.onNewMagnetometer(
                x = axisX,
                y = axisY,
                z = axisZ,
                timestamp = event.timestamp
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO("Not yet implemented")
    }
}