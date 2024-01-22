package com.pawlowski.imucollector.data

import android.util.Log
import com.pawlowski.imucollector.domain.model.ModelInfo
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.roundToInt

class IMUServerDataProvider @Inject constructor(
    private val modelTrainingApi: ModelTrainingApi,
    private val modelInfoApi: ModelInfoApi,
    private val interferenceApi: InterferenceApi,
) {
    suspend fun sendImuData(
        sensorData: String,
        activityType: ActivityType,
    ) {
        Log.d("sending", sensorData)
        val response = modelTrainingApi.sendIMU(
            body = sensorData.toRequestBody(contentType = "text/csv".toMediaTypeOrNull()),
            activityType = activityType.code,
        )
        Log.d("sending", "no exception")
        println(response.message())
    }

    suspend fun getPredictions(
        sensorData: String,
    ): Map<ActivityType, Float> = interferenceApi.getPredictions(
        body = sensorData.toRequestBody(contentType = "text/csv".toMediaTypeOrNull()),
    ).body()!!.first().let { tensor ->
        ActivityType.values().associateWith {
            tensor[it.tensorIndex]
        }
    }

    suspend fun getLatestModelInfo(): ModelInfo {
        return modelInfoApi.getLastModel()
    }

    fun softmax(scores: Map<ActivityType, Float>): Map<ActivityType, Int> {
        val expScores = scores.mapValues { (_, score) -> exp(score.toDouble()) }
        val expSum = expScores.values.sum()

        return expScores.mapValues { (_, expScore) -> ((expScore / expSum).toFloat() * 100).roundToInt() }
    }
}
