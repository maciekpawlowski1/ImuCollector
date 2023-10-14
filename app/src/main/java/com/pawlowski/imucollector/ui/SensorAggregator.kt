package com.pawlowski.imucollector.ui

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.take
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SensorAggregator {
    private val gyroFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    private val accelerometerFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    private val magnetometerFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)


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

    fun onNewMagnetometer(x: Float, y: Float, z: Float, timestamp: Long) {
        magnetometerFlow.tryEmit(
            SensorData(
                x = x,
                y = y,
                z = z,
            )
        )
    }

    @OptIn(FlowPreview::class)
    suspend fun collect(
        interval: Duration = 50.milliseconds,
        count: Int = 50,
    ): String =
        combine(gyroFlow, magnetometerFlow, accelerometerFlow) { gyroSample, magnetometerSample, accelerometerSample ->
            Triple(gyroSample, magnetometerSample, accelerometerSample)
        }.sample(period = interval)
            .map {
                it.toCsvRow()
            }
            .onEach(::println)
            .take(count)
            .fold(initial = "") { acc, value ->
                acc + value
            }
}

private fun Triple<SensorData, SensorData, SensorData>.toCsvRow(): String =
    "${first.x};${first.y};${first.z};${second.x};${second.y};${second.z};${third.x};${third.y};${third.z};\n"