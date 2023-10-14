package com.pawlowski.imucollector

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.pawlowski.imucollector.ui.AccelerometerSensorListener
import com.pawlowski.imucollector.ui.SensorAggregator
import com.pawlowski.imucollector.ui.GyroSensorListener
import com.pawlowski.imucollector.ui.theme.ImuCollectorTheme
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

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

    private val aggregator = SensorAggregator()

    private val gyroListener = GyroSensorListener(aggregator)
    private val accelerometerListener = AccelerometerSensorListener(aggregator)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImuCollectorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        sensorManager.registerListener(gyroListener, gyroscopeSensor, SENSOR_DELAY_UI)
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SENSOR_DELAY_UI)

        lifecycleScope.launch {
            aggregator.collect(1.seconds)
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(gyroListener, gyroscopeSensor)
        sensorManager.unregisterListener(accelerometerListener, accelerometerSensor)

        super.onDestroy()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImuCollectorTheme {
        Greeting("Android")
    }
}