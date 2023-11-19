package com.pawlowski.imucollector.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlowski.imucollector.data.ActivityType
import com.pawlowski.imucollector.data.IMUServerDataProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataProvider: IMUServerDataProvider,
    private val aggregator: SensorAggregator,
    private val context: Application,
) : ViewModel() {

    val state: StateFlow<MainState>
        get() = _state.asStateFlow()
    private val _state = MutableStateFlow(MainState())

    fun startActivity(selectedType: ActivityType) {
        if (!state.value.isInProgress) {
            _state.update {
                it.copy(isInProgress = true)
            }
            vibrate(context = context)

            viewModelScope.launch(Dispatchers.IO) {
                val result = aggregator.collect()

                withContext(Dispatchers.Main) {
                    vibrate(context)
                    _state.update {
                        it.copy(isSending = true)
                    }
                }

                dataProvider.sendImuData(
                    sensorData = result,
                    activityType = selectedType,
                )

                withContext(Dispatchers.Main) {
                    _state.update {
                        it.copy(
                            isInProgress = false,
                            isSending = false,
                        )
                    }
                }
            }
        }
    }

    private fun vibrate(context: Context) {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
