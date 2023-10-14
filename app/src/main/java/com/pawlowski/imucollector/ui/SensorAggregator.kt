package com.pawlowski.imucollector.ui

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Duration

class SensorAggregator {
    private val gyroFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    private val accelerometerFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)


    fun onNewGyro(
        x: Float,
        y: Float,
        z: Float,
        timestamp: Long,
    ) {
        gyroFlow.tryEmit(
            SensorData(
                x = x,
                y = y,
                z = z,
                timestamp = timestamp,
            )
        )
    }

    fun onNewAccelerometer(
        x: Float,
        y: Float,
        z: Float,
        timestamp: Long,
    ) {
        accelerometerFlow.tryEmit(
            SensorData(
                x = x,
                y = y,
                z = z,
                timestamp = timestamp,
            )
        )
    }

    suspend fun collect(interval: Duration) {
        combine(gyroFlow, accelerometerFlow) { gyroSample, accelerometerSample ->
            Pair(gyroSample, accelerometerSample)
        }.throttle(interval.inWholeMilliseconds)
            .map {
                "Gyro: ${it.first} Accelerometer: ${it.second}"
            }
            .collect {
                Log.d("collecting", "$it")
            }
    }
}