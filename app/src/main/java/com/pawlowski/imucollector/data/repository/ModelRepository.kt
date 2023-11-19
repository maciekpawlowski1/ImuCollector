package com.pawlowski.imucollector.data.repository

import android.app.Application
import com.pawlowski.imucollector.data.ModelInfoApi
import com.pawlowski.imucollector.domain.model.ModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ModelRepository @Inject constructor(
    private val modelInfoApi: ModelInfoApi,
    private val context: Application,
) {

    private val lastSavedModel = MutableStateFlow<ModelInfo?>(null)

    suspend fun refreshModel(): ModelInfo {
        val modelInfo = modelInfoApi.getLastModel()

        val modelResponse = modelInfoApi.downloadModel(fileUrl = modelInfo.url)

        val inputStream = modelResponse.body()!!.byteStream()

        val newFile = File(context.filesDir, "v${modelInfo.version}.tflite")

        withContext(Dispatchers.IO) {
            FileOutputStream(newFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return modelInfo.also {
            lastSavedModel.value = it
        }
    }

    fun getSavedModel(): File? {
        return lastSavedModel.value?.let {
            File(context.filesDir, "model.tflite")
        }
    }
}
