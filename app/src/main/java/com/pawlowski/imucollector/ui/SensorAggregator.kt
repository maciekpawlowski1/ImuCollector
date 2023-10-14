package com.pawlowski.imucollector.ui

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.take
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
            )
        )
    }

    suspend fun collect(interval: Duration): String =
        combine(gyroFlow, accelerometerFlow) { gyroSample, accelerometerSample ->
            Pair(gyroSample, accelerometerSample)
        }.sample(period = interval)
            .map {
                "Gyro: ${it.first} Accelerometer: ${it.second}"
            }
            .onEach(::println)
            .take(20)
            .fold(initial = "") { acc, value ->
                acc + value
            }
}