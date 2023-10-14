package com.pawlowski.imucollector.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class MagnetometerSensorListener(
    private val aggregator: SensorAggregator,
): SensorEventListener {
    // Create a constant to convert nanoseconds to seconds.
    private val NS2S = 1.0f / 1000000000.0f

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            // Axis of the rotation sample, not normalized
            var axisX: Float = event.values[0]
            var axisY: Float = event.values[1]
            var axisZ: Float = event.values[2]

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