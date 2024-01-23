package com.pawlowski.imucollector.ui

import com.pawlowski.imucollector.data.ActivityType
import com.pawlowski.imucollector.domain.model.RunMode

data class MainState(
    val isTrackingInProgress: Boolean = false,
    val isSending: Boolean = false,
    val lastStartTime: Long? = null,
    val lastPrediction: Map<ActivityType, Int>? = null,
    val runMode: RunMode = RunMode.TRAINING,
    val error: String? = null,
)
