package com.pawlowski.imucollector

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pawlowski.imucollector.data.ActivityType
import com.pawlowski.imucollector.domain.model.RunMode
import com.pawlowski.imucollector.ui.AccelerometerSensorListener
import com.pawlowski.imucollector.ui.GyroSensorListener
import com.pawlowski.imucollector.ui.MagnetometerSensorListener
import com.pawlowski.imucollector.ui.MainViewModel
import com.pawlowski.imucollector.ui.SensorAggregator
import com.pawlowski.imucollector.ui.theme.ImuCollectorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val gyroscopeSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private val accelerometerSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val magnetometerSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    @Inject
    lateinit var aggregator: SensorAggregator

    private val gyroListener by lazy {
        GyroSensorListener(aggregator)
    }
    private val accelerometerListener by lazy {
        AccelerometerSensorListener(aggregator)
    }
    private val magnetometerListener by lazy {
        MagnetometerSensorListener(aggregator)
    }

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImuCollectorTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    val selectedType = remember {
                        mutableStateOf(ActivityType.CIRCLES_LEFT)
                    }
                    val state by mainViewModel.state.collectAsState()

                    RunModeRow(
                        selectedRunMode = state.runMode,
                        onRunModeChange = {
                            mainViewModel.changeRunMode(it)
                        },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.runMode == RunMode.TRAINING) {
                        ActivityTypeRow(
                            selectedType = selectedType.value,
                            onActivityTypeChange = {
                                selectedType.value = it
                            },
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (state.isTrackingInProgress && !state.isSending) {
                        CircularProgressIndicator()
                    }

                    if (state.isTrackingInProgress && !state.isSending) {
                        Button(
                            onClick = {
                                mainViewModel.stopActivity()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                        ) {
                            Text(text = "Stop")
                        }
                    } else {
                        Button(
                            onClick = {
                                mainViewModel.startActivity(selectedType = selectedType.value)
                            },
                            enabled = !state.isTrackingInProgress && !state.isSending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                        ) {
                            Text(text = "Start")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.runMode == RunMode.TESTING) {
                        state.lastPrediction?.let { lastPrediction ->
                            lastPrediction.maxBy { it.value }
                                .let {
                                    Text(text = "${it.key.code}: ${it.value}%")
                                }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.error != null) {
                        Text(text = "Error: ${state.error}")
                    }
                }
            }
        }

        registerListeners()
    }

    private fun registerListeners() {
        sensorManager.registerListener(gyroListener, gyroscopeSensor, SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(magnetometerListener, magnetometerSensor, SENSOR_DELAY_FASTEST)
    }

    private fun unRegisterListeners() {
        sensorManager.unregisterListener(gyroListener, gyroscopeSensor)
        sensorManager.unregisterListener(accelerometerListener, accelerometerSensor)
        sensorManager.unregisterListener(magnetometerListener, magnetometerSensor)
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterListeners()
    }
}

@Composable
fun RunModeRow(
    selectedRunMode: RunMode,
    onRunModeChange: (RunMode) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(RunMode.values().toList()) {
            RunModeChip(
                isSelected = selectedRunMode == it,
                type = it,
                onClick = {
                    onRunModeChange(it)
                },
            )
        }
    }
}

@Composable
fun ActivityTypeRow(
    selectedType: ActivityType,
    onActivityTypeChange: (ActivityType) -> Unit,
) {
    LazyColumn(
//        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(ActivityType.values().toList()) {
            ActivityTypeChip(
                isSelected = selectedType == it,
                type = it,
                onClick = {
                    onActivityTypeChange(it)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTypeChip(
    isSelected: Boolean,
    type: ActivityType,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = type.code) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunModeChip(
    isSelected: Boolean,
    type: RunMode,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = type.name) },
    )
}
