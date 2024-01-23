package com.pawlowski.imucollector.data

import kotlin.math.exp
import kotlin.math.roundToInt

fun Map<ActivityType, Float>.softmax(): Map<ActivityType, Int> {
    val expScores = this.mapValues { (_, score) -> exp(score.toDouble()) }
    val expSum = expScores.values.sum()

    return expScores.mapValues { (_, expScore) -> ((expScore / expSum).toFloat() * 100).roundToInt() }
}