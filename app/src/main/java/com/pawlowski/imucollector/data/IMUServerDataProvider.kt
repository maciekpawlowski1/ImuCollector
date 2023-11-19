package com.pawlowski.imucollector.data

import android.util.Log
import com.pawlowski.imucollector.domain.model.ModelInfo
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class IMUServerDataProvider @Inject constructor(
    private val modelTrainingApi: ModelTrainingApi,
    private val modelInfoApi: ModelInfoApi,
) {
    suspend fun sendImuData(
        sensorData: String,
        activityType: ActivityType,
    ) {
        Log.d("sending", sensorData)
        val response = modelTrainingApi.sendIMU(
            body = RequestBody.create(MediaType.parse("text/csv"), sensorData),
            activityType = activityType.code,
        )
        Log.d("sending", "no exception")
        println(response.message())
    }

    suspend fun getLatestModelInfo(): ModelInfo {
        return modelInfoApi.getLastModel()
    }
}
