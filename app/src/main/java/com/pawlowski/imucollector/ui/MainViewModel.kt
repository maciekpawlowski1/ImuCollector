package com.pawlowski.imucollector.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawlowski.imucollector.data.ActivityType
import com.pawlowski.imucollector.data.IMUServerDataProvider
import com.pawlowski.imucollector.domain.model.RunMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
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
                it.copy(
                    isInProgress = true,
                    lastPrediction = null,
                )
            }
            vibrate(context = context)

            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val result = aggregator.collect()

                    withContext(Dispatchers.Main) {
                        vibrate(context)
                        _state.update {
                            it.copy(isSending = true)
                        }
                    }

                    when (state.value.runMode) {
                        RunMode.TRAINING -> {
                            dataProvider.sendImuData(
                                sensorData = result,
                                activityType = selectedType,
                            )
                        }
                        RunMode.TESTING -> {
                            val predictions = dataProvider.getPredictions(sensorData = result)

                            _state.update {
                                it.copy(lastPrediction = predictions)
                            }
                        }
                    }
                }.onFailure { throwable ->
                    ensureActive()
                    _state.update {
                        it.copy(
                            lastPrediction = null,
                            error = throwable.message ?: "Unknown error",
                        )
                    }
                }.onSuccess {
                    _state.update {
                        it.copy(error = null)
                    }
                }

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

    fun changeRunMode(runMode: RunMode) {
        _state.update {
            it.copy(runMode = runMode)
        }
    }

    private fun vibrate(context: Context) {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
