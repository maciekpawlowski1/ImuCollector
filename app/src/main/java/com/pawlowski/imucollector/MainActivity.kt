package com.pawlowski.imucollector

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_FASTEST
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pawlowski.imucollector.data.ActivityType
import com.pawlowski.imucollector.data.IMUServerDataProvider
import com.pawlowski.imucollector.ui.AccelerometerSensorListener
import com.pawlowski.imucollector.ui.GyroSensorListener
import com.pawlowski.imucollector.ui.MagnetometerSensorListener
import com.pawlowski.imucollector.ui.SensorAggregator
import com.pawlowski.imucollector.ui.theme.ImuCollectorTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val aggregator = SensorAggregator()

    private val gyroListener = GyroSensorListener(aggregator)
    private val accelerometerListener = AccelerometerSensorListener(aggregator)
    private val magnetometerListener = MagnetometerSensorListener(aggregator)

    @Inject
    lateinit var dataProvider: IMUServerDataProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImuCollectorTheme {
                // A surface container using the 'background' color from the theme
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
                    val isInProgress = remember {
                        mutableStateOf(false)
                    }
                    val isSending = remember {
                        mutableStateOf(false)
                    }
                    ActivityTypeRow(
                        selectedType = selectedType.value,
                        onActivityTypeChange = {
                            selectedType.value = it
                        },
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isInProgress.value && !isSending.value) {
                        CircularProgressIndicator()
                    }
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current

                    Button(
                        onClick = {
                            isInProgress.value = true
                            vibrate(context)

                            scope.launch(Dispatchers.IO) {
                                val result = aggregator.collect()

                                withContext(Dispatchers.Main) {
                                    vibrate(context)
                                    isSending.value = true
                                }

                                dataProvider.sendImuData(
                                    sensorData = result,
                                    activityType = selectedType.value,
                                )

                                withContext(Dispatchers.Main) {
                                    isInProgress.value = false
                                    isSending.value = false
                                }
                            }
                        },
                        enabled = !isInProgress.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    ) {
                        Text(text = "Start")
                    }
                }
            }
        }

        sensorManager.registerListener(gyroListener, gyroscopeSensor, SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(magnetometerListener, magnetometerSensor, SENSOR_DELAY_FASTEST)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(gyroListener, gyroscopeSensor)
        sensorManager.unregisterListener(accelerometerListener, accelerometerSensor)
        sensorManager.unregisterListener(magnetometerListener, magnetometerSensor)

        super.onDestroy()
    }
}

@Composable
fun ActivityTypeRow(
    selectedType: ActivityType,
    onActivityTypeChange: (ActivityType) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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

private fun vibrate(context: Context) {
    (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
}
