package com.pawlowski.imucollector.ui

import com.pawlowski.imucollector.data.ActivityType
import com.pawlowski.imucollector.domain.model.RunMode

data class MainState(
    val isInProgress: Boolean = false,
    val isSending: Boolean = false,
    val lastPrediction: Map<ActivityType, Float>? = null,
    val runMode: RunMode = RunMode.TRAINING,
    val error: String? = null,
)
